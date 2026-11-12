package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api") @RequiredArgsConstructor public class ServiceController{
 private final ServiceProductRepository products; private final ServiceOrderRepository orders;
 private final PaymentRecordRepository payments; private final GravePlotRepository plots; private final SysUserRepository users;
 @GetMapping("/services") public Result<List<ServiceProduct>> listServices(@RequestParam(required=false) String category){
  List<ServiceProduct> all=products.findAll(Sort.by(Sort.Direction.DESC,"sales"));
  return Result.ok(all.stream().filter(s->category==null||category.isBlank()||category.equals(s.getCategory())).collect(Collectors.toList())); }
 @PostMapping("/services") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<ServiceProduct> createService(@RequestBody ServiceProduct r){r.setId(null); r.setCreateTime(LocalDateTime.now()); if(r.getStatus()==null)r.setStatus(1); return Result.ok(products.save(r));}
 @PutMapping("/services/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<ServiceProduct> updateService(@PathVariable Long id,@RequestBody ServiceProduct r){
  ServiceProduct s=products.findById(id).orElseThrow(()->new BizException("服务项目不存在")); r.setId(id); r.setCreateTime(s.getCreateTime()); r.setSales(s.getSales()); return Result.ok(products.save(r)); }
 @DeleteMapping("/services/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> delService(@PathVariable Long id){products.deleteById(id); return Result.ok();}
 // ===== 服务下单 =====
 @Data public static class OrderReq{ private Long productId; private Long plotId; private Integer quantity; private LocalDate serviceDate; private String timeSlot; private String remark; }
 @PostMapping("/service-orders") @Transactional public Result<ServiceOrder> createOrder(Authentication a,@RequestBody OrderReq req){
  ServiceProduct s=products.findById(req.getProductId()).orElseThrow(()->new BizException("服务项目不存在"));
  if(s.getStatus()!=null&&s.getStatus()==0) throw new BizException("该服务已下架");
  int qty=req.getQuantity()==null?1:req.getQuantity(); if(qty<1) throw new BizException("数量至少为1");
  ServiceOrder o=new ServiceOrder();
  o.setOrderNo("FW"+LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+String.format("%03d",(int)(Math.random()*900+100)));
  o.setProductId(s.getId()); o.setProductName(s.getName()); o.setQuantity(qty);
  o.setTotalAmount(s.getPrice().multiply(BigDecimal.valueOf(qty)));
  o.setFamilyId(CurrentUser.id(a)); SysUser u=users.findById(CurrentUser.id(a)).orElse(null); if(u!=null)o.setFamilyName(u.getRealName());
  if(req.getPlotId()!=null){ GravePlot p=plots.findById(req.getPlotId()).orElse(null); if(p!=null){o.setPlotId(p.getId()); o.setPlotNo(p.getPlotNo());} }
  o.setServiceDate(req.getServiceDate()); o.setTimeSlot(req.getTimeSlot()); o.setRemark(req.getRemark());
  o.setStatus("PENDING"); o.setPayStatus("UNPAID"); orders.save(o);
  s.setSales(s.getSales()+qty); products.save(s); return Result.ok(o); }
 @GetMapping("/service-orders") public Result<List<ServiceOrder>> listOrders(Authentication a,@RequestParam(required=false) String status){
  List<ServiceOrder> all=orders.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(o->CurrentUser.id(a).equals(o.getFamilyId())).collect(Collectors.toList());
  if("STAFF".equals(CurrentUser.role(a))) all=all.stream().filter(o->o.getStaffId()==null||CurrentUser.id(a).equals(o.getStaffId())||"PENDING".equals(o.getStatus())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(o->status==null||status.isBlank()||status.equals(o.getStatus())).collect(Collectors.toList())); }
 @PostMapping("/service-orders/{id}/pay") @Transactional public Result<ServiceOrder> payOrder(Authentication a,@PathVariable Long id){
  ServiceOrder o=orders.findById(id).orElseThrow(()->new BizException("订单不存在"));
  if(CurrentUser.isFamily(a)&&!CurrentUser.id(a).equals(o.getFamilyId())) throw new BizException("无权支付他人订单");
  if("PAID".equals(o.getPayStatus())) throw new BizException("订单已支付");
  if("CANCELLED".equals(o.getStatus())) throw new BizException("已取消订单不能支付");
  o.setPayStatus("PAID"); o.setUpdateTime(LocalDateTime.now()); orders.save(o);
  PaymentRecord pr=new PaymentRecord(); pr.setPayNo("PAY"+System.currentTimeMillis()); pr.setBizType("SERVICE");
  pr.setBizId(o.getId()); pr.setBizNo(o.getOrderNo()); pr.setPayerId(o.getFamilyId()); pr.setPayerName(o.getFamilyName());
  pr.setAmount(o.getTotalAmount()); pr.setMethod("ONLINE"); pr.setStatus("PAID"); pr.setRemark("服务订单支付: "+o.getProductName()); payments.save(pr);
  return Result.ok(o); }
 // 状态流转：PENDING -> ASSIGNED -> PROCESSING -> COMPLETED（代祭扫完成后回传结果说明）
 @Data public static class FlowReq{ private Long staffId; private String resultNote; }
 @PostMapping("/service-orders/{id}/assign") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<ServiceOrder> assign(@PathVariable Long id,@RequestBody FlowReq req){
  ServiceOrder o=orders.findById(id).orElseThrow(()->new BizException("订单不存在"));
  if(!"PENDING".equals(o.getStatus())) throw new BizException("只有待派单订单可以派单，当前: "+o.getStatus());
  SysUser st=users.findById(req.getStaffId()).orElseThrow(()->new BizException("工作人员不存在"));
  o.setStaffId(st.getId()); o.setStaffName(st.getRealName()); o.setStatus("ASSIGNED"); o.setUpdateTime(LocalDateTime.now()); return Result.ok(orders.save(o)); }
 @PostMapping("/service-orders/{id}/start") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<ServiceOrder> start(@PathVariable Long id){
  ServiceOrder o=orders.findById(id).orElseThrow(()->new BizException("订单不存在"));
  if(!"ASSIGNED".equals(o.getStatus())) throw new BizException("只有已派单订单可以开始服务，当前: "+o.getStatus());
  o.setStatus("PROCESSING"); o.setUpdateTime(LocalDateTime.now()); return Result.ok(orders.save(o)); }
 @PostMapping("/service-orders/{id}/complete") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<ServiceOrder> complete(@PathVariable Long id,@RequestBody(required=false) FlowReq req){
  ServiceOrder o=orders.findById(id).orElseThrow(()->new BizException("订单不存在"));
  if(!"PROCESSING".equals(o.getStatus())&&!"ASSIGNED".equals(o.getStatus())) throw new BizException("当前状态不可完成: "+o.getStatus());
  o.setStatus("COMPLETED"); if(req!=null) o.setResultNote(req.getResultNote()); o.setUpdateTime(LocalDateTime.now()); return Result.ok(orders.save(o)); }
 @PostMapping("/service-orders/{id}/cancel") public Result<ServiceOrder> cancelOrder(Authentication a,@PathVariable Long id){
  ServiceOrder o=orders.findById(id).orElseThrow(()->new BizException("订单不存在"));
  if("COMPLETED".equals(o.getStatus())) throw new BizException("已完成订单不能取消");
  o.setStatus("CANCELLED"); o.setUpdateTime(LocalDateTime.now()); return Result.ok(orders.save(o)); } }
