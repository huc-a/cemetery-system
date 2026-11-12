package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/stats") @RequiredArgsConstructor public class StatsController{
 private final GravePlotRepository plots; private final CemeteryAreaRepository areas; private final PurchaseContractRepository contracts;
 private final PaymentRecordRepository payments; private final VisitAppointmentRepository visits; private final ServiceOrderRepository serviceOrders;
 private final WorkOrderRepository workOrders; private final MemorialHallRepository halls; private final DeceasedRepository deceasedRepo; private final SysUserRepository users;
 @GetMapping("/dashboard") public Result<Map<String,Object>> dashboard(Authentication a){
  if(!CurrentUser.isStaffSide(a)) throw new BizException("仅管理端可查看统计");
  Map<String,Object> m=new LinkedHashMap<>();
  m.put("totalPlots",plots.count()); m.put("freePlots",plots.countByStatus("FREE")); m.put("reservedPlots",plots.countByStatus("RESERVED"));
  m.put("soldPlots",plots.countByStatus("SOLD")); m.put("buriedPlots",plots.countByStatus("BURIED")); m.put("maintenancePlots",plots.countByStatus("MAINTENANCE"));
  m.put("totalContracts",contracts.count()); m.put("totalDeceased",deceasedRepo.count()); m.put("totalFamilies",users.findByRole("FAMILY").size());
  m.put("totalRevenue",payments.findAll().stream().map(PaymentRecord::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add));
  m.put("paymentCount",payments.count()); m.put("memorialCount",halls.count());
  m.put("totalFlowers",halls.findAll().stream().mapToInt(h->h.getFlowerCount()==null?0:h.getFlowerCount()).sum());
  m.put("pendingWorkOrders",workOrders.findByStatus("PENDING").size()+workOrders.findByStatus("ASSIGNED").size()+workOrders.findByStatus("PROCESSING").size());
  m.put("todayVisits",visits.findByVisitDate(LocalDate.now()).stream().filter(v->!"CANCELLED".equals(v.getStatus())).mapToInt(v->v.getVisitors()==null?1:v.getVisitors()).sum());
  // 墓位状态分布
  List<Map<String,Object>> dist=new ArrayList<>();
  for(String s:List.of("FREE","RESERVED","SOLD","BURIED","MAINTENANCE")){ Map<String,Object> d=new LinkedHashMap<>(); d.put("name",Map.of("FREE","空闲","RESERVED","已预订","SOLD","已售","BURIED","已安葬","MAINTENANCE","维护中").get(s)); d.put("value",plots.countByStatus(s)); dist.add(d); }
  m.put("plotStatusDist",dist);
  // 近6个月收入（按缴费记录月份汇总，与财务流水对账）
  List<Map<String,Object>> monthly=new ArrayList<>();
  List<PaymentRecord> allPay=payments.findAll();
  for(int i=5;i>=0;i--){ YearMonth ym=YearMonth.now().minusMonths(i);
   BigDecimal sum=allPay.stream().filter(p->p.getCreateTime()!=null&&YearMonth.from(p.getCreateTime()).equals(ym)).map(PaymentRecord::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
   Map<String,Object> d=new LinkedHashMap<>(); d.put("month",ym.toString()); d.put("amount",sum); monthly.add(d); }
  m.put("monthlyRevenue",monthly);
  // 收入构成（按业务类型）
  List<Map<String,Object>> comp=new ArrayList<>();
  Map<String,String> names=Map.of("CONTRACT","购墓合同","MANAGE_FEE","管理费","SERVICE","增值服务");
  allPay.stream().collect(Collectors.groupingBy(PaymentRecord::getBizType,Collectors.reducing(BigDecimal.ZERO,PaymentRecord::getAmount,BigDecimal::add)))
   .forEach((k,v)->{Map<String,Object> d=new LinkedHashMap<>(); d.put("name",names.getOrDefault(k,k)); d.put("value",v); comp.add(d);});
  m.put("revenueComposition",comp);
  // 近7天祭扫预约趋势（人次）
  List<Map<String,Object>> trend=new ArrayList<>();
  for(int i=6;i>=0;i--){ LocalDate d=LocalDate.now().minusDays(i);
   int cnt=visits.findByVisitDate(d).stream().filter(v->!"CANCELLED".equals(v.getStatus())).mapToInt(v->v.getVisitors()==null?1:v.getVisitors()).sum();
   Map<String,Object> dd=new LinkedHashMap<>(); dd.put("date",d.toString()); dd.put("visitors",cnt); trend.add(dd); }
  m.put("visitTrend",trend);
  // 各墓区售出率
  List<Map<String,Object>> areaStats=areas.findAll().stream().map(ar->{ List<GravePlot> ps=plots.findByAreaId(ar.getId());
   Map<String,Object> d=new LinkedHashMap<>(); d.put("area",ar.getName()); d.put("total",ps.size());
   d.put("sold",ps.stream().filter(p->"SOLD".equals(p.getStatus())||"BURIED".equals(p.getStatus())).count()); return d;}).collect(Collectors.toList());
  m.put("areaStats",areaStats);
  m.put("recentContracts",contracts.findAll(Sort.by(Sort.Direction.DESC,"id")).stream().limit(5).collect(Collectors.toList()));
  m.put("recentPayments",allPay.stream().sorted(Comparator.comparing(PaymentRecord::getId).reversed()).limit(5).collect(Collectors.toList()));
  return Result.ok(m); } }
