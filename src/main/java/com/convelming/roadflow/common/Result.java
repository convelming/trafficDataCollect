package com.convelming.roadflow.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    private Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    public static Result fialOrOk(boolean ok){
        return ok ? Result.ok() : Result.fail();
    }
    public static Result ok(){
        return new Result(HttpStatus.OK.value(), "OK", null);
    }

    public static Result ok(Object data) {
        return new Result(HttpStatus.OK.value(), "OK", data);
    }

    public static Result ok(String msg, Object data) {
        return new Result(HttpStatus.OK.value(), msg, data);
    }

    public static Result fail(Integer code, String msg, Object data){
        return new Result(code, msg, data);
    }

    public static Result fail(Integer code, String msg){
        return new Result(code, msg, null);
    }

    public static Result fail(String msg){
        return new Result(HttpStatus.INTERNAL_SERVER_ERROR.value(), msg, null);
    }

    public static Result fail(){
        return new Result(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error .", null);
    }

}
