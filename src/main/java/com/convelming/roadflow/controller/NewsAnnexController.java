package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.service.NewsAnnexService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/newsAnnex")
public class NewsAnnexController {

    @Resource
    private NewsAnnexService newsAnnexService;

    @PutMapping
    public Result insert(MultipartFile file, long type) {
        return Result.ok(newsAnnexService.insert(file, type));
    }

    @DeleteMapping("/delete/{ids}")
    public Result delete(@PathVariable String ids) {
        return Result.ok(newsAnnexService.delete(ids));
    }

    @GetMapping("/batchDownload/{ids}")
    public void batchDownload(@PathVariable String ids, HttpServletResponse response) {
        newsAnnexService.batchDownload(ids, response);
    }

}
