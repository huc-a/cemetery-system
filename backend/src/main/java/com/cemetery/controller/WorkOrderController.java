package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/work-orders") @RequiredArgsConstructor public class WorkOrderController{
 private final WorkOrderRepository repo; private final GravePlotRepository plots; private final SysUserRepository users;
 @GetMapping public Result<List<WorkOrder>> list(Authentication a,@RequestParam(required=false) String status){
  List<WorkOrder> all=repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if("STAFF".equals(CurrentUser.role(a))) all=all.stream().filter(o->CurrentUser.id(a).equals(o.getAssigneeId())||CurrentUser.id(a).equals(o.getReporterId())).collect(Collectors.toList());
  if(CurrentUser.isFamily(a)) all=all.stream().filter(o->CurrentUser.id(a).equals(o.getReporterId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(o->status==null||status.isBlank()||status.equals(o.getStatus())).collect(Collectors.toList())); }
 @PostMapping public Result<WorkOrder> create(Authentication a,@RequestBody WorkOrder r){
  r.setId(null); r.setOrderNo("GD"+LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+String.format("%03d",(int)(Math.random()*900+100)));
  r.setReporterId(CurrentUser.id(a)); SysUser u=users.findById(CurrentUser.id(a)).orElse(null); if(u!=null) r.setReporterName(u.getRealName());
  if(r.getPlotId()!=null){ GravePlot p=plots.findById(r.getPlotId()).orElse(null); if(p!=null){r.setPlotNo(p.getPlotNo()); r.setAreaName(p.getAreaName());} }
  if(r.getStatus()==null) r.setStatus("PENDING"); if(r.getPriority()==null) r.setPriority("NORMAL");
  r.setCreateTime(LocalDateTime.now()); r.setUpdateTime(LocalDateTime.now()); return Result.ok(repo.save(r)); }
 // 派单 -> 处理中 -> 完成（结果必填）流转，非法流转拒绝
 @Data public static class FlowReq{ private Long assigneeId; private String resultNote; }
 @PostMapping("/{id}/assign") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<WorkOrder> assign(@PathVariable Long id,@RequestBody FlowReq req){
  WorkOrder o=repo.findById(id).orElseThrow(()->new BizException("工单不存在"));
  if(!"PENDING".equals(o.getStatus())) throw new BizException("只有待派单工单可以派单，当前: "+o.getStatus());
  SysUser st=users.findById(req.getAssigneeId()).orElseThrow(()->new BizException("工作人员不存在"));
  o.setAssigneeId(st.getId()); o.setAssigneeName(st.getRealName()); o.setStatus("ASSIGNED"); o.setUpdateTime(LocalDateTime.now()); return Result.ok(repo.save(o)); }
 @PostMapping("/{id}/start") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<WorkOrder> start(@PathVariable Long id){
  WorkOrder o=repo.findById(id).orElseThrow(()->new BizException("工单不存在"));
  if(!"ASSIGNED".equals(o.getStatus())) throw new BizException("只有已派单工单可以开始处理，当前: "+o.getStatus());
  o.setStatus("PROCESSING"); o.setUpdateTime(LocalDateTime.now()); return Result.ok(repo.save(o)); }
 @PostMapping("/{id}/complete") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<WorkOrder> complete(@PathVariable Long id,@RequestBody(required=false) FlowReq req){
  WorkOrder o=repo.findById(id).orElseThrow(()->new BizException("工单不存在"));
  if(!"PROCESSING".equals(o.getStatus())&&!"ASSIGNED".equals(o.getStatus())) throw new BizException("当前状态不可完成: "+o.getStatus());
  if(req==null||req.getResultNote()==null||req.getResultNote().isBlank()) throw new BizException("完成工单必须填写处理结果");
  o.setStatus("COMPLETED"); o.setResultNote(req.getResultNote()); o.setFinishTime(LocalDateTime.now()); o.setUpdateTime(LocalDateTime.now()); return Result.ok(repo.save(o)); }
 @PostMapping("/{id}/cancel") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<WorkOrder> cancel(@PathVariable Long id){
  WorkOrder o=repo.findById(id).orElseThrow(()->new BizException("工单不存在"));
  if("COMPLETED".equals(o.getStatus())) throw new BizException("已完成工单不能取消");
  o.setStatus("CANCELLED"); o.setUpdateTime(LocalDateTime.now()); return Result.ok(repo.save(o)); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){repo.deleteById(id); return Result.ok(); } }
