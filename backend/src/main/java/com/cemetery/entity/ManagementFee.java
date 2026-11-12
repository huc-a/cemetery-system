package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class ManagementFee{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long plotId; private String plotNo; private Long contractId;
 private Long familyId; private String familyName;
 private BigDecimal yearlyAmount=BigDecimal.ZERO; private Integer years=1;
 private BigDecimal totalAmount=BigDecimal.ZERO;
 private LocalDate periodStart; private LocalDate periodEnd; private LocalDate dueDate;
 private String status="UNPAID"; private LocalDateTime payTime;
 @Column(length=300) private String remark; private LocalDateTime createTime=LocalDateTime.now(); }
