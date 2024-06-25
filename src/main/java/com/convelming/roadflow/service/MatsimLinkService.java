package com.convelming.roadflow.service;

import com.convelming.roadflow.model.MatsimLink;

import java.util.List;

public interface MatsimLinkService {

    List<List<MatsimLink>> queryByOrigid(String origid);

    MatsimLink queryById(String id);
    List<MatsimLink> queryLikeId(String id);

    MatsimLink queryReverseLink(String id);

    long update(MatsimLink link);

    long updateInWay(MatsimLink link);

}
