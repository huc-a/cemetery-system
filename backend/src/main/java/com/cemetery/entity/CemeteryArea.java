package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class CemeteryArea{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=60) private String name;
 private String code; private String type; private String description;
 private BigDecimal priceFrom=BigDecimal.ZERO; private String coverEmoji;
 private Integer sortOrder=0; private LocalDateTime createTime=LocalDateTime.now(); }
