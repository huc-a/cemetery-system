package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/plots") @RequiredArgsConstructor public class PlotController{
 private final GravePlotRepository plots; private final CemeteryAreaRepository areas;
 private final PurchaseContractRepository contracts; private final PaymentRecordRepository payments; private final SysUserRepository users;
 @GetMapping public Result<List<GravePlot>> list(@RequestParam(required=false) Long areaId,@RequestParam(required=false) String status,@RequestParam(required=false) String kw){
  List<GravePlot> all=areaId!=null?plots.findByAreaId(areaId):plots.findAll(Sort.by("areaId","rowNo","colNo"));
  return Result.ok(all.stream().filter(p->status==null||status.isBlank()||status.equals(p.getStatus()))
   .filter(p->kw==null||kw.isBlank()||p.getPlotNo().contains(kw)).collect(Collectors.toList())); }
 @GetMapping("/{id}") public Result<GravePlot> get(@PathVariable Long id){return Result.ok(plots.findById(id).orElseThrow(()->new BizException("墓位不存在")));}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<GravePlot> create(@RequestBody GravePlot r){
  CemeteryArea a=areas.findById(r.getAreaId()).orElseThrow(()->new BizException("墓区不存在"));
  r.setId(null); r.setAreaName(a.getName()); if(r.getStatus()==null)r.setStatus("FREE");
  r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now()); return Result.ok(plots.save(r)); }
 @PostMapping("/batch-generate") @PreAuthorize("hasRole('ADMIN')") public Result<Map<String,Object>> batch(@RequestBody Map<String,Object> req){
  Long areaId=Long.valueOf(req.get("areaId").toString()); int rows=Integer.parseInt(req.get("rows").toString()); int cols=Integer.parseInt(req.get("cols").toString());
  CemeteryArea a=areas.findById(areaId).orElseThrow(()->new BizException("墓区不存在"));
  BigDecimal price=new BigDecimal(req.getOrDefault("price","18800").toString()); BigDecimal fee=new BigDecimal(req.getOrDefault("manageFeeYearly","200").toString());
  String type=String.valueOf(req.getOrDefault("plotType","双穴墓")); int created=0;
  Set<String> exist=plots.findByAreaId(areaId).stream().map(GravePlot::getPlotNo).collect(Collectors.toSet());
  for(int r=1;r<=rows;r++) for(int c=1;c<=cols;c++){ String no=a.getCode()+"-"+r+"排-"+c+"号"; if(exist.contains(no)) continue;
   GravePlot p=new GravePlot(); p.setAreaId(areaId); p.setAreaName(a.getName()); p.setPlotNo(no); p.setRowNo(r); p.setColNo(c);
   p.setPlotType(type); p.setAreaSize(new BigDecimal("3.50")); p.setPrice(price); p.setManageFeeYearly(fee);
   p.setOrientation("坐北朝南"); p.setStatus("FREE"); plots.save(p); created++; }
  return Result.ok(Map.of("created",created)); }
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<GravePlot> update(@PathVariable Long id,@RequestBody GravePlot r){
  GravePlot p=plots.findById(id).orElseThrow(()->new BizException("墓位不存在"));
  if(r.getPrice()!=null)p.setPrice(r.getPrice()); if(r.getManageFeeYearly()!=null)p.setManageFeeYearly(r.getManageFeeYearly());
  if(r.getPlotType()!=null)p.setPlotType(r.getPlotType()); if(r.getStatus()!=null)p.setStatus(r.getStatus());
  if(r.getOrientation()!=null)p.setOrientation(r.getOrientation()); p.setUpdateTime(LocalDateTime.now()); return Result.ok(plots.save(p)); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){
  GravePlot p=plots.findById(id).orElseThrow(()->new BizException("墓位不存在"));
  if(!"FREE".equals(p.getStatus())) throw new BizException("非空闲墓位不能删除"); plots.deleteById(id); return Result.ok(); }
 // ===== 核心业务：选墓购买（选墓→签约→缴费状态流转） =====
 @Data public static class BuyReq{ private Long plotId; private Long buyerId; private String deceasedName; private Integer manageYears; private String remark; }
 @PostMapping("/buy") @Transactional public Result<PurchaseContract> buy(Authentication auth,@RequestBody BuyReq req){
  GravePlot p=plots.findById(req.getPlotId()).orElseThrow(()->new BizException("墓位不存在"));
  if("SOLD".equals(p.getStatus())||"BURIED".equals(p.getStatus())) throw new BizException("该墓位已售出，不能重复购买");
  if("MAINTENANCE".equals(p.getStatus())) throw new BizException("该墓位维护中，暂不可购买");
  if("RESERVED".equals(p.getStatus())&&CurrentUser.isFamily(auth)&&p.getBuyerId()!=null&&!p.getBuyerId().equals(CurrentUser.id(auth)))
   throw new BizException("该墓位已被他人预订");
  Long buyerId=req.getBuyerId();
  if(CurrentUser.isFamily(auth)) buyerId=CurrentUser.id(auth);
  if(buyerId==null) throw new BizException("请选择购买人");
  SysUser buyer=users.findById(buyerId).orElseThrow(()->new BizException("购买人不存在"));
  if(contracts.existsByPlotIdAndStatusNot(p.getId(),"CANCELLED")) throw new BizException("该墓位已存在有效合同，不能重复购买");
  int years=req.getManageYears()==null?20:req.getManageYears();
  PurchaseContract c=new PurchaseContract();
  c.setContractNo("HT"+LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+String.format("%03d",(int)(Math.random()*900+100)));
  c.setPlotId(p.getId()); c.setPlotNo(p.getPlotNo()); c.setAreaId(p.getAreaId()); c.setAreaName(p.getAreaName());
  c.setBuyerId(buyer.getId()); c.setBuyerName(buyer.getRealName()); c.setBuyerPhone(buyer.getPhone());
  c.setDeceasedName(req.getDeceasedName()); c.setPlotPrice(p.getPrice());
  BigDecimal svc=p.getPrice().multiply(new BigDecimal("0.02")).setScale(2,java.math.RoundingMode.HALF_UP);
  c.setServiceFee(svc); c.setTotalAmount(p.getPrice().add(svc).add(p.getManageFeeYearly().multiply(BigDecimal.valueOf(years))));
  c.setPaidAmount(BigDecimal.ZERO); c.setStatus("PENDING_SIGN"); c.setPayStatus("UNPAID");
  c.setManageYears(years); c.setManageExpireDate(LocalDate.now().plusYears(years)); c.setRemark(req.getRemark());
  contracts.save(c);
  p.setStatus("RESERVED"); p.setBuyerId(buyer.getId()); p.setBuyerName(buyer.getRealName()); p.setUpdateTime(LocalDateTime.now()); plots.save(p);
  return Result.ok(c); }
 @PostMapping("/{id}/reserve") @Transactional public Result<GravePlot> reserve(Authentication auth,@PathVariable Long id){
  GravePlot p=plots.findById(id).orElseThrow(()->new BizException("墓位不存在"));
  if(!"FREE".equals(p.getStatus())) throw new BizException("只有空闲墓位可以预订");
  p.setStatus("RESERVED"); p.setBuyerId(CurrentUser.id(auth));
  SysUser u=users.findById(CurrentUser.id(auth)).orElse(null); if(u!=null)p.setBuyerName(u.getRealName());
  p.setUpdateTime(LocalDateTime.now()); return Result.ok(plots.save(p)); }
 @PostMapping("/{id}/cancel-reserve") @Transactional public Result<GravePlot> cancelReserve(@PathVariable Long id){
  GravePlot p=plots.findById(id).orElseThrow(()->new BizException("墓位不存在"));
  if(!"RESERVED".equals(p.getStatus())) throw new BizException("该墓位不是预订状态");
  if(contracts.existsByPlotIdAndStatusNot(id,"CANCELLED")) throw new BizException("该墓位已有合同，请先处理合同");
  p.setStatus("FREE"); p.setBuyerId(null); p.setBuyerName(null); p.setUpdateTime(LocalDateTime.now()); return Result.ok(plots.save(p)); } }
