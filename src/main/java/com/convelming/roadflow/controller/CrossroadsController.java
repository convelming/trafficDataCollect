package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.service.CossroadsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
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
     * 十字路列表
     * @param param 分页参数
     */
    @PostMapping("/list")
    public Result list(@RequestBody QueryParam param) {
        Page<Crossroads> page = new Page<>(param.getPageNum(), param.getPageSize());
        return Result.ok(service.list(page));
    }

    /**
     * 新增十字路数据
     *
     * @param bo 参数
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody CossroadsBo bo) {
//        if (bo.vertex.length <= 2) {
//            Result.fail("至少需要三个顶点");
//        }
//        // 把最后一个点设置为第一个点，连成一个封闭图行
//        double[][] vertex = Arrays.copyOf(bo.vertex, bo.vertex.length + 1);
//        vertex[vertex.length - 1] = bo.vertex[0];
//        bo.vertex = vertex;
        return Result.ok(service.insert(bo));
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
    public Result saveline(@RequestBody CossroadsLineBo bo) {
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
     * 下载分析视频
     *
     * @param cossroadsId 十字路id
     */
    @GetMapping("/analyzeVideo/{cossroadsId}")
    public void analyzeVideo(@PathVariable Long cossroadsId, HttpServletResponse response) {
        String video = Constant.DATA_PATH + "/data/" + cossroadsId + "/output_result/output_video.mp4";
        if (!new File(video).exists()) {
            throw new RuntimeException("未生成分析视频");
        }
        String fileName = "output_video.mp4";
//        response.addHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName));
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream is = new FileInputStream(video)
        ) {
            int len;
            byte[] b = new byte[1024 * 10];
            while ((len = is.read(b)) > 0) {
                os.write(b, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
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

    /**
     * 运行视频识别
     *
     * @param cossroadsId 十字路id
     */
    @GetMapping("/runVehicleCounts/{cossroadsId}")
    public Result runVehicleCounts(@PathVariable Long cossroadsId) {
        return Result.failOrOk(service.runVehicleCounts(cossroadsId));
    }

    @Data
    public static class CossroadsBo {

        /**
         * 视频地址
         */
        String video;

        /**
         * 录入类型（手动录入，视频录入）
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

        /**
         * 中心点
         */
        double[] center;

        /**
         * 拍摄类型（1俯视航拍，2侧面路拍，3正斜角拍摄
         */
        Integer videoType;

    }

    @Data
    public static class QueryParam {
        /**
         * 分页每页大小
         */
        private Integer pageSize = 10;
        /**
         * 分页第几页
         */
        private Integer pageNum = 1;
    }

    @Data
    public static class CossroadsLineBo {

        Long cossroadsId;

        /**
         * 多边形点
         */
        double[][] vertex;

        /**
         * 绘制线
         */
        List<LineBo> lines;
    }

    @Data
    public static class LineBo {
        String lineName;
        String imageName;
        int beginx;
        int beginy;
        int endx;
        int endy;
        int width;
        int height;

        double mktBeginx;
        double mktBeginy;
        double mktEndx;
        double mktEndy;
    }


}
