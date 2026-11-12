package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class VisitAppointment{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long familyId; private String familyName; private String phone;
 private Long plotId; private String plotNo; private String deceasedName;
 private LocalDate visitDate; private String timeSlot; private Integer visitors=1;
 private String status="BOOKED"; @Column(length=300) private String remark;
 private LocalDateTime createTime=LocalDateTime.now(); }
