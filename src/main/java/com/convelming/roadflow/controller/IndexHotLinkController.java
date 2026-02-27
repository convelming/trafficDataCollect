package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.model.IndexHotLink;
import com.convelming.roadflow.service.IndexHotLinkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/indexHotLink")
public class IndexHotLinkController {

    @Resource
    private IndexHotLinkService service;

    // 点击链接
    @PostMapping("/click")
    public Result click(@RequestBody IndexHotLink ihl) {
        service.click(ihl);
        return Result.ok();
    }


    // 热点链接
    @GetMapping("/hotLinks")
    public Result hotLinks() {
        return Result.ok(service.hotLinks());
    }

}
