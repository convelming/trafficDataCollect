package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.IndexHotLink;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@Repository
public class IndexHotLinkMapper {

    private static final String TABLE_NAME = " index_hot_link ";

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    public void click(IndexHotLink ihl) {
        IndexHotLink count = eeq.queryable(IndexHotLink.class)
                .where(t -> t.name().eq(ihl.getName()))
                .singleOrNull();
        if (count != null) {
            eeq.sqlExecute("update index_hot_link set click_count = click_count + 1 where id = ?", Collections.singletonList(count.getId()));
        } else {
            ihl.setId(idUtil.getId(TABLE_NAME));
            ihl.setClickCount(1L);
            eeq.insertable(ihl).executeRows();
        }
    }

    public List<IndexHotLink> hot(int size) {
        return eeq.queryable(IndexHotLink.class)
                .orderBy(t -> t.clickCount().desc())
                .limit(0, size).toList();
    }

}
