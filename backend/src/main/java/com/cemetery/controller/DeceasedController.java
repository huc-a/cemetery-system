package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/deceased") @RequiredArgsConstructor public class DeceasedController{
 private final DeceasedRepository repo; private final GravePlotRepository plots; private final MemorialHallRepository halls; private final SysUserRepository users;
 @GetMapping public Result<List<Deceased>> list(Authentication a,@RequestParam(required=false) String kw){
  List<Deceased> all=repo.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(d->CurrentUser.id(a).equals(d.getFamilyId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(d->kw==null||kw.isBlank()||d.getName().contains(kw)).collect(Collectors.toList())); }
 @GetMapping("/{id}") public Result<Deceased> get(@PathVariable Long id){return Result.ok(repo.findById(id).orElseThrow(()->new BizException("逝者档案不存在")));}
 @PostMapping @Transactional public Result<Deceased> create(Authentication a,@RequestBody Deceased r){
  if(CurrentUser.isFamily(a)) r.setFamilyId(CurrentUser.id(a));
  if(r.getFamilyId()!=null){ SysUser fam=users.findById(r.getFamilyId()).orElse(null); if(fam!=null) r.setFamilyName(fam.getRealName()); }
  if(r.getPlotId()!=null){ GravePlot p=plots.findById(r.getPlotId()).orElseThrow(()->new BizException("墓位不存在")); r.setPlotNo(p.getPlotNo()); }
  if(r.getBirthDate()!=null&&r.getDeathDate()!=null) r.setAge(java.time.Period.between(r.getBirthDate(),r.getDeathDate()).getYears());
  r.setId(null); r.setCreateTime(LocalDateTime.now()); repo.save(r);
  // 自动创建在线纪念馆（一人一馆）
  MemorialHall h=new MemorialHall(); h.setDeceasedId(r.getId()); h.setDeceasedName(r.getName()); h.setPlotId(r.getPlotId()); h.setPlotNo(r.getPlotNo());
  h.setFamilyId(r.getFamilyId()); h.setFamilyName(r.getFamilyName()); h.setBiography(r.getBiography()); h.setPhotoEmoji(r.getPhotoEmoji());
  h.setEpitaph("音容宛在，永志不忘"); halls.save(h);
  return Result.ok(r); }
 @PutMapping("/{id}") public Result<Deceased> update(Authentication a,@PathVariable Long id,@RequestBody Deceased r){
  Deceased d=get(id).getData();
  if(CurrentUser.isFamily(a)&&!CurrentUser.id(a).equals(d.getFamilyId())) throw new BizException("无权修改他人档案");
  if(r.getName()!=null)d.setName(r.getName()); if(r.getBiography()!=null)d.setBiography(r.getBiography());
  if(r.getRelation()!=null)d.setRelation(r.getRelation()); if(r.getBirthDate()!=null)d.setBirthDate(r.getBirthDate());
  if(r.getDeathDate()!=null)d.setDeathDate(r.getDeathDate()); if(r.getPhotoEmoji()!=null)d.setPhotoEmoji(r.getPhotoEmoji());
  if(d.getBirthDate()!=null&&d.getDeathDate()!=null) d.setAge(java.time.Period.between(d.getBirthDate(),d.getDeathDate()).getYears());
  return Result.ok(repo.save(d)); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){repo.deleteById(id); return Result.ok(); } }
