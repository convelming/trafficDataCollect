package com.convelming.roadflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.service.OSMWayService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/osm/way")
public class OSMWayController {

    @Resource
    private OSMWayService service;

    @PostMapping("/getGeomjson")
    public Result getGeomjson(@RequestBody QeuryParam param) {
        double[][] xyarr = null;
        if(!param.selectAll){
            xyarr = new double[param.getXyarr().length + 1][2];
            for (int i = 0; i < param.getXyarr().length; i++) {
                xyarr[i] = Arrays.copyOf(param.getXyarr()[i], 2);
            }
            xyarr[param.getXyarr().length] = param.getXyarr()[0];
        }
        return Result.ok(service.getGeomjson(xyarr, param.getSelectAll()));
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class QeuryParam {

        private double[][] xyarr;
        private Boolean selectAll = false;

    }

}
