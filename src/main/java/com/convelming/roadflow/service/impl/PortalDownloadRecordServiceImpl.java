package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.mapper.PortalDownloadRecordMapper;
import com.convelming.roadflow.model.PortalDownloadRecord;
import com.convelming.roadflow.service.PortalDownloadRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class PortalDownloadRecordServiceImpl implements PortalDownloadRecordService {

    @Resource
    private PortalDownloadRecordMapper mapper;

    @Override
    public void save(PortalDownloadRecord record) {
        try {
            String type = record.getUrl().substring(record.getUrl().lastIndexOf(".") + 1);
            record.setType(type);
            record.setDate(new Date());
            mapper.insert(record);
//            log.info("nginx访问：{}", record);
        } catch (Exception e) {
            log.error("记录nginx文件目录访问记录失败：{}", e.getMessage());
        }

    }
}
