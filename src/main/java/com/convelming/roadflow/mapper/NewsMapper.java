package com.convelming.roadflow.mapper;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.News;
import com.convelming.roadflow.model.NewsAnnex;
import com.convelming.roadflow.util.IdUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class NewsMapper {

    @Resource
    private IdUtil idUtil;
    @Resource
    private EasyEntityQuery eeq;

    private static final String TABLE_NAME = " news ";
    @Autowired
    private NewsAnnexMapper newsAnnexMapper;

    // 新增
    public long insert(News news) {
        news.setId(idUtil.getId(TABLE_NAME));
        return eeq.insertable(news).executeRows();
    }

    // 删除
    public long delete(long id) {
        return eeq.deletable(News.class).where(t -> t.id().eq(id)).executeRows();
    }

    // 删除
    public long batchDelete(List<Long> id) {
        return eeq.deletable(News.class).where(t -> t.id().in(id)).executeRows();
    }

    // 修改
    public long update(News news) {
        return eeq.updatable(news).executeRows();
    }

    // 详情
    public News selectById(long id) {
        News news = eeq.queryable(News.class).where(t -> t.id().eq(id)).singleOrNull();
        if (news != null) {
            // 填充附件
            List<NewsAnnex> annex = eeq.queryable(NewsAnnex.class).where(n -> n.newsId().eq(id)).toList();
            news.setAnnexs(annex);
        }
        return news;
    }

    // 列表
    public Page<News> page(Page<News> page) {
        Map<String, Object> params = page.getParam();

        String typeStr = String.valueOf(params.get("type"));
        if (typeStr.isEmpty() || typeStr.equals("null")) {
            typeStr = "0";
        }
        int type = Integer.parseInt(typeStr);
        List<News> data = eeq.queryable(News.class)
                // 排序id倒序
                .orderBy(n -> n.date().desc())
                // 查询条件
                .where(t -> {
                    t.type().eq(type);
                })
                .limit(page.getOffset(), page.getPageSize())
                .toList();

        // 填充附件列表
        data.forEach(news -> {
            news.setAnnexs(newsAnnexMapper.select(new NewsAnnex() {{
                setNewsId(news.getId());
            }}));
            news.getAnnexs().forEach(annex -> {
                annex.setUrl(Constant.FILE_DOWNLOAD_API + annex.getPath());
            }); // 填充下载地址
        });

        long total = eeq.queryable(News.class)
                .where(t -> {
                    t.type().eq(type);
                })
                .count();
        return page.build(data, total);
    }

}
