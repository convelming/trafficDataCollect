package com.convelming.roadflow.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private static final String UNKNOW = "unknown";

    public static String getIpAddr(HttpServletRequest request){
        return request.getRemoteAddr();
    }

}
