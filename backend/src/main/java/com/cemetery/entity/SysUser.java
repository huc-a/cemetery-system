package com.cemetery.entity;
import com.fasterxml.jackson.annotation.*; import jakarta.persistence.*; import lombok.*;
import java.math.BigDecimal; import java.time.*;
@Data @Entity public class SysUser{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=50) private String username;
 @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) @Column(nullable=false) private String password;
 private String realName; @Column(nullable=false,length=20) private String role;
 private String phone; private String email; private String address; private String avatarEmoji;
 private Integer status=1; private LocalDateTime createTime=LocalDateTime.now(); }
