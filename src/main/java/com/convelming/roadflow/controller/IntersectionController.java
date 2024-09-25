package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.Intersection;
import com.convelming.roadflow.service.IntersectionService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/intersection")
public class IntersectionController {

    @Resource
    private IntersectionService service;

    @PostMapping("/list")
    public Result list(@RequestBody QueryParam param) {
        Page<Intersection> page = new Page<>(param.getPageNum(), param.getPageSize());
        page.param(new Object[]{"name", param.getName()});
        return Result.ok(service.list(page));
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id){
        return Result.ok(service.detail(id));
    }

    @PostMapping("/insert")
    public Result insert(@RequestBody Intersection intersection) {
        return Result.failOrOk(service.insert(intersection));
    }

    @PostMapping("/update")
    public Result update(@RequestBody Intersection intersection) {
        return Result.failOrOk(service.update(intersection));
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return Result.failOrOk(service.delete(id));
    }

    @Data
    public static class QueryParam {

        private String name;
        /**
         * 分页每页大小
         */
        private Integer pageSize = 10;
        /**
         * 分页第几页
         */
        private Integer pageNum = 1;
    }


}
