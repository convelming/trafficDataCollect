package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.service.PortalService;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/portal")
public class PortalController {

    @Resource
    private PortalService service;

    @PostMapping("/roadInfo")
    public Result roadInfo(@RequestBody Param param) {
        return Result.ok(service.roadInfo(param.url, param.xyarr));
    }

    @PostMapping("/poi")
    public Result poi() {
        return null;
    }

    @Data
    private static class Param {
        String url;
        double[][] xyarr;
    }


}
