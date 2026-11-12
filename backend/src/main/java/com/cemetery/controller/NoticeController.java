package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/notices") @RequiredArgsConstructor public class NoticeController{
 private final NoticeRepository notices; private final SysUserRepository users;
 @GetMapping public Result<List<Notice>> list(){return Result.ok(notices.findAllByOrderByPinnedDescCreateTimeDesc());}
 @PostMapping @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<Notice> create(Authentication a,@RequestBody Notice r){
  r.setId(null); r.setPublisherId(CurrentUser.id(a)); SysUser u=users.findById(CurrentUser.id(a)).orElse(null);
  if(u!=null) r.setPublisherName(u.getRealName()); if(r.getPinned()==null) r.setPinned(0); r.setCreateTime(LocalDateTime.now()); return Result.ok(notices.save(r)); }
 @PutMapping("/{id}") @PreAuthorize("hasAnyRole('ADMIN','STAFF')") public Result<Notice> update(@PathVariable Long id,@RequestBody Notice r){
  Notice n=notices.findById(id).orElseThrow(()->new BizException("公告不存在")); r.setId(id); r.setCreateTime(n.getCreateTime()); r.setPublisherId(n.getPublisherId()); r.setPublisherName(n.getPublisherName()); return Result.ok(notices.save(r)); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){notices.deleteById(id); return Result.ok(); } }
