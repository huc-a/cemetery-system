package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class Notice{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=120) private String title;
 @Column(length=3000) private String content; private String type="NOTICE";
 private Long publisherId; private String publisherName; private Integer pinned=0;
 private LocalDateTime createTime=LocalDateTime.now(); }
