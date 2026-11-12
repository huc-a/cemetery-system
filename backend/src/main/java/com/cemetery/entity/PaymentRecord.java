package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class PaymentRecord{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,length=34) private String payNo;
 private String bizType; private Long bizId; private String bizNo;
 private Long payerId; private String payerName; private BigDecimal amount=BigDecimal.ZERO;
 private String method="ONLINE"; private String status="PAID";
 @Column(length=300) private String remark; private LocalDateTime createTime=LocalDateTime.now(); }
