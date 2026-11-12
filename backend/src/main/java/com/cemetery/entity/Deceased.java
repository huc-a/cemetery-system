package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class Deceased{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,length=60) private String name;
 private String gender; private LocalDate birthDate; private LocalDate deathDate;
 private Integer age; private Long plotId; private String plotNo;
 private Long familyId; private String familyName; private String relation;
 @Column(length=1000) private String biography; private String photoEmoji;
 private LocalDate burialDate; private LocalDateTime createTime=LocalDateTime.now(); }
