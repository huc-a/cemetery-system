package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/fees") @RequiredArgsConstructor public class FeeController{
 private final ManagementFeeRepository fees; private final GravePlotRepository plots; private final PaymentRecordRepository payments; private final SysUserRepository users;
 @GetMapping public Result<List<ManagementFee>> list(Authentication a,@RequestParam(required=false) String status){
  List<ManagementFee> all=fees.findAll(Sort.by(Sort.Direction.ASC,"dueDate"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(f->CurrentUser.id(a).equals(f.getFamilyId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(f->status==null||status.isBlank()||status.equals(effectiveStatus(f))).map(f->{f.setStatus(effectiveStatus(f)); return f;}).collect(Collectors.toList())); }
 // 到期状态动态计算：已缴 PAID；逾期 OVERDUE；30天内到期 DUE_SOON；其余 UNPAID
 public static String effectiveStatus(ManagementFee f){
  if("PAID".equals(f.getStatus())) return "PAID";
  if(f.getDueDate()==null) return f.getStatus();
  LocalDate now=LocalDate.now();
  if(f.getDueDate().isBefore(now)) return "OVERDUE";
  if(!f.getDueDate().isAfter(now.plusDays(30))) return "DUE_SOON";
  return "UNPAID"; }
 @GetMapping("/alerts") public Result<Map<String,Object>> alerts(Authentication a){
  List<ManagementFee> all=list(a,null).getData();
  List<ManagementFee> overdue=all.stream().filter(f->"OVERDUE".equals(f.getStatus())).collect(Collectors.toList());
  List<ManagementFee> dueSoon=all.stream().filter(f->"DUE_SOON".equals(f.getStatus())).collect(Collectors.toList());
  Map<String,Object> m=new LinkedHashMap<>(); m.put("overdue",overdue); m.put("dueSoon",dueSoon);
  m.put("overdueCount",overdue.size()); m.put("dueSoonCount",dueSoon.size());
  m.put("overdueAmount",overdue.stream().map(ManagementFee::getTotalAmount).reduce(BigDecimal.ZERO,BigDecimal::add));
  return Result.ok(m); }
 @PostMapping public Result<ManagementFee> create(Authentication a,@RequestBody ManagementFee r){
  if(!CurrentUser.isStaffSide(a)) throw new BizException("无权操作");
  GravePlot p=plots.findById(r.getPlotId()).orElseThrow(()->new BizException("墓位不存在"));
  r.setId(null); r.setPlotNo(p.getPlotNo()); if(r.getYearlyAmount()==null||r.getYearlyAmount().compareTo(BigDecimal.ZERO)==0) r.setYearlyAmount(p.getManageFeeYearly());
  if(r.getYears()==null) r.setYears(1); r.setTotalAmount(r.getYearlyAmount().multiply(BigDecimal.valueOf(r.getYears())));
  if(r.getPeriodStart()==null) r.setPeriodStart(LocalDate.now());
  r.setPeriodEnd(r.getPeriodStart().plusYears(r.getYears())); r.setDueDate(r.getPeriodEnd()); r.setStatus("UNPAID"); r.setCreateTime(LocalDateTime.now());
  return Result.ok(fees.save(r)); }
 // 续费：缴纳一笔到期/未缴费用，并顺延生成下一周期费用记录
 @Data public static class RenewReq{ private Integer years; private String method; }
 @PostMapping("/{id}/renew") @Transactional public Result<ManagementFee> renew(Authentication a,@PathVariable Long id,@RequestBody(required=false) RenewReq req){
  ManagementFee f=fees.findById(id).orElseThrow(()->new BizException("费用记录不存在"));
  if(CurrentUser.isFamily(a)&&!CurrentUser.id(a).equals(f.getFamilyId())) throw new BizException("无权为他人续费");
  if("PAID".equals(f.getStatus())) throw new BizException("该笔费用已缴纳，无需续费");
  int yrs=(req!=null&&req.getYears()!=null)?req.getYears():f.getYears();
  f.setStatus("PAID"); f.setPayTime(LocalDateTime.now()); fees.save(f);
  PaymentRecord pr=new PaymentRecord(); pr.setPayNo("PAY"+System.currentTimeMillis()); pr.setBizType("MANAGE_FEE");
  pr.setBizId(f.getId()); pr.setBizNo(f.getPlotNo()); pr.setPayerId(f.getFamilyId()); pr.setPayerName(f.getFamilyName());
  pr.setAmount(f.getTotalAmount()); pr.setMethod(req!=null&&req.getMethod()!=null?req.getMethod():"ONLINE"); pr.setStatus("PAID"); pr.setRemark("管理费缴纳"); payments.save(pr);
  ManagementFee next=new ManagementFee(); next.setPlotId(f.getPlotId()); next.setPlotNo(f.getPlotNo()); next.setContractId(f.getContractId());
  next.setFamilyId(f.getFamilyId()); next.setFamilyName(f.getFamilyName()); next.setYearlyAmount(f.getYearlyAmount());
  next.setYears(yrs); next.setTotalAmount(f.getYearlyAmount().multiply(BigDecimal.valueOf(yrs)));
  LocalDate start=f.getPeriodEnd()!=null?f.getPeriodEnd():LocalDate.now();
  next.setPeriodStart(start); next.setPeriodEnd(start.plusYears(yrs)); next.setDueDate(start.plusYears(yrs));
  next.setStatus("UNPAID"); next.setRemark("续费顺延周期"); fees.save(next);
  return Result.ok(next); }
 @DeleteMapping("/{id}") public Result<Void> del(Authentication a,@PathVariable Long id){
  if(!CurrentUser.isAdmin(a)) throw new BizException("仅管理员可删除"); fees.deleteById(id); return Result.ok(); } }
