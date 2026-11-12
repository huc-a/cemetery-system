package com.cemetery.controller;
import com.cemetery.common.*; import com.cemetery.entity.*; import com.cemetery.repository.*;
import lombok.*; import org.springframework.data.domain.*; import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional; import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.time.*; import java.util.*; import java.util.stream.*;
import org.springframework.security.crypto.password.PasswordEncoder; import org.springframework.security.access.prepost.PreAuthorize;
@RestController @RequestMapping("/api/users") @RequiredArgsConstructor public class UserController{
 private final SysUserRepository users; private final PasswordEncoder encoder;
 @GetMapping @PreAuthorize("hasRole('ADMIN')") public Result<List<SysUser>> list(@RequestParam(required=false) String role,@RequestParam(required=false) String kw){
  List<SysUser> all=users.findAll(Sort.by(Sort.Direction.DESC,"id"));
  return Result.ok(all.stream().filter(u->role==null||role.isBlank()||role.equals(u.getRole()))
   .filter(u->kw==null||kw.isBlank()||(u.getRealName()!=null&&u.getRealName().contains(kw))||u.getUsername().contains(kw)).collect(Collectors.toList())); }
 @PostMapping @PreAuthorize("hasRole('ADMIN')") public Result<SysUser> create(@RequestBody SysUser req){
  if(users.existsByUsername(req.getUsername())) throw new BizException("用户名已存在");
  req.setId(null); req.setPassword(encoder.encode(req.getPassword()==null||req.getPassword().isBlank()?"123456":req.getPassword()));
  if(req.getStatus()==null)req.setStatus(1); req.setCreateTime(LocalDateTime.now()); return Result.ok(users.save(req)); }
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<SysUser> update(@PathVariable Long id,@RequestBody SysUser req){
  SysUser u=users.findById(id).orElseThrow(()->new BizException("用户不存在"));
  if(req.getRealName()!=null)u.setRealName(req.getRealName()); if(req.getPhone()!=null)u.setPhone(req.getPhone());
  if(req.getEmail()!=null)u.setEmail(req.getEmail()); if(req.getRole()!=null)u.setRole(req.getRole());
  if(req.getStatus()!=null)u.setStatus(req.getStatus()); if(req.getAddress()!=null)u.setAddress(req.getAddress());
  return Result.ok(users.save(u)); }
 @PutMapping("/{id}/toggle") @PreAuthorize("hasRole('ADMIN')") public Result<SysUser> toggle(@PathVariable Long id){
  SysUser u=users.findById(id).orElseThrow(()->new BizException("用户不存在"));
  u.setStatus(u.getStatus()!=null&&u.getStatus()==1?0:1); return Result.ok(users.save(u)); }
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public Result<Void> del(@PathVariable Long id){users.deleteById(id); return Result.ok(); }
 @GetMapping("/families") public Result<List<SysUser>> families(Authentication a){
  if(!CurrentUser.isStaffSide(a)) throw new BizException("无权访问");
  return Result.ok(users.findByRole("FAMILY")); }
 @GetMapping("/staff") public Result<List<SysUser>> staff(Authentication a){
  if(!CurrentUser.isStaffSide(a)) throw new BizException("无权访问");
  List<SysUser> r=new ArrayList<>(users.findByRole("STAFF")); r.addAll(users.findByRole("ADMIN")); return Result.ok(r); } }
