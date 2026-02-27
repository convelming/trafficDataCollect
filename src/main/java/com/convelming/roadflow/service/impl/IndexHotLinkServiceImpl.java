package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.mapper.IndexHotLinkMapper;
import com.convelming.roadflow.model.IndexHotLink;
import com.convelming.roadflow.service.IndexHotLinkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class IndexHotLinkServiceImpl implements IndexHotLinkService {

    @Resource
    private IndexHotLinkMapper mapper;

    @Override
    public void click(IndexHotLink ihl) {
        mapper.click(ihl);
    }

    @Override
    public List<IndexHotLink> hotLinks() {
        return mapper.hot(5);
    }
}
