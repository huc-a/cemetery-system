package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class ServiceProduct{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=80) private String name;
 private String category; @Column(length=600) private String description;
 private BigDecimal price=BigDecimal.ZERO; private String unit; private String coverEmoji;
 private Integer sales=0; private Integer status=1; private LocalDateTime createTime=LocalDateTime.now(); }
