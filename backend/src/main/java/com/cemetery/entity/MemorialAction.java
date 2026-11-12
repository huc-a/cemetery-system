package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class MemorialAction{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long hallId; private Long userId; private String userName;
 private String actionType; @Column(length=1000) private String content;
 private LocalDateTime createTime=LocalDateTime.now(); }
