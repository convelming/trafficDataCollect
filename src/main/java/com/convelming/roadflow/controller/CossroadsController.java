package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.Cossroads;
import com.convelming.roadflow.service.CossroadsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * 十字路
 */
@Slf4j
@RestController
@RequestMapping("/link/cossroads")
public class CossroadsController {

    @Resource
    private CossroadsService service;


    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/insert")
    public Result insert(@RequestBody CossroadsBo bo) {
        if (bo.vertex.length == 2) {
            Result.fail("至少需要三个顶点");
        }
        // 把最后一个点设置为第一个点，连成一个封闭图行
        double[][] vertex = Arrays.copyOf(bo.vertex, bo.vertex.length + 1);
        vertex[vertex.length - 1] = bo.vertex[0];
        bo.vertex = vertex;

        return Result.failOrOk(service.insert(bo, vertex));
    }


    @Data
    public static class CossroadsBo {

        double[][] vertex;

        String video;

        String type;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        Date beginTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        Date endTime;

        String remark;

    }


}
