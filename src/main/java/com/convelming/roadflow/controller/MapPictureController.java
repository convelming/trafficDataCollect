package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.MapPicture;
import com.convelming.roadflow.service.MapPictureService;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

@RestController
@RequestMapping("/mappicture")
public class MapPictureController {

    @Resource
    private MapPictureService service;

    /**
     * 列表
     */
    @PostMapping("/list")
    public Result list(@RequestBody QueryParam param) {
        Page<MapPicture> page = new Page<>(param.pageNum, param.getPageSize());
        return Result.failOrOk(service.list(page));
    }

    @PostMapping("/treeList")
    public Result treeList(@RequestBody(required = false) QueryParam param) {
        HashMap<String, Object> map = new HashMap<>();
        if (param != null) {
            map.put("name", param.name);
            map.put("beginTime", param.beginTime);
            map.put("endTime", param.endTime);
        }
        return Result.failOrOk(service.treeList(map));
    }


    @PostMapping("/deleteByPath")
    public Result deleteByPath(@RequestBody QueryParam param) {
        return Result.failOrOk(service.deleteByPath(param.path));
    }

    /**
     * 全部
     */
    @PostMapping("/allMaker")
    public Result allMark() {
        return Result.failOrOk(service.allMaker());
    }

    /**
     * id查询
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.failOrOk(service.detail(id));
    }

    /**
     * 上传zip
     */
    @PostMapping("/uploadzip")
    public Result uploadzip(MultipartFile file) {
        return Result.failOrOk(service.unzip(file));
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{ids}")
    public Result delete(@PathVariable String ids) {
        return Result.failOrOk(service.delete(ids));
    }

    @Data
    private static class QueryParam {
        /**
         * 分页每页大小
         */
        private Integer pageSize = 10;
        /**
         * 分页第几页
         */
        private Integer pageNum = 1;

        private String path;

        private String name;
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date beginTime;
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private Date endTime;

    }

}
