package com.cemetery.config;
import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component; import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal; import java.time.*; import java.util.*;
@Slf4j @Component @RequiredArgsConstructor public class DataInitializer implements CommandLineRunner{
 private final SysUserRepository users; private final PasswordEncoder encoder;
 private final CemeteryAreaRepository areas; private final GravePlotRepository plots;
 private final DeceasedRepository deceasedRepo; private final PurchaseContractRepository contracts;
 private final ManagementFeeRepository fees; private final BurialRecordRepository burials;
 private final VisitAppointmentRepository visits; private final MemorialHallRepository halls;
 private final MemorialActionRepository memorialActions; private final ServiceProductRepository products;
 private final ServiceOrderRepository serviceOrders; private final WorkOrderRepository workOrders;
 private final PaymentRecordRepository payments; private final NoticeRepository notices;
 private SysUser user(String un,String pw,String name,String role,String phone,String emoji){
  SysUser u=new SysUser(); u.setUsername(un); u.setPassword(encoder.encode(pw)); u.setRealName(name); u.setRole(role);
  u.setPhone(phone); u.setAvatarEmoji(emoji); u.setStatus(1); return users.save(u); }
 private PaymentRecord pay(String type,Long bizId,String bizNo,Long payerId,String payerName,String amount,String remark,LocalDateTime t){
  PaymentRecord p=new PaymentRecord(); p.setPayNo("PAY"+t.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))+String.format("%04d",(int)(Math.random()*9000+1000)));
  p.setBizType(type); p.setBizId(bizId); p.setBizNo(bizNo); p.setPayerId(payerId); p.setPayerName(payerName);
  p.setAmount(new BigDecimal(amount)); p.setMethod("ONLINE"); p.setStatus("PAID"); p.setRemark(remark); p.setCreateTime(t); return payments.save(p); }
 @Override @Transactional public void run(String... args){
  if(users.count()>0){ log.info("数据已初始化，跳过种子数据"); return; }
  log.info("开始初始化智慧墓园种子数据...");
  SysUser admin=user("admin","admin123","陈静园","ADMIN","13800000001","🪦");
  SysUser staff=user("staff","staff123","李守山","STAFF","13800000002","🌲");
  SysUser staff2=user("staff2","staff123","王青松","STAFF","13800000003","🧹");
  SysUser staff3=user("staff3","staff123","赵迎客","STAFF","13800000004","💐");
  SysUser family=user("family","family123","张念慈","FAMILY","13900000001","🕯️");
  String[][] famData={{"family2","刘思源","13900000002"},{"family3","周慕云","13900000003"},{"family4","吴怀瑾","13900000004"},
   {"family5","郑忆安","13900000005"},{"family6","孙望舒","13900000006"},{"family7","林归朴","13900000007"},{"family8","何清晏","13900000008"}};
  List<SysUser> fams=new ArrayList<>(); fams.add(family);
  for(String[] f:famData) fams.add(user(f[0],"family123",f[1],"FAMILY",f[2],"🕯️"));
  // ===== 墓区 =====
  Object[][] areaData={
   {"松鹤园","A","传统墓区","松柏环绕、庄严肃穆的传统立碑区，坐北朝南，风水上佳。","18800","🌲",1},
   {"梅香园","B","艺术墓区","艺术碑型与个性化定制区，梅香四溢，环境清幽。","26800","🌸",2},
   {"兰馨园","C","生态葬区","节地生态安葬区，花坛葬与草坪葬，回归自然。","9800","🌿",3},
   {"竹韵园","D","家族墓区","多穴家族墓区，竹影婆娑，可安葬多位家族成员。","45800","🎋",4},
   {"福泽园","E","壁葬长廊","室内壁葬与骨灰长廊，全天候祭扫不受天气影响。","6800","🏮",5}};
  List<CemeteryArea> areaList=new ArrayList<>();
  for(Object[] d:areaData){ CemeteryArea a=new CemeteryArea(); a.setName((String)d[0]); a.setCode((String)d[1]); a.setType((String)d[2]);
   a.setDescription((String)d[3]); a.setPriceFrom(new BigDecimal((String)d[4])); a.setCoverEmoji((String)d[5]); a.setSortOrder((Integer)d[6]); areaList.add(areas.save(a)); }
  // ===== 墓位（网格生成，共122个） =====
  int[][] grid={{5,6},{5,6},{4,6},{4,5},{3,6}};
  String[] types={"双穴立碑墓","艺术定制墓","花坛生态葬","四穴家族墓","壁葬格位"};
  String[] priceBase={"18800","26800","9800","45800","6800"};
  String[] feeBase={"260","360","150","680","120"};
  List<GravePlot> allPlots=new ArrayList<>();
  for(int ai=0;ai<5;ai++){ CemeteryArea a=areaList.get(ai);
   for(int r=1;r<=grid[ai][0];r++) for(int c=1;c<=grid[ai][1];c++){
    GravePlot p=new GravePlot(); p.setAreaId(a.getId()); p.setAreaName(a.getName());
    p.setPlotNo(a.getCode()+"-"+r+"排-"+c+"号"); p.setRowNo(r); p.setColNo(c); p.setPlotType(types[ai]);
    p.setAreaSize(new BigDecimal(ai==4?"0.60":"3.50"));
    p.setPrice(new BigDecimal(priceBase[ai]).add(BigDecimal.valueOf((long)(6-c)*200)));
    p.setManageFeeYearly(new BigDecimal(feeBase[ai])); p.setOrientation(ai==4?"室内长廊":"坐北朝南"); p.setStatus("FREE");
    allPlots.add(plots.save(p)); } }
  // 维护中样例
  for(int idx:new int[]{7,45,90}){ GravePlot p=allPlots.get(idx); p.setStatus("MAINTENANCE"); plots.save(p); }
  // ===== 逝者 + 合同 + 已售/已安葬墓位 =====
  Object[][] decData={
   {"张寿山","男","1938-04-12","2023-11-02","父亲","一生勤俭持家，曾为乡村教师四十余载，桃李满天下。"},
   {"王桂芳","女","1941-07-23","2024-02-18","祖母","慈祥和蔼，擅长苏绣，作品曾获市级工艺奖。"},
   {"刘建国","男","1952-01-30","2022-06-15","父亲","退伍军人，参加过边境自卫反击战，荣立三等功。"},
   {"周雅琴","女","1955-09-09","2023-03-27","母亲","中学音乐教师，合唱团指挥，培养了几代学生。"},
   {"吴文正","男","1945-12-01","2021-08-08","祖父","老中医，行医五十载，悬壶济世，口碑载道。"},
   {"郑秀英","女","1948-05-16","2024-10-11","祖母","纺织厂劳模，与丈夫相濡以沫六十载。"},
   {"孙明远","男","1960-03-03","2020-12-25","父亲","桥梁工程师，参与建设多座跨江大桥。"},
   {"林静姝","女","1963-11-19","2023-07-07","母亲","图书馆馆员，爱书如命，捐赠藏书三千册。"},
   {"何志强","男","1958-08-25","2022-04-04","父亲","消防员，火场救人十余次，荣获英模称号。"},
   {"张慧敏","女","1970-02-14","2024-05-20","母亲","小学教师，乡村支教二十年。"}};
  List<Deceased> decList=new ArrayList<>(); List<PurchaseContract> contractList=new ArrayList<>();
  int[] plotIdx={0,1,30,31,60,61,84,85,104,105};
  for(int i=0;i<decData.length;i++){ Object[] d=decData[i]; GravePlot p=allPlots.get(plotIdx[i]); SysUser fam=fams.get(i%fams.size());
   boolean buried=i<7;
   p.setStatus(buried?"BURIED":"SOLD"); p.setBuyerId(fam.getId()); p.setBuyerName(fam.getRealName()); p.setUpdateTime(LocalDateTime.now()); plots.save(p);
   Deceased dec=new Deceased(); dec.setName((String)d[0]); dec.setGender((String)d[1]);
   dec.setBirthDate(LocalDate.parse((String)d[2])); dec.setDeathDate(LocalDate.parse((String)d[3]));
   dec.setAge(java.time.Period.between(dec.getBirthDate(),dec.getDeathDate()).getYears());
   dec.setPlotId(p.getId()); dec.setPlotNo(p.getPlotNo()); dec.setFamilyId(fam.getId()); dec.setFamilyName(fam.getRealName());
   dec.setRelation((String)d[4]); dec.setBiography((String)d[5]); dec.setPhotoEmoji("🖼️");
   if(buried) dec.setBurialDate(dec.getDeathDate().plusDays(5));
   decList.add(deceasedRepo.save(dec));
   PurchaseContract ct=new PurchaseContract();
   ct.setContractNo("HT2025"+String.format("%04d",1001+i)); ct.setPlotId(p.getId()); ct.setPlotNo(p.getPlotNo());
   ct.setAreaId(p.getAreaId()); ct.setAreaName(p.getAreaName()); ct.setBuyerId(fam.getId()); ct.setBuyerName(fam.getRealName()); ct.setBuyerPhone(fam.getPhone());
   ct.setDeceasedName(dec.getName()); ct.setPlotPrice(p.getPrice());
   BigDecimal svc=p.getPrice().multiply(new BigDecimal("0.02")).setScale(2,java.math.RoundingMode.HALF_UP);
   ct.setServiceFee(svc); int yrs=20;
   ct.setTotalAmount(p.getPrice().add(svc).add(p.getManageFeeYearly().multiply(BigDecimal.valueOf(yrs))));
   ct.setPaidAmount(ct.getTotalAmount()); ct.setStatus("SIGNED"); ct.setPayStatus("PAID"); ct.setManageYears(yrs);
   ct.setSignDate(dec.getDeathDate().plusDays(2)); ct.setManageExpireDate(ct.getSignDate().plusYears(yrs));
   contractList.add(contracts.save(ct));
   pay("CONTRACT",ct.getId(),ct.getContractNo(),fam.getId(),fam.getRealName(),ct.getTotalAmount().toString(),"购墓合同全款",ct.getSignDate().atTime(10,30));
   // 首期管理费（已缴）
   ManagementFee f0=new ManagementFee(); f0.setPlotId(p.getId()); f0.setPlotNo(p.getPlotNo()); f0.setContractId(ct.getId());
   f0.setFamilyId(fam.getId()); f0.setFamilyName(fam.getRealName()); f0.setYearlyAmount(p.getManageFeeYearly()); f0.setYears(yrs);
   f0.setTotalAmount(p.getManageFeeYearly().multiply(BigDecimal.valueOf(yrs))); f0.setPeriodStart(ct.getSignDate());
   f0.setPeriodEnd(ct.getManageExpireDate()); f0.setDueDate(ct.getManageExpireDate()); f0.setStatus("PAID"); f0.setPayTime(ct.getSignDate().atTime(10,30));
   f0.setRemark("购墓时一次性缴纳"); fees.save(f0);
   if(buried){ BurialRecord b=new BurialRecord(); b.setPlotId(p.getId()); b.setPlotNo(p.getPlotNo()); b.setDeceasedId(dec.getId()); b.setDeceasedName(dec.getName());
    b.setFamilyId(fam.getId()); b.setFamilyName(fam.getRealName()); b.setBurialDate(dec.getBurialDate()); b.setTimeSlot("10:00-12:00");
    b.setCeremonyType(i%2==0?"传统告别仪式":"简约安葬仪式"); b.setStatus("COMPLETED"); b.setStaffId(staff.getId()); b.setStaffName(staff.getRealName());
    b.setRemark("安葬礼成，家属满意"); burials.save(b); }
   // 纪念馆
   MemorialHall h=new MemorialHall(); h.setDeceasedId(dec.getId()); h.setDeceasedName(dec.getName()); h.setPlotId(p.getId()); h.setPlotNo(p.getPlotNo());
   h.setFamilyId(fam.getId()); h.setFamilyName(fam.getRealName()); h.setEpitaph("音容宛在，永志不忘"); h.setBiography(dec.getBiography());
   h.setPhotoEmoji("🖼️"); h.setFlowerCount(30+i*17); h.setCandleCount(20+i*11); h.setMessageCount(3+i); h.setVisitCount(120+i*43); halls.save(h);
   String[] msgs={"爸，清明又到了，我们都很好，您放心。","妈，家里的小院花又开了，想您。","爷爷，我考上研究生了，来看您了。","永远怀念，愿天堂没有病痛。"};
   for(int mi=0;mi<2;mi++){ MemorialAction ma=new MemorialAction(); ma.setHallId(h.getId()); ma.setUserId(fam.getId()); ma.setUserName(fam.getRealName());
    ma.setActionType("MESSAGE"); ma.setContent(msgs[(i+mi)%msgs.length]); ma.setCreateTime(LocalDateTime.now().minusDays(10L*i+mi*3)); memorialActions.save(ma); } }
  // 在签/待签合同 2 份（预订中墓位）
  for(int i=0;i<2;i++){ GravePlot p=allPlots.get(i==0?12:48); SysUser fam=fams.get((i+3)%fams.size());
   p.setStatus("RESERVED"); p.setBuyerId(fam.getId()); p.setBuyerName(fam.getRealName()); plots.save(p);
   PurchaseContract ct=new PurchaseContract(); ct.setContractNo("HT2026"+String.format("%04d",2001+i));
   ct.setPlotId(p.getId()); ct.setPlotNo(p.getPlotNo()); ct.setAreaId(p.getAreaId()); ct.setAreaName(p.getAreaName());
   ct.setBuyerId(fam.getId()); ct.setBuyerName(fam.getRealName()); ct.setBuyerPhone(fam.getPhone()); ct.setPlotPrice(p.getPrice());
   BigDecimal svc=p.getPrice().multiply(new BigDecimal("0.02")).setScale(2,java.math.RoundingMode.HALF_UP); ct.setServiceFee(svc);
   ct.setTotalAmount(p.getPrice().add(svc).add(p.getManageFeeYearly().multiply(BigDecimal.valueOf(20))));
   ct.setPaidAmount(BigDecimal.ZERO); ct.setStatus("PENDING_SIGN"); ct.setPayStatus("UNPAID"); ct.setManageYears(20);
   ct.setManageExpireDate(LocalDate.now().plusYears(20)); contracts.save(ct); }
  // ===== 管理费提醒：逾期 2 笔 + 临期 2 笔（独立短周期费用单） =====
  Object[][] feeSeed={{2,-45,1},{5,-12,1},{3,10,2},{6,25,1}};
  int fi=0;
  for(Object[] fs:feeSeed){ PurchaseContract ct=contractList.get((Integer)fs[0]); int offset=(Integer)fs[1]; int yrs=(Integer)fs[2];
   ManagementFee f=new ManagementFee(); f.setPlotId(ct.getPlotId()); f.setPlotNo(ct.getPlotNo()); f.setContractId(ct.getId());
   f.setFamilyId(ct.getBuyerId()); f.setFamilyName(ct.getBuyerName());
   GravePlot gp=plots.findById(ct.getPlotId()).orElse(null); BigDecimal yearly=gp!=null?gp.getManageFeeYearly():new BigDecimal("260");
   f.setYearlyAmount(yearly); f.setYears(yrs); f.setTotalAmount(yearly.multiply(BigDecimal.valueOf(yrs)));
   f.setPeriodEnd(LocalDate.now().plusDays(offset)); f.setPeriodStart(f.getPeriodEnd().minusYears(yrs)); f.setDueDate(f.getPeriodEnd());
   f.setStatus("UNPAID"); f.setRemark(offset<0?"管理费已逾期，请及时续缴":"管理费即将到期，请提前续缴"); fees.save(f); fi++; }
  // ===== 祭扫预约 =====
  Object[][] visitSeed={{0,0,"08:00-10:00",4},{1,0,"10:00-12:00",3},{2,1,"08:00-10:00",5},{3,2,"13:00-15:00",2},{4,0,"13:00-15:00",6},{5,3,"10:00-12:00",3}};
  for(Object[] vs:visitSeed){ Deceased dec=decList.get((Integer)vs[0]); SysUser fam=users.findById(dec.getFamilyId()).orElse(family);
   VisitAppointment v=new VisitAppointment(); v.setFamilyId(fam.getId()); v.setFamilyName(fam.getRealName()); v.setPhone(fam.getPhone());
   v.setPlotId(dec.getPlotId()); v.setPlotNo(dec.getPlotNo()); v.setDeceasedName(dec.getName());
   v.setVisitDate(LocalDate.now().plusDays((Integer)vs[1])); v.setTimeSlot((String)vs[2]); v.setVisitors((Integer)vs[3]); v.setStatus("BOOKED");
   v.setCreateTime(LocalDateTime.now().minusDays(1)); visits.save(v); }
  // ===== 服务项目 =====
  Object[][] svcData={
   {"标准立碑服务","碑刻服务","专业碑刻师傅立碑（含基础碑文排版与刻字）","3800","座","🪦"},
   {"碑文精刻加字","碑刻服务","碑文增刻、描金、瓷像镶嵌（按项计价）","680","项","✒️"},
   {"代客祭扫·基础包","代祭扫","擦拭墓碑、敬献鲜花一束、三鞠躬代祭，照片回传","198","次","💐"},
   {"代客祭扫·尊享包","代祭扫","擦碑+鲜花+供果+点烛上香+诵读祭文，全程照片与短视频回传","498","次","🕯️"},
   {"墓位绿化养护","绿化养护","墓位周边绿植修剪、补种、浇灌（年度）","1200","年","🌿"},
   {"墓碑清洗翻新","养护维修","墓碑深度清洗、描字翻新、破损修补评估","580","次","🧽"},
   {"安葬礼仪服务","礼仪服务","专业礼仪师主持安葬仪式，含司仪与礼生","2600","场","🎗️"},
   {"鲜花预订配送","祭扫用品","祭扫鲜花（菊花/百合混搭）预订并配送至墓位","168","束","🌼"},
   {"骨灰寄存格位年费","寄存服务","福泽园室内寄存格位一年使用与日常维护","800","年","🏮"},
   {"迁墓/改穴咨询服务","咨询服务","迁墓政策咨询、手续代办指导","300","次","📋"}};
  List<ServiceProduct> svcList=new ArrayList<>();
  for(Object[] s:svcData){ ServiceProduct sp=new ServiceProduct(); sp.setName((String)s[0]); sp.setCategory((String)s[1]); sp.setDescription((String)s[2]);
   sp.setPrice(new BigDecimal((String)s[3])); sp.setUnit((String)s[4]); sp.setCoverEmoji((String)s[5]); sp.setStatus(1); sp.setSales(5+svcList.size()*3); svcList.add(products.save(sp)); }
  // ===== 服务订单 =====
  String[] soStatus={"COMPLETED","COMPLETED","ASSIGNED","PENDING","PROCESSING","COMPLETED"};
  for(int i=0;i<6;i++){ ServiceProduct sp=svcList.get((i*2)%svcList.size()); Deceased dec=decList.get(i); SysUser fam=users.findById(dec.getFamilyId()).orElse(family);
   ServiceOrder o=new ServiceOrder(); o.setOrderNo("FW2026"+String.format("%04d",3001+i)); o.setProductId(sp.getId()); o.setProductName(sp.getName());
   o.setFamilyId(fam.getId()); o.setFamilyName(fam.getRealName()); o.setPlotId(dec.getPlotId()); o.setPlotNo(dec.getPlotNo());
   o.setQuantity(1); o.setTotalAmount(sp.getPrice()); o.setServiceDate(LocalDate.now().plusDays(i-2)); o.setTimeSlot("10:00-12:00");
   o.setStatus(soStatus[i]); o.setPayStatus("PAID");
   if(!"PENDING".equals(soStatus[i])){ o.setStaffId(staff3.getId()); o.setStaffName(staff3.getRealName()); }
   if("COMPLETED".equals(soStatus[i])) o.setResultNote("服务已完成，现场照片已回传家属，家属确认满意。");
   o.setCreateTime(LocalDateTime.now().minusDays(6-i)); o.setUpdateTime(LocalDateTime.now().minusDays(1)); serviceOrders.save(o);
   pay("SERVICE",o.getId(),o.getOrderNo(),fam.getId(),fam.getRealName(),sp.getPrice().toString(),"服务订单支付: "+sp.getName(),LocalDateTime.now().minusDays(6-i).plusHours(1)); }
  // ===== 巡查养护工单 =====
  Object[][] woData={
   {"松鹤园A区3排道路积水清理","环境巡查",15,"HIGH","COMPLETED","已清理积水并疏通排水沟，路面恢复通行。"},
   {"梅香园B区路灯故障维修","设施维修",36,"NORMAL","PROCESSING",null},
   {"兰馨园C区花坛杂草清理","绿化养护",66,"NORMAL","ASSIGNED",null},
   {"张寿山墓位碑面污渍处理","墓位养护",0,"NORMAL","PENDING",null},
   {"福泽园长廊消防通道巡查","安全巡查",104,"HIGH","COMPLETED","通道无占用，灭火器在有效期内，巡查合格。"},
   {"竹韵园D区围栏破损报修","设施维修",90,"NORMAL","PENDING",null},
   {"园区主干道落叶清扫","环境巡查",20,"LOW","COMPLETED","已完成清扫，落叶集中清运。"}};
  int wi=0;
  for(Object[] wd:woData){ GravePlot p=allPlots.get((Integer)wd[2]); WorkOrder o=new WorkOrder();
   o.setOrderNo("GD2026"+String.format("%04d",4001+wi)); o.setTitle((String)wd[0]); o.setType((String)wd[1]);
   o.setPlotId(p.getId()); o.setPlotNo(p.getPlotNo()); o.setAreaName(p.getAreaName()); o.setPriority((String)wd[3]);
   o.setStatus((String)wd[4]); o.setReporterId(admin.getId()); o.setReporterName(admin.getRealName());
   o.setDescription("巡查发现: "+wd[0]+"，请按规范处理并回传结果。");
   if(!"PENDING".equals(wd[4])){ o.setAssigneeId(staff2.getId()); o.setAssigneeName(staff2.getRealName()); }
   if("COMPLETED".equals(wd[4])){ o.setResultNote((String)wd[5]); o.setFinishTime(LocalDateTime.now().minusDays(1)); }
   o.setCreateTime(LocalDateTime.now().minusDays(3)); o.setUpdateTime(LocalDateTime.now().minusDays(1)); workOrders.save(o); wi++; }
  // ===== 公告 =====
  Object[][] noticeData={
   {"清明祭扫预约公告","清明期间园区实行分时预约祭扫，请家属提前在本系统预约时段，凭预约入园；提倡鲜花祭扫、云祭扫等文明低碳方式。","NOTICE",1},
   {"冬季防火安全提示","冬季天干物燥，园区内严禁焚烧纸钱、燃放鞭炮，请使用集中焚烧炉或鲜花、黄丝带等文明祭扫用品。","SAFETY",1},
   {"管理费续缴温馨提示","管理费临期或逾期的家属可在本系统在线续缴，也可到园区服务中心办理；逾期将按合同约定处理。","FEE",0},
   {"代祭扫服务上线","不便亲临的家属可下单代祭扫服务，工作人员将擦碑、献花、行礼并回传照片与短视频。","SERVICE",0},
   {"园区绿化升级通知","兰馨园生态葬区绿化升级施工期间部分区域围挡，由此带来不便敬请谅解。","NOTICE",0}};
  for(Object[] nd:noticeData){ Notice n=new Notice(); n.setTitle((String)nd[0]); n.setContent((String)nd[1]); n.setType((String)nd[2]);
   n.setPinned((Integer)nd[3]); n.setPublisherId(admin.getId()); n.setPublisherName(admin.getRealName()); n.setCreateTime(LocalDateTime.now().minusDays(2)); notices.save(n); }
  log.info("种子数据完成: 墓位{} 逝者{} 合同{}", allPlots.size(), decList.size(), contractList.size()+2);
 }
}
