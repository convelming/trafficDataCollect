package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.service.CossroadsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 十字路
 */
@Slf4j
@RestController
@RequestMapping("/crossroads")
public class CrossroadsController {

    @Resource
    private CossroadsService service;


    /**
     * 新增十字路数据
     *
     * @param bo 参数
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody CossroadsBo bo) {
        if (bo.vertex.length <= 2) {
            Result.fail("至少需要三个顶点");
        }
        // 把最后一个点设置为第一个点，连成一个封闭图行
        double[][] vertex = Arrays.copyOf(bo.vertex, bo.vertex.length + 1);
        vertex[vertex.length - 1] = bo.vertex[0];
        bo.vertex = vertex;

        return Result.failOrOk(service.insert(bo, vertex));
    }

    /**
     * 生成并获取视频封面信息
     *
     * @param cossroadsId 十字路id
     * @return 封面信息
     */
    @GetMapping("/frame/{cossroadsId}")
    public Result frame(@PathVariable Long cossroadsId) {
        return Result.ok(service.frame(cossroadsId));
    }


    /**
     * 保存绘制线
     *
     * @param bo 线数据
     */
    @PostMapping("/saveline")
    public Result saveline(@RequestBody List<LineBo> bo) {
        return Result.failOrOk(service.saveline(bo));
    }

    /**
     * 十字路流量表
     *
     * @param cossroadsId 十字路id
     */
    @GetMapping("/corssStatsTable/{cossroadsId}")
    public Result statstable(@PathVariable Long cossroadsId) {
        return Result.ok(service.corssStatsTable(cossroadsId));
    }

    /**
     * 删除十字路流量（删除行）
     *
     * @param crossroadStatsId 十字路流量id
     */
    @DeleteMapping("/deleteStats/{crossroadStatsId}")
    public Result deleteStats(@PathVariable Long crossroadStatsId) {
        return Result.failOrOk(service.deleteStats(crossroadStatsId));
    }

    /**
     * 新增十字路流量（新增行）
     *
     * @param stats 十字路流量
     */
    @PostMapping("/insertStats")
    public Result insertStats(@RequestBody CrossroadsStats stats) {
        return Result.failOrOk(service.insertStats(stats));
    }

    /**
     * 修改十字路流量（修改行）
     *
     * @param stats 十字路流量
     */
    @PostMapping("/updateStats")
    public Result updateStats(@RequestBody CrossroadsStats stats) {
        return Result.failOrOk(service.updateStats(stats));
    }

    /**
     * 获取全部十字路inoutlink
     *
     * @param cossroadsId 十字路id
     */
    @GetMapping("/inoutlink/{cossroadsId}")
    public Result inoutlink(@PathVariable Long cossroadsId) {
        return Result.ok(service.inoutlink(cossroadsId));
    }

    /**
     * 十字路流量表
     *
     * @param cossroadsId 十字路id
     */
    @GetMapping("/carLegTable/{cossroadsId}")
    public Result carLegTable(@PathVariable String cossroadsId) {
        return Result.ok(cossroadsId);
    }


    @Data
    public static class CossroadsBo {

        /**
         * 多边形点
         */
        double[][] vertex;

        /**
         * 视频地址
         */
        String video;

        /**
         * 拍摄角度
         */
        String type;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date beginTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date endTime;

        /**
         * 备注
         */
        String remark;

    }

    @Data
    public static class LineBo {
        Long cossroadsId;
        String lineName;
        String imageName;
        Integer beginx;
        Integer beginy;
        Integer endx;
        Integer endy;
        Integer width;
        Integer height;
    }

}
