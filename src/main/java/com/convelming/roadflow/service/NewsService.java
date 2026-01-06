package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.News;

public interface NewsService {

    // 列表
    Page<News> page(Page<News> page);

    // 详情
    News detail(Long id);

    // 新增
    boolean insert(News news);

    // 修改
    News update(News news);

    // 删除
    boolean delete(String ids);


}
