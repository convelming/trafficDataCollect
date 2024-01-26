package com.convelming.roadflow.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.service.LinkStatsService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


/**
 * 流量调查数据
 */
@RestController
@RequestMapping("/link/stats")
public class LinkStatsController {

    @Resource
    private LinkStatsService linkStatsService;

    /**
     * 新增流量调查数据
     * @return
     */
    @PostMapping("/insert")
    public Result insert(@RequestBody LinkStats stats, HttpServletRequest request) {
        stats.setIpAddr(request.getRemoteAddr());
        return Result.fialOrOk(linkStatsService.insert(stats));
    }

    /**
     * 修改流量调查数据
     * @param stats
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody LinkStats stats, HttpServletRequest request) {
        stats.setIpAddr(request.getRemoteAddr());
        return Result.fialOrOk(linkStatsService.update(stats));
    }

    /**
     * 查询详情
     * @param id 主键
     * @return
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.ok(linkStatsService.queryById(id));
    }

    /**
     * 删除
     * @param id 主键
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.ok(linkStatsService.delete(id));
    }

    /**
     * 查询全部路段流量
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @param type 类型
     * @return
     */
    @GetMapping("/queryAllMaker")
    public Result queryAllMaker(String beginTime,
                                String endTime,
                                String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date begin = null, end = null;
        try {
            if (beginTime != null && !"".equals(beginTime)) begin = sdf.parse(beginTime);
            if (endTime != null && !"".equals(endTime)) end = sdf.parse(endTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.ok(linkStatsService.queryAllMaker(begin, end, type));
    }

    /**
     * 查询区域内路段流量
     * @param param param.xyarr
     * @return
     */
    @PostMapping("/queryByArea")
    public Result queryByArea(@RequestBody QueryParam param) {
        double[][] xyarr = new double[param.getXyarr().length + 1][2];
        for (int i = 0; i < param.getXyarr().length; i++) {
            xyarr[i] = Arrays.copyOf(param.getXyarr()[i], 2);
        }
        xyarr[param.getXyarr().length] = param.getXyarr()[0];
        Page<LinkStats> page = new Page<>(param.pageNum, param.pageSize);
        page.param(
                new Object[]{"linkId", param.getLinkId()},
                new Object[]{"watId", param.getWayId()},
                new Object[]{"type", param.getType()},
                new Object[]{"beginTime", param.getBeginTime()},
                new Object[]{"endTime", param.getEndTime()}
        );
        return Result.ok(linkStatsService.queryByArea(xyarr, param.getSelectAll(), page));
    }

    /**
     * 根据linkId查询流量
     * @param linkId linkId
     * @param param 分页/查询条件
     * @return
     */
    @PostMapping("/queryByLinkId/{linkId}")
    public Result queryByLinkId(@PathVariable String linkId, @RequestBody QueryParam param) {
        Page<LinkStats> page = new Page<>(param.getPageNum(), param.getPageSize());
        page.param(
                new Object[]{"type", param.getType()},
                new Object[]{"beginTime", param.getBeginTime()},
                new Object[]{"endTime", param.getEndTime()}
        );
        return Result.ok(linkStatsService.queryByLinkId(linkId, page));
    }

    /**
     * 查询link每小时平均流量
     * @param param
     * @return
     */
    @PostMapping("/queryAvgStats")
    public Result queryAvgStats(@RequestBody QueryParam param) {
        return Result.ok(linkStatsService.queryAvgStats(param.getIds(), param.getLinkId()));
    }

    @PostMapping("/export")
    public void export(@RequestBody QueryParam param, HttpServletResponse response) {
        List<LinkStats> list = linkStatsService.queryByIds(List.of(param.getIds()));

        String fileName = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() + "";

        response.addHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");

        try {
            OutputStream os = response.getOutputStream();

            Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("道路流量", "道路流量"),
                    LinkStats.class, list);

            workbook.write(os);

            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryParam {
        /**
         * 主键id
         */
        private Long[] ids;
        /**
         * 坐标数组
         * xyarr[x][0] x轴
         * xyarr[x][1] y轴
         */
        private double[][] xyarr;
        /**
         * 是否查询全部
         */
        private Boolean selectAll = false;
        /**
         * 分页每页大小
         */
        private Integer pageSize = 10;
        /**
         * 分页第几页
         */
        private Integer pageNum = 1;

        private String linkId;
        private Long wayId;
        /**
         * 调查方式
         */
        private String type;
        /**
         * 开始时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date beginTime;
        /**
         * 结束时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date endTime;

    }


}
