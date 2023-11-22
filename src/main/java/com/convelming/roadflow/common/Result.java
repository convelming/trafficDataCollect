package com.convelming.roadflow.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    private Result(HttpStatus code, String msg, Object data) {
        this.code = code.value();
        this.msg = msg;
        this.data = data;
    }

    public static Result fialOrOk(boolean ok){
        return ok ? Result.ok() : Result.fail();
    }
    public static Result ok(){
        return new Result(HttpStatus.OK, "OK", null);
    }

    public static Result ok(Object data) {
        return new Result(HttpStatus.OK, "OK", data);
    }

    public static Result ok(String msg, Object data) {
        return new Result(HttpStatus.OK, msg, data);
    }

    public static Result fail(HttpStatus code, String msg, Object data){
        return new Result(code, msg, data);
    }

    public static Result fail(String msg, Object data){
        return new Result(HttpStatus.INTERNAL_SERVER_ERROR, msg, data);
    }

    public static Result fail(Object data){
        return new Result(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error .", data);
    }

    public static Result fail(){
        return new Result(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error .", null);
    }

}
