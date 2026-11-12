package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class WorkOrder{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,length=30) private String orderNo;
 @Column(nullable=false,length=100) private String title;
 private String type; private Long plotId; private String plotNo; private String areaName;
 private String priority="NORMAL"; private String status="PENDING";
 private Long reporterId; private String reporterName; private Long assigneeId; private String assigneeName;
 @Column(length=800) private String description; @Column(length=800) private String resultNote;
 private LocalDateTime finishTime; private LocalDateTime createTime=LocalDateTime.now(); private LocalDateTime updateTime=LocalDateTime.now(); }
