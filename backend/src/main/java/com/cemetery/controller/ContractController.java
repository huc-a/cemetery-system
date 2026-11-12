package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/contracts") @RequiredArgsConstructor public class ContractController{
 private final PurchaseContractRepository contracts; private final GravePlotRepository plots;
 private final PaymentRecordRepository payments; private final ManagementFeeRepository fees; private final SysUserRepository users;
 @GetMapping public Result<List<PurchaseContract>> list(Authentication a,@RequestParam(required=false) String status,@RequestParam(required=false) String kw){
  List<PurchaseContract> all=contracts.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(c->CurrentUser.id(a).equals(c.getBuyerId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(c->status==null||status.isBlank()||status.equals(c.getStatus()))
   .filter(c->kw==null||kw.isBlank()||c.getContractNo().contains(kw)||(c.getBuyerName()!=null&&c.getBuyerName().contains(kw))||(c.getPlotNo()!=null&&c.getPlotNo().contains(kw))).collect(Collectors.toList())); }
 @GetMapping("/{id}") public Result<PurchaseContract> get(Authentication a,@PathVariable Long id){
  PurchaseContract c=contracts.findById(id).orElseThrow(()->new BizException("合同不存在"));
  if(CurrentUser.isFamily(a)&&!CurrentUser.id(a).equals(c.getBuyerId())) throw new BizException("无权查看他人合同");
  return Result.ok(c); }
 // 签约：PENDING_SIGN -> SIGNED，墓位 RESERVED -> SOLD
 @PostMapping("/{id}/sign") @Transactional @PreAuthorize("hasAnyRole('ADMIN','STAFF','FAMILY')") public Result<PurchaseContract> sign(Authentication a,@PathVariable Long id){
  PurchaseContract c=get(a,id).getData();
  if(!"PENDING_SIGN".equals(c.getStatus())) throw new BizException("只有待签约合同可以签约，当前状态: "+c.getStatus());
  c.setStatus("SIGNED"); c.setSignDate(LocalDate.now()); c.setUpdateTime(LocalDateTime.now()); contracts.save(c);
  GravePlot p=plots.findById(c.getPlotId()).orElseThrow(()->new BizException("墓位不存在"));
  p.setStatus("SOLD"); p.setUpdateTime(LocalDateTime.now()); plots.save(p); return Result.ok(c); }
 // 缴费：累计实付；付清后 payStatus=PAID，并生成首期管理费记录（已缴）
 @Data public static class PayReq{ private BigDecimal amount; private String method; }
 @PostMapping("/{id}/pay") @Transactional public Result<PurchaseContract> pay(Authentication a,@PathVariable Long id,@RequestBody PayReq req){
  PurchaseContract c=get(a,id).getData();
  if("CANCELLED".equals(c.getStatus())) throw new BizException("已取消合同不能缴费");
  if(!"SIGNED".equals(c.getStatus())) throw new BizException("请先完成签约再缴费");
  if(req.getAmount()==null||req.getAmount().compareTo(BigDecimal.ZERO)<=0) throw new BizException("缴费金额必须大于0");
  BigDecimal remain=c.getTotalAmount().subtract(c.getPaidAmount());
  if(req.getAmount().compareTo(remain)>0) throw new BizException("缴费金额超过应付余额 "+remain);
  c.setPaidAmount(c.getPaidAmount().add(req.getAmount()));
  boolean paidOff=c.getPaidAmount().compareTo(c.getTotalAmount())>=0;
  c.setPayStatus(paidOff?"PAID":"PARTIAL"); c.setUpdateTime(LocalDateTime.now()); contracts.save(c);
  PaymentRecord pr=new PaymentRecord(); pr.setPayNo("PAY"+System.currentTimeMillis()); pr.setBizType("CONTRACT");
  pr.setBizId(c.getId()); pr.setBizNo(c.getContractNo()); pr.setPayerId(c.getBuyerId()); pr.setPayerName(c.getBuyerName());
  pr.setAmount(req.getAmount()); pr.setMethod(req.getMethod()==null?"ONLINE":req.getMethod()); pr.setStatus("PAID");
  pr.setRemark("购墓合同缴费"); payments.save(pr);
  if(paidOff&&fees.findAll().stream().noneMatch(f->c.getId().equals(f.getContractId()))){
   GravePlot p=plots.findById(c.getPlotId()).orElseThrow(()->new BizException("墓位不存在"));
   ManagementFee f=new ManagementFee(); f.setPlotId(p.getId()); f.setPlotNo(p.getPlotNo()); f.setContractId(c.getId());
   f.setFamilyId(c.getBuyerId()); f.setFamilyName(c.getBuyerName()); f.setYearlyAmount(p.getManageFeeYearly());
   f.setYears(c.getManageYears()); f.setTotalAmount(p.getManageFeeYearly().multiply(BigDecimal.valueOf(c.getManageYears())));
   f.setPeriodStart(LocalDate.now()); f.setPeriodEnd(c.getManageExpireDate()); f.setDueDate(c.getManageExpireDate());
   f.setStatus("PAID"); f.setPayTime(LocalDateTime.now()); f.setRemark("购墓时一次性缴纳 "+c.getManageYears()+" 年管理费"); fees.save(f); }
  return Result.ok(c); }
 @PostMapping("/{id}/cancel") @Transactional @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<PurchaseContract> cancel(@PathVariable Long id){
  PurchaseContract c=contracts.findById(id).orElseThrow(()->new BizException("合同不存在"));
  if("CANCELLED".equals(c.getStatus())) throw new BizException("合同已取消");
  if(c.getPaidAmount()!=null&&c.getPaidAmount().compareTo(BigDecimal.ZERO)>0) throw new BizException("已缴费合同不能直接取消，请走退墓退款流程");
  c.setStatus("CANCELLED"); c.setUpdateTime(LocalDateTime.now()); contracts.save(c);
  GravePlot p=plots.findById(c.getPlotId()).orElse(null);
  if(p!=null&&("RESERVED".equals(p.getStatus())||"SOLD".equals(p.getStatus()))){p.setStatus("FREE"); p.setBuyerId(null); p.setBuyerName(null); p.setUpdateTime(LocalDateTime.now()); plots.save(p);}
  return Result.ok(c); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){contracts.deleteById(id); return Result.ok(); } }
