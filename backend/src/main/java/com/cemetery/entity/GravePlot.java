package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class GravePlot{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long areaId; private String areaName;
 @Column(nullable=false,length=40) private String plotNo;
 private Integer rowNo; private Integer colNo;
 private String plotType; private BigDecimal areaSize=BigDecimal.ZERO;
 private BigDecimal price=BigDecimal.ZERO; private BigDecimal manageFeeYearly=BigDecimal.ZERO;
 private String orientation; private String status="FREE";
 private Long buyerId; private String buyerName;
 private LocalDateTime createTime=LocalDateTime.now(); private LocalDateTime updateTime=LocalDateTime.now(); }
