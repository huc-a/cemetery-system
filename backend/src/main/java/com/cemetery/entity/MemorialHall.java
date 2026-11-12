package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class MemorialHall{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long deceasedId; @Column(nullable=false,length=60) private String deceasedName;
 private Long plotId; private String plotNo; private Long familyId; private String familyName;
 @Column(length=1000) private String epitaph; @Column(length=2000) private String biography;
 private String photoEmoji; private Integer flowerCount=0; private Integer candleCount=0;
 private Integer messageCount=0; private Integer visitCount=0;
 private LocalDateTime createTime=LocalDateTime.now(); }
