package com.cemetery.common;
import org.springframework.security.core.Authentication;
public class CurrentUser{
 public static Long id(Authentication a){return a==null?null:(Long)a.getDetails();}
 public static String role(Authentication a){return a==null?"":a.getAuthorities().stream().findFirst().map(x->x.getAuthority().replace("ROLE_","")).orElse("");}
 public static boolean isAdmin(Authentication a){return "ADMIN".equals(role(a));}
 public static boolean isStaff(Authentication a){return "STAFF".equals(role(a));}
 public static boolean isFamily(Authentication a){return "FAMILY".equals(role(a));}
 public static boolean isStaffSide(Authentication a){return isAdmin(a)||isStaff(a);} }
