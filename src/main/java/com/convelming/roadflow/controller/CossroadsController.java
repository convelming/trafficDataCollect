package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.service.CossroadsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    /**
     * 新增十字路数据
     * @param bo
     */
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

    /**
     * 生成并获取视频封面信息
     * @param id id
     * @return 封面信息
     */
    @GetMapping("/frame/{id}")
    public Result frame(@PathVariable Long id) {
        return Result.ok(service.frame(id));
    }

    /**
     * 画线后获取经过线的linkId
     * @return
     */
    @PostMapping("/getLineLink")
    public Result getLineLink(@RequestBody double[][] xyarr) {

        return null;
    }

    /**
     * 保存绘制线
     * @param bo 线数据
     */
    @PostMapping("/saveline")
    public Result saveline(@RequestBody List<LineBo> bo) {
        return Result.failOrOk(service.saveline(bo));
    }

    public Result select() {
        return null;
    }


    @Data
    public static class CossroadsBo {


        double[][] vertex;

        String video;

        String type;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date beginTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        Date endTime;

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
