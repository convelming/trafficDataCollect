package com.convelming.roadflow.model;

import com.convelming.roadflow.model.proxy.PortalDownloadRecordProxy;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString

@Table("portal_download_record")
@EntityProxy
public class PortalDownloadRecord implements ProxyEntityAvailable<PortalDownloadRecord, PortalDownloadRecordProxy>{

    private Long id;
    private String ip;
    private String url;
    private String type;
    private Date date;

}
