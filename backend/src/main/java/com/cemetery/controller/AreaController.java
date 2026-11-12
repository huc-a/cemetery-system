package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/areas") @RequiredArgsConstructor public class AreaController{
 private final CemeteryAreaRepository areas; private final GravePlotRepository plots;
 @GetMapping public Result<List<Map<String,Object>>> list(){
  return Result.ok(areas.findAll(Sort.by("sortOrder")).stream().map(a->{Map<String,Object> m=new LinkedHashMap<>();
   m.put("area",a); long total=plots.countByAreaId(a.getId());
   m.put("total",total); m.put("free",plots.findByAreaId(a.getId()).stream().filter(p->"FREE".equals(p.getStatus())).count());
   m.put("sold",plots.findByAreaId(a.getId()).stream().filter(p->"SOLD".equals(p.getStatus())||"BURIED".equals(p.getStatus())).count());
   return m;}).collect(Collectors.toList())); }
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<CemeteryArea> create(@RequestBody CemeteryArea r){r.setId(null); r.setCreateTime(LocalDateTime.now()); return Result.ok(areas.save(r));}
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<CemeteryArea> update(@PathVariable Long id,@RequestBody CemeteryArea r){
  CemeteryArea a=areas.findById(id).orElseThrow(()->new BizException("墓区不存在")); r.setId(id); r.setCreateTime(a.getCreateTime()); return Result.ok(areas.save(r)); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){
  if(plots.countByAreaId(id)>0) throw new BizException("该墓区下已有墓位，不能删除"); areas.deleteById(id); return Result.ok(); } }
