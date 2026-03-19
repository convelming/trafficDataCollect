package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.PortalDownloadRecord;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

@Repository
public class PortalDownloadRecordMapper {

    private static final String TABLE_NAME = " portal_download_record ";

    @Resource
    private EasyEntityQuery eeq;
    @Resource
    private IdUtil idUtil;

    public long insert(PortalDownloadRecord record) {
        record.setId(idUtil.getId(TABLE_NAME));
        return eeq.insertable(record).executeRows();
    }

}
