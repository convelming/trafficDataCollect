package com.convelming.roadflow.service;

import com.convelming.roadflow.model.IndexHotLink;

import java.util.List;

public interface IndexHotLinkService {

    void click(IndexHotLink ihl);

    List<IndexHotLink> hotLinks();

}
