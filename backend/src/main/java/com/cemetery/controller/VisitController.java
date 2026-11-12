package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/visits") @RequiredArgsConstructor public class VisitController{
 private final VisitAppointmentRepository repo; private final GravePlotRepository plots; private final SysUserRepository users;
 // 每个日期+时段的接待容量上限（清明等高峰期限流的核心规则）
 public static final int SLOT_CAPACITY=20;
 public static final List<String> SLOTS=List.of("08:00-10:00","10:00-12:00","13:00-15:00","15:00-17:00");
 @GetMapping public Result<List<VisitAppointment>> list(Authentication a,@RequestParam(required=false) String status){
  List<VisitAppointment> all=repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(v->CurrentUser.id(a).equals(v.getFamilyId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(v->status==null||status.isBlank()||status.equals(v.getStatus())).collect(Collectors.toList())); }
 @GetMapping("/slots") public Result<List<Map<String,Object>>> slots(@RequestParam String date){
  LocalDate d=LocalDate.parse(date);
  return Result.ok(SLOTS.stream().map(s->{ long used=repo.findByVisitDate(d).stream().filter(v->s.equals(v.getTimeSlot())&&!"CANCELLED".equals(v.getStatus())).mapToInt(v->v.getVisitors()==null?1:v.getVisitors()).sum();
   Map<String,Object> m=new LinkedHashMap<>(); m.put("slot",s); m.put("capacity",SLOT_CAPACITY); m.put("booked",used); m.put("remaining",SLOT_CAPACITY-used); return m;}).collect(Collectors.toList())); }
 @PostMapping @Transactional public Result<VisitAppointment> create(Authentication a,@RequestBody VisitAppointment r){
  if(r.getVisitDate()==null) throw new BizException("请选择祭扫日期");
  if(r.getVisitDate().isBefore(LocalDate.now())) throw new BizException("不能预约过去的日期");
  if(!SLOTS.contains(r.getTimeSlot())) throw new BizException("时段不合法，可选时段: "+SLOTS);
  long used=repo.findByVisitDate(r.getVisitDate()).stream().filter(v->r.getTimeSlot().equals(v.getTimeSlot())&&!"CANCELLED".equals(v.getStatus())).mapToInt(v->v.getVisitors()==null?1:v.getVisitors()).sum();
  int visitors=r.getVisitors()==null?1:r.getVisitors();
  if(used+visitors>SLOT_CAPACITY) throw new BizException("该时段预约已满（容量 "+SLOT_CAPACITY+" 人，已约 "+used+" 人），请更换时段或日期");
  if(CurrentUser.isFamily(a)){ r.setFamilyId(CurrentUser.id(a)); SysUser u=users.findById(CurrentUser.id(a)).orElse(null); if(u!=null){r.setFamilyName(u.getRealName()); if(r.getPhone()==null) r.setPhone(u.getPhone());} }
  if(r.getPlotId()!=null){ GravePlot p=plots.findById(r.getPlotId()).orElse(null); if(p!=null) r.setPlotNo(p.getPlotNo()); }
  r.setId(null); r.setStatus("BOOKED"); r.setVisitors(visitors); r.setCreateTime(LocalDateTime.now());
  return Result.ok(repo.save(r)); }
 @PostMapping("/{id}/checkin") public Result<VisitAppointment> checkin(Authentication a,@PathVariable Long id){
  if(!CurrentUser.isStaffSide(a)) throw new BizException("仅工作人员可核销");
  VisitAppointment v=repo.findById(id).orElseThrow(()->new BizException("预约不存在"));
  if(!"BOOKED".equals(v.getStatus())) throw new BizException("该预约当前状态不可核销: "+v.getStatus());
  v.setStatus("CHECKED_IN"); return Result.ok(repo.save(v)); }
 @PostMapping("/{id}/cancel") public Result<VisitAppointment> cancel(Authentication a,@PathVariable Long id){
  VisitAppointment v=repo.findById(id).orElseThrow(()->new BizException("预约不存在"));
  if(CurrentUser.isFamily(a)&&!CurrentUser.id(a).equals(v.getFamilyId())) throw new BizException("无权取消他人预约");
  if("CHECKED_IN".equals(v.getStatus())) throw new BizException("已入园的预约不能取消");
  v.setStatus("CANCELLED"); return Result.ok(repo.save(v)); }
 @DeleteMapping("/{id}") public Result<Void> del(Authentication a,@PathVariable Long id){
  if(!CurrentUser.isAdmin(a)) throw new BizException("仅管理员可删除"); repo.deleteById(id); return Result.ok(); } }
