package com.convelming.roadflow.controller;

import com.convelming.roadflow.model.PortalDownloadRecord;
import com.convelming.roadflow.service.PortalDownloadRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/portal")
public class PortalDownloadRecordController {

    @Resource
    private PortalDownloadRecordService service;

    @PostMapping("/record")
    public void record(@RequestHeader("X-Real-IP") String ip,
                       @RequestHeader("X-Original-URI") String encodedUrl,
                       @RequestHeader("X-Time") String time) {
        String url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);
        if (!url.contains(".")) {
            return;
        }
        PortalDownloadRecord record = new PortalDownloadRecord();
        record.setIp(ip);
        record.setUrl(url);
        log.info("Access record - IP: {}, URL: {}, Time: {}", ip, url, time);
        service.save(record);
    }

}
