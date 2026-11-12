package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/burials") @RequiredArgsConstructor public class BurialController{
 private final BurialRecordRepository repo; private final GravePlotRepository plots; private final DeceasedRepository deceasedRepo; private final SysUserRepository users;
 @GetMapping public Result<List<BurialRecord>> list(Authentication a,@RequestParam(required=false) String status){
  List<BurialRecord> all=repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(b->CurrentUser.id(a).equals(b.getFamilyId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(b->status==null||status.isBlank()||status.equals(b.getStatus())).collect(Collectors.toList())); }
 @PostMapping @Transactional public Result<BurialRecord> create(Authentication a,@RequestBody BurialRecord r){
  GravePlot p=plots.findById(r.getPlotId()).orElseThrow(()->new BizException("墓位不存在"));
  if(!"SOLD".equals(p.getStatus())&&!"BURIED".equals(p.getStatus())) throw new BizException("只有已售墓位可以办理安葬，当前墓位状态: "+p.getStatus());
  Deceased d=deceasedRepo.findById(r.getDeceasedId()).orElseThrow(()->new BizException("逝者档案不存在"));
  if(d.getPlotId()!=null&&!d.getPlotId().equals(p.getId())) throw new BizException("该逝者已绑定其他墓位");
  long clash=repo.findAll().stream().filter(b->p.getId().equals(b.getPlotId())&&r.getBurialDate()!=null&&r.getBurialDate().equals(b.getBurialDate())&&r.getTimeSlot()!=null&&r.getTimeSlot().equals(b.getTimeSlot())&&!"CANCELLED".equals(b.getStatus())).count();
  if(clash>0) throw new BizException("该墓位在所选日期时段已有安葬安排，请换时段");
  r.setId(null); r.setPlotNo(p.getPlotNo()); r.setDeceasedName(d.getName());
  if(CurrentUser.isFamily(a)) r.setFamilyId(CurrentUser.id(a));
  if(r.getFamilyId()!=null){SysUser fam=users.findById(r.getFamilyId()).orElse(null); if(fam!=null) r.setFamilyName(fam.getRealName());}
  if(r.getStatus()==null) r.setStatus("BOOKED"); r.setCreateTime(LocalDateTime.now());
  return Result.ok(repo.save(r)); }
 // 完成安葬：墓位 SOLD -> BURIED，逝者档案回填安葬日期
 @PostMapping("/{id}/complete") @Transactional public Result<BurialRecord> complete(Authentication a,@PathVariable Long id){
  if(!CurrentUser.isStaffSide(a)) throw new BizException("仅工作人员可登记安葬完成");
  BurialRecord b=repo.findById(id).orElseThrow(()->new BizException("安葬记录不存在"));
  if(!"BOOKED".equals(b.getStatus())) throw new BizException("只有已预约状态可以完成安葬，当前: "+b.getStatus());
  b.setStatus("COMPLETED"); repo.save(b);
  GravePlot p=plots.findById(b.getPlotId()).orElseThrow(()->new BizException("墓位不存在"));
  p.setStatus("BURIED"); p.setUpdateTime(LocalDateTime.now()); plots.save(p);
  Deceased d=deceasedRepo.findById(b.getDeceasedId()).orElse(null);
  if(d!=null){ d.setBurialDate(b.getBurialDate()); d.setPlotId(p.getId()); d.setPlotNo(p.getPlotNo()); deceasedRepo.save(d); }
  return Result.ok(b); }
 @PostMapping("/{id}/cancel") @Transactional public Result<BurialRecord> cancel(@PathVariable Long id){
  BurialRecord b=repo.findById(id).orElseThrow(()->new BizException("安葬记录不存在"));
  if("COMPLETED".equals(b.getStatus())) throw new BizException("已完成的安葬不能取消");
  b.setStatus("CANCELLED"); return Result.ok(repo.save(b)); }
 @DeleteMapping("/{id}") public Result<Void> del(Authentication a,@PathVariable Long id){
  if(!CurrentUser.isAdmin(a)) throw new BizException("仅管理员可删除"); repo.deleteById(id); return Result.ok(); } }
