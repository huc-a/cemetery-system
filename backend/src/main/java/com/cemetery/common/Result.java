package com.cemetery.common;
import lombok.Data;
@Data public class Result<T>{ private int code; private String msg; private T data;
 public static <T> Result<T> ok(T d){Result<T> r=new Result<>();r.code=200;r.msg="操作成功";r.data=d;return r;}
 public static <T> Result<T> ok(){return ok(null);}
 public static <T> Result<T> fail(int c,String m){Result<T> r=new Result<>();r.code=c;r.msg=m;return r;}
 public static <T> Result<T> fail(String m){return fail(400,m);} }
