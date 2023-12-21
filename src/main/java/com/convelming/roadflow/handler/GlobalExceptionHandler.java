package com.convelming.roadflow.handler;

import com.convelming.roadflow.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.geotools.ows.ServiceException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.FileNotFoundException;
import java.util.Objects;

/**
 * 全局异常处理器
 *
 * @author Lion Li
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({FileNotFoundException.class})
    public Result handleFileNotFoundException(FileNotFoundException e, HttpServletRequest request, HttpServletResponse response){
        return Result.fail(e.getMessage());
    }

    /**
     * 权限码异常
     */
//    @ExceptionHandler(NotPermissionException.class)
//    public R<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',权限码校验失败'{}'", requestURI, e.getMessage());
//        return R.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系管理员授权");
//    }

    /**
     * 角色权限异常
     */
//    @ExceptionHandler(NotRoleException.class)
//    public R<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',角色权限校验失败'{}'", requestURI, e.getMessage());
//        return R.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系管理员授权");
//    }

    /**
     * 认证失败
     */
//    @ExceptionHandler(NotLoginException.class)
//    public R<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        log.error("请求地址'{}',认证失败'{}',无法访问系统资源", requestURI, e.getMessage());
//        return R.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源");
//    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                       HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return Result.fail(e.getMessage());
    }

    /**
     * 主键或UNIQUE索引，数据重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',数据库中已存在记录'{}'", requestURI, e.getMessage());
        return Result.fail("数据库中已存在该记录，请联系管理员确认");
    }

    /**
     * Mybatis系统异常 通用处理
     */
//    @ExceptionHandler(MyBatisSystemException.class)
//    public R<Void> handleCannotFindDataSourceException(MyBatisSystemException e, HttpServletRequest request) {
//        String requestURI = request.getRequestURI();
//        String message = e.getMessage();
//        if (message.contains("CannotFindDataSourceException")) {
//            log.error("请求地址'{}', 未找到数据源", requestURI);
//            return R.fail("未找到数据源，请联系管理员确认");
//        }
//        log.error("请求地址'{}', Mybatis系统异常", requestURI, e);
//        return R.fail(message);
//    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public Result handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage());
        Integer code = Integer.valueOf(e.getCode());
        return !Objects.isNull(code) ? Result.fail(code, e.getMessage()) : Result.fail(e.getMessage());
    }

    /**
     * 业务异常
     */
//    @ExceptionHandler(RuntimeException.class)
//    public Result handleBaseException(RuntimeException e, HttpServletRequest request) {
//        log.error(e.getMessage());
//        return Result.fail(e.getMessage());
//    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public Result handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI);
        return Result.fail(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI);
        return Result.fail(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), e.getValue()));
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return Result.fail(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public Result EmptyResultDataAccessException(EmptyResultDataAccessException e){
        log.error("数据库查询为空", e);
        return Result.fail(200, "查询不到数据");
    }

    /**
     * 自定义验证异常
     */
//    @ExceptionHandler(BindException.class)
//    public R<Void> handleBindException(BindException e) {
//        log.error(e.getMessage());
//        String message = StreamUtils.join(e.getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
//        return R.fail(message);
//    }

    /**
     * 自定义验证异常
     */
//    @ExceptionHandler(ConstraintViolationException.class)
//    public R<Void> constraintViolationException(ConstraintViolationException e) {
//        log.error(e.getMessage());
//        String message = StreamUtils.join(e.getConstraintViolations(), ConstraintViolation::getMessage, ", ");
//        return R.fail(message);
//    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(message);
    }

}