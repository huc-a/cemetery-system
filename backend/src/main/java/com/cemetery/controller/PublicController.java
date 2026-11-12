package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/public") @RequiredArgsConstructor public class PublicController{
 private final CemeteryAreaRepository areas; private final GravePlotRepository plots; private final ServiceProductRepository products;
 private final MemorialHallRepository halls; private final NoticeRepository notices; private final DeceasedRepository deceasedRepo;
 @GetMapping("/overview") public Result<Map<String,Object>> overview(){
  Map<String,Object> m=new LinkedHashMap<>();
  m.put("areas",areas.findAll(Sort.by("sortOrder"))); m.put("totalPlots",plots.count());
  m.put("freePlots",plots.countByStatus("FREE")); m.put("buriedCount",plots.countByStatus("BURIED"));
  m.put("memorialCount",halls.count()); m.put("notices",notices.findAllByOrderByPinnedDescCreateTimeDesc().stream().limit(5).collect(Collectors.toList()));
  m.put("hotServices",products.findAll(Sort.by(Sort.Direction.DESC,"sales")).stream().limit(6).collect(Collectors.toList()));
  return Result.ok(m); }
 @GetMapping("/plots") public Result<List<GravePlot>> plots(@RequestParam(required=false) Long areaId){
  List<GravePlot> all=areaId!=null?plots.findByAreaId(areaId):plots.findAll(Sort.by("areaId","rowNo","colNo"));
  // 游客端脱敏：不返回购买人信息
  all.forEach(p->{p.setBuyerId(null); p.setBuyerName(null);}); return Result.ok(all); }
 @GetMapping("/services") public Result<List<ServiceProduct>> services(){return Result.ok(products.findByStatus(1));}
 @GetMapping("/memorials") public Result<List<MemorialHall>> memorials(@RequestParam(required=false) String kw){
  return Result.ok(halls.findAll(Sort.by(Sort.Direction.DESC,"flowerCount")).stream()
   .filter(h->kw==null||kw.isBlank()||h.getDeceasedName().contains(kw)).limit(60).collect(Collectors.toList())); }
 @GetMapping("/notices") public Result<List<Notice>> notices(){return Result.ok(notices.findAllByOrderByPinnedDescCreateTimeDesc());} }
