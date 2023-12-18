package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.mapper.LinkStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.service.LinkStatsService;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class LinkStatsServiceImpl implements LinkStatsService {

    @Resource
    private LinkStatsMapper linkStatsMapper;
    @Resource
    private MatsimLinkMapper matsimLinkMapper;

    @Override
    public boolean insert(LinkStats stats) {

        MatsimLink link = matsimLinkMapper.selectById(stats.getLinkId());
        stats.setWayId(link.getOrigid());
        // 中心点
        double[] xy = GeomUtil.point2xy(link.getCenter());
        stats.setX(xy[0]);
        stats.setY(xy[1]);
        // 计算 pcu/h
        double pcu = 0.;
        pcu += stats.getScar() + stats.getStruck();
        pcu += (stats.getMcar() + stats.getMtruck()) * 1.5;
        pcu += (stats.getLcar() + stats.getLtruck()) * 2.0;
        stats.setPcuH(pcu);

        if (stats.getIsTwoWay()) {

            MatsimLink reverse = matsimLinkMapper.queryReverseLink(stats.getLinkId());
            if (reverse != null) {
                // 对面路也添加一条
                LinkStats twoWay = new LinkStats();
                BeanUtils.copyProperties(link, twoWay);
                twoWay.setLinkId(reverse.getId());
                linkStatsMapper.insert(twoWay);
            }
        }

        return linkStatsMapper.insert(stats);
    }

    @Override
    public boolean update(LinkStats stats) {
        LinkStats resource = linkStatsMapper.selectById(stats.getId());
        if (resource == null) {
            throw new RuntimeException("找不到要修改的对象");
        }
        // 计算 pcu/h
        double pcu = 0.;
        pcu += stats.getScar() + stats.getStruck();
        pcu += (stats.getMcar() + stats.getMtruck()) * 1.5;
        pcu += (stats.getLcar() + stats.getLtruck()) * 2.0;
        stats.setPcuH(pcu);

        MatsimLink link = matsimLinkMapper.selectById(stats.getLinkId());
        stats.setWayId(link.getOrigid());
        // 中心点
        double[] xy = GeomUtil.point2xy(link.getCenter());
        stats.setX(xy[0]);
        stats.setY(xy[1]);

        BeanUtils.copyProperties(stats, resource);
        return linkStatsMapper.update(resource);
    }

    @Override
    public boolean delete(Long id) {
        return linkStatsMapper.delete(id);
    }

    @Override
    public List<LinkStats> queryAllMaker() {
        return linkStatsMapper.queryAllMaker();
    }

    @Override
    public Page<LinkStats> queryByArea(double[][] xyarr, Boolean all, Page<LinkStats> page) {
        return linkStatsMapper.queryByGeometry(GeomUtil.genPolygon(xyarr, GeomUtil.MKT), all, page);
    }

    @Override
    public Page<LinkStats> queryByLinkId(Long linkId, Page<LinkStats> page) {
        return linkStatsMapper.queryByLinkId(linkId, page);
    }

    @Override
    public List<LinkStats> queryByIds(Collection<Long> ids) {
        return linkStatsMapper.queryByIds(ids);
    }

    @Override
    public LinkStats queryById(Long id) {
        return linkStatsMapper.selectById(id);
    }
}
