package com.convelming.roadflow.service;

import com.convelming.roadflow.model.MatsimLink;

import java.util.List;

public interface MatsimLinkService {

    List<List<MatsimLink>> queryByOrigid(Long origid);

    MatsimLink queryById(Long id);

}
