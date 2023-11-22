package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.service.MatsimLinkService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/matsim/link")
public class MatsimLinkController {

    @Resource
    private MatsimLinkService matsimLinkService;

    @GetMapping("/getMatsimLink/{origid}")
    public Result getMatsimLinkByWayId(@PathVariable Long origid){
        return Result.ok(matsimLinkService.queryByOrigid(origid));
    }


    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        return Result.ok(matsimLinkService.queryById(id));
    }



}
