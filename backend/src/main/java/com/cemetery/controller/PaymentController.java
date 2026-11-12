package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;

@RestController @RequestMapping("/api/payments") @RequiredArgsConstructor public class PaymentController{
 private final PaymentRecordRepository payments;
 @GetMapping public Result<List<PaymentRecord>> list(Authentication a,@RequestParam(required=false) String bizType){
  List<PaymentRecord> all=payments.findAll(Sort.by(Sort.Direction.DESC,"id"));
  if(CurrentUser.isFamily(a)) all=all.stream().filter(p->CurrentUser.id(a).equals(p.getPayerId())).collect(Collectors.toList());
  return Result.ok(all.stream().filter(p->bizType==null||bizType.isBlank()||bizType.equals(p.getBizType())).collect(Collectors.toList())); }
 @GetMapping("/summary") public Result<Map<String,Object>> summary(Authentication a){
  List<PaymentRecord> all=list(a,null).getData();
  Map<String,Object> m=new LinkedHashMap<>();
  m.put("total",all.stream().map(PaymentRecord::getAmount).reduce(BigDecimal.ZERO,BigDecimal::add));
  m.put("count",all.size());
  m.put("byType",all.stream().collect(Collectors.groupingBy(PaymentRecord::getBizType,Collectors.reducing(BigDecimal.ZERO,PaymentRecord::getAmount,BigDecimal::add))));
  return Result.ok(m); } }
