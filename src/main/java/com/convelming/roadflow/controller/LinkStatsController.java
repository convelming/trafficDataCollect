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
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/link/stats")
public class LinkStatsController {

    @Resource
    private LinkStatsService linkStatsService;



    @PostMapping("/insert")
    public Result insert(@RequestBody LinkStats stats, HttpServletRequest request) {
        stats.setIpAddr(request.getRemoteAddr());
        return Result.fialOrOk(linkStatsService.insert(stats));
    }

    @PostMapping("/update")
    public Result update(@RequestBody LinkStats stats, HttpServletRequest request) {
        stats.setIpAddr(request.getRemoteAddr());
        return Result.fialOrOk(linkStatsService.update(stats));
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id) {
        return Result.ok(linkStatsService.queryById(id));
    }


    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.fialOrOk(linkStatsService.delete(id));
    }

    @GetMapping("/queryAllMaker")
    public Result queryAllMaker(){
        return Result.ok(linkStatsService.queryAllMaker());
    }

    @PostMapping("/queryByArea")
    public Result queryByArea(@RequestBody QeuryParam param) {
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

    @PostMapping("/queryByLinkId/{linkId}")
    public Result queryByLinkId(@PathVariable Long linkId, @RequestBody QeuryParam param) {
        Page<LinkStats> page = new Page<>(param.getPageNum(), param.getPageSize());
        page.param(
                new Object[]{"type", param.getType()},
                new Object[]{"beginTime", param.getBeginTime()},
                new Object[]{"endTime", param.getEndTime()}
        );
        return Result.ok(linkStatsService.queryByLinkId(linkId, page));
    }

    @PostMapping("/export")
    public void export(@RequestBody QeuryParam param, HttpServletResponse response) {
        List<LinkStats> list = linkStatsService.queryByIds(List.of(param.getIds()));

        String fileName = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli() + "";

        response.addHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.addHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        try {
            OutputStream os = response.getOutputStream();

            Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams("道路流量","道路流量"),
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
    private static class QeuryParam {
        private Long[] ids;
        private double[][] xyarr;
        private Boolean selectAll = false;

        private Integer pageSize = 10;
        private Integer pageNum = 1;

        private Long linkId;
        private Long wayId;
        private String type;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date beginTime;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date endTime;

    }


}
