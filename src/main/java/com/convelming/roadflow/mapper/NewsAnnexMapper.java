package com.convelming.roadflow.mapper;

import com.convelming.roadflow.model.NewsAnnex;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class NewsAnnexMapper {

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    private static final String TABLE_NAME = " news_annex ";

    public long insert(NewsAnnex newsAnnex) {
        newsAnnex.setId(idUtil.getId(TABLE_NAME));
        return eeq.insertable(newsAnnex).executeRows();
    }

    public List<NewsAnnex> select(NewsAnnex param) {
        return eeq.queryable(NewsAnnex.class).where(n -> n.newsId().eq(param.getNewsId())).toList();
    }

    public List<NewsAnnex> selectByIds(List<Long> ids) {
        return eeq.queryable(NewsAnnex.class).where(n -> n.id().in(ids)).toList();
    }

    public long bindNews(List<NewsAnnex> annexes, Long newsId) {
        annexes.forEach(annex -> {
            annex.setNewsId(newsId);
        });
        return eeq.updatable(annexes).executeRows();
    }

    public long unbindAnnex(Long newsId) {
        return eeq.updatable(NewsAnnex.class).setColumns(a -> a.newsId().setNull()).where(n -> n.newsId().eq(newsId)).executeRows();
    }

    public long delete(List<Long> ids) {
        return eeq.deletable(NewsAnnex.class).where(n -> n.id().in(ids)).executeRows();
    }

    public long delete(Long id) {
        return eeq.deletable(NewsAnnex.class).where(n -> n.id().eq(id)).executeRows();
    }
}
