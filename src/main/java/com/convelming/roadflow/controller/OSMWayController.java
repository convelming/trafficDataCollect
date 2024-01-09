package com.convelming.roadflow.controller;

import com.alibaba.fastjson.JSONObject;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.service.OSMWayService;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/osm/way")
public class OSMWayController {

    @Resource
    private OSMWayService service;

    /**
     * 查询全部路网geojson数据
     * @param param
     * @return
     */
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

    /**
     * 根据路名模糊查询
     * @param name 路名
     * @return
     */
    @GetMapping("/getWayByName")
    public Result getWayByName(String name){
        return Result.ok(service.getWayByName(name));
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class QeuryParam {

        private double[][] xyarr;
        private Boolean selectAll = false;

    }

}
