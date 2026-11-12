package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class PurchaseContract{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,length=30) private String contractNo;
 private Long plotId; private String plotNo; private Long areaId; private String areaName;
 private Long buyerId; private String buyerName; private String buyerPhone;
 private String deceasedName; private BigDecimal plotPrice=BigDecimal.ZERO;
 private BigDecimal serviceFee=BigDecimal.ZERO; private BigDecimal totalAmount=BigDecimal.ZERO;
 private BigDecimal paidAmount=BigDecimal.ZERO;
 private String status="PENDING_SIGN"; private String payStatus="UNPAID";
 private Integer manageYears=20; private LocalDate manageExpireDate;
 private LocalDate signDate; @Column(length=500) private String remark;
 private LocalDateTime createTime=LocalDateTime.now(); private LocalDateTime updateTime=LocalDateTime.now(); }
