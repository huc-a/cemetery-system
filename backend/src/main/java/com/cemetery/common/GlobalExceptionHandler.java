package com.cemetery.common;
import org.springframework.web.bind.annotation.*;
@RestControllerAdvice public class GlobalExceptionHandler{
 @ExceptionHandler(BizException.class) public Result<Void> biz(BizException e){return Result.fail(e.getMessage());}
 @ExceptionHandler(Exception.class) public Result<Void> ex(Exception e){e.printStackTrace();return Result.fail(500,"服务器异常: "+e.getMessage());} }
