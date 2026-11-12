package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class ServiceOrder{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,length=30) private String orderNo;
 private Long productId; private String productName; private Long familyId; private String familyName;
 private Long plotId; private String plotNo; private Integer quantity=1;
 private BigDecimal totalAmount=BigDecimal.ZERO; private LocalDate serviceDate; private String timeSlot;
 private String status="PENDING"; private String payStatus="UNPAID";
 private Long staffId; private String staffName; @Column(length=500) private String resultNote;
 @Column(length=300) private String remark; private LocalDateTime createTime=LocalDateTime.now(); private LocalDateTime updateTime=LocalDateTime.now(); }
