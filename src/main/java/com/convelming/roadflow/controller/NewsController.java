package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.News;
import com.convelming.roadflow.service.NewsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/news")
public class NewsController {

    @Resource
    private NewsService newsService;

    // 新闻列表
    @GetMapping("/page")
    public Result page(Page<News> page) {
        return Result.ok(newsService.page(page));
    }

    // 新闻详情
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return Result.ok(newsService.detail(id));
    }

    // 新增新闻
    @PutMapping
    public Result insert(@RequestBody News news) {
        return Result.ok(newsService.insert(news));
    }

    // 修改新闻
    @PostMapping("/update")
    public Result update(@RequestBody News news) {
        return Result.ok(newsService.update(news));
    }

    // 删除新闻
    @DeleteMapping("/delete/{ids}")
    public Result delete(@PathVariable String ids) {
        return Result.ok(newsService.delete(ids));
    }

    // 上传附件
    @PostMapping("/upload/annex")
    public Result upload(MultipartFile file) {
        return Result.ok();
    }


}
