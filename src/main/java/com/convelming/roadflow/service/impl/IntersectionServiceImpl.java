package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.mapper.IntersectionMapper;
import com.convelming.roadflow.model.Intersection;
import com.convelming.roadflow.service.IntersectionService;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class IntersectionServiceImpl implements IntersectionService {

    @Resource
    private IntersectionMapper mapper;

    @Override
    public Page<Intersection> list(Page<Intersection> page) {
        return mapper.list(page);
    }

    @Override
    public Intersection detail(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public Intersection insert(Intersection intersection) {
        intersection.setGeom(GeomUtil.genPoint(intersection.getX(), intersection.getY(), GeomUtil.MKT));
        intersection.setStatus(0);
        if(mapper.insert(intersection)){
            return intersection;
        }
        return null;
    }

    @Override
    public boolean update(Intersection intersection) {
        Intersection targ = mapper.selectById(intersection.getId());
        if (targ == null) {
            return false;
        }
        targ.setName(intersection.getName());
        targ.setX(intersection.getX());
        targ.setY(intersection.getY());
        targ.setGeom(GeomUtil.genPoint(intersection.getX(), intersection.getY(), GeomUtil.MKT));
        targ.setVersion(targ.getVersion() + 1);
        targ.setUpdateTime(new Date());
        return mapper.updateById(targ);
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(id);
    }

}
