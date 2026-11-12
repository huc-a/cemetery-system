package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/memorials") @RequiredArgsConstructor public class MemorialController{
 private final MemorialHallRepository halls; private final MemorialActionRepository actions; private final SysUserRepository users;
 @GetMapping public Result<List<MemorialHall>> list(Authentication a,@RequestParam(required=false) String kw){
  List<MemorialHall> all=halls.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(h->CurrentUser.id(a).equals(h.getFamilyId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(h->kw==null||kw.isBlank()||h.getDeceasedName().contains(kw)).collect(Collectors.toList())); }
 @GetMapping("/{id}") public Result<Map<String,Object>> detail(@PathVariable Long id){
  MemorialHall h=halls.findById(id).orElseThrow(()->new BizException("纪念馆不存在"));
  h.setVisitCount(h.getVisitCount()+1); halls.save(h);
  Map<String,Object> m=new LinkedHashMap<>(); m.put("hall",h); m.put("actions",actions.findByHallIdOrderByCreateTimeDesc(id)); return Result.ok(m); }
 @PutMapping("/{id}") public Result<MemorialHall> update(Authentication a,@PathVariable Long id,@RequestBody MemorialHall r){
  MemorialHall h=halls.findById(id).orElseThrow(()->new BizException("纪念馆不存在"));
  if(CurrentUser.isFamily(a)&&!CurrentUser.id(a).equals(h.getFamilyId())) throw new BizException("无权编辑他人纪念馆");
  if(r.getEpitaph()!=null)h.setEpitaph(r.getEpitaph()); if(r.getBiography()!=null)h.setBiography(r.getBiography());
  if(r.getPhotoEmoji()!=null)h.setPhotoEmoji(r.getPhotoEmoji()); return Result.ok(halls.save(h)); }
 // 云祭扫动作：献花 FLOWER / 点烛 CANDLE / 留言 MESSAGE / 上香 INCENSE / 鞠躬 BOW —— 计数落库
 @Data public static class ActionReq{ private String actionType; private String content; }
 @PostMapping("/{id}/action") @Transactional public Result<MemorialHall> action(Authentication a,@PathVariable Long id,@RequestBody ActionReq req){
  MemorialHall h=halls.findById(id).orElseThrow(()->new BizException("纪念馆不存在"));
  String t=req.getActionType()==null?"FLOWER":req.getActionType().toUpperCase();
  if(!List.of("FLOWER","CANDLE","MESSAGE","INCENSE","BOW").contains(t)) throw new BizException("不支持的祭扫动作: "+t);
  if("MESSAGE".equals(t)&&(req.getContent()==null||req.getContent().isBlank())) throw new BizException("留言内容不能为空");
  MemorialAction act=new MemorialAction(); act.setHallId(id); act.setUserId(CurrentUser.id(a));
  SysUser u=users.findById(CurrentUser.id(a)).orElse(null); act.setUserName(u!=null?u.getRealName():a.getName());
  act.setActionType(t); act.setContent(req.getContent()); actions.save(act);
  if("FLOWER".equals(t)) h.setFlowerCount(h.getFlowerCount()+1);
  if("CANDLE".equals(t)) h.setCandleCount(h.getCandleCount()+1);
  if("MESSAGE".equals(t)) h.setMessageCount(h.getMessageCount()+1);
  halls.save(h); return Result.ok(h); }
 @GetMapping("/{id}/actions") public Result<List<MemorialAction>> acts(@PathVariable Long id){return Result.ok(actions.findByHallIdOrderByCreateTimeDesc(id));} }
