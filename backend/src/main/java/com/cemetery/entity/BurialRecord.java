package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class BurialRecord{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long plotId; private String plotNo; private Long deceasedId; private String deceasedName;
 private Long familyId; private String familyName;
 private LocalDate burialDate; private String timeSlot; private String ceremonyType;
 private String status="BOOKED"; private Long staffId; private String staffName;
 @Column(length=500) private String remark; private LocalDateTime createTime=LocalDateTime.now(); }
