package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.Intersection;

public interface IntersectionService {

    Page<Intersection> list(Page<Intersection> page);
    Intersection detail(Long id);

    boolean insert(Intersection intersection);

    boolean update(Intersection intersection);

    boolean delete(Long id);

}
