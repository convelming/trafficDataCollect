package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.mapper.NewsAnnexMapper;
import com.convelming.roadflow.mapper.NewsMapper;
import com.convelming.roadflow.model.News;
import com.convelming.roadflow.model.NewsAnnex;
import com.convelming.roadflow.service.NewsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NewsServiceImpl implements NewsService {

    @Resource
    private NewsMapper newsMapper;
    @Resource
    private NewsAnnexMapper newsAnnexMapper;


    @Override
    public Page<News> page(Page<News> page) {
        return newsMapper.page(page);
    }

    @Override
    public News detail(Long id) {
        News news = newsMapper.selectById(id);
        if (news != null) {
            List<NewsAnnex> annexes = newsAnnexMapper.select(new NewsAnnex() {{
                setNewsId(id);
            }});
            annexes.forEach(annex -> {
                annex.setUrl(Constant.FILE_DOWNLOAD_API + annex.getPath());
            }); // 填充下载地址
            news.setAnnexs(annexes);
        }
        return news;
    }

    @Override
    public boolean insert(News news) {
        long rows = newsMapper.insert(news);
        if (rows > 0) {
            // 绑定附件
            List<NewsAnnex> annexes = newsAnnexMapper.selectByIds(news.getAnnexs().stream().map(NewsAnnex::getId).collect(Collectors.toList()));
            long bindRows = newsAnnexMapper.bindNews(annexes, news.getId());
            annexes.forEach(annex -> {
                annex.setUrl(Constant.FILE_DOWNLOAD_API + annex.getPath());
            }); // 填充下载地址
        }
        return rows > 0;
    }

    @Override
    public News update(News news) {
        if (newsMapper.update(news) > 0) {
            // 清空附件列表
            long unbindRow = newsAnnexMapper.unbindAnnex(news.getId());
            // 重新绑定附件列表
            List<NewsAnnex> annexes = newsAnnexMapper.selectByIds(news.getAnnexs().stream().map(NewsAnnex::getId).collect(Collectors.toList()));
            annexes.forEach(annex -> {
                annex.setNewsId(news.getId());
            });
            long bindRows = newsAnnexMapper.bindNews(annexes, news.getId());
            return detail(news.getId());
        }
        return news;
    }

    @Override
    public boolean delete(String ids) {
        String[] idArray = ids.split(",");
        List<Long> idList = new ArrayList<>(idArray.length);
        for (String s : idArray) {
            idList.add(Long.parseLong(s));
        }
        return newsMapper.batchDelete(idList) > 0;
    }
}
