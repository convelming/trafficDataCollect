package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.mapper.LinkStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.vo.LinkStatsAvg;
import com.convelming.roadflow.service.LinkStatsService;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class LinkStatsServiceImpl implements LinkStatsService {

    @Resource
    private LinkStatsMapper linkStatsMapper;
    @Resource
    private MatsimLinkMapper matsimLinkMapper;

    /**
     * 一小时毫秒数
     */
    private final BigDecimal HOURS = new BigDecimal("3600000");
    private final BigDecimal M = new BigDecimal("1.5");
    private final BigDecimal L = new BigDecimal("2");

    @Override
    public boolean insert(LinkStats stats) {

        MatsimLink link = matsimLinkMapper.selectById(stats.getLinkId());
        stats.setWayId(link.getOrigid());
        // 中心点
        double[] xy = GeomUtil.point2xy(link.getCenter());
        stats.setX(xy[0]);
        stats.setY(xy[1]);
        // 计算 pcu/h
        BigDecimal pcu = BigDecimal.ZERO;
        pcu = pcu.add(BigDecimal.valueOf(stats.getScar())).add(BigDecimal.valueOf(stats.getStruck()));
        pcu = pcu.add(BigDecimal.valueOf(stats.getMcar()).multiply(M)).add(BigDecimal.valueOf(stats.getMtruck()).multiply(M));
        pcu = pcu.add(BigDecimal.valueOf(stats.getLcar()).multiply(L)).add(BigDecimal.valueOf(stats.getLtruck()).multiply(L));
        pcu = pcu.divide(BigDecimal.valueOf(stats.getEndTime().getTime() - stats.getBeginTime().getTime()), 64, RoundingMode.UP).multiply(HOURS);

        stats.setPcuH(pcu.setScale(2, RoundingMode.DOWN).doubleValue());

//        linkStatsMapper.insert(stats);

//        if (stats.getIsTwoWay()) {
//            try {
//                MatsimLink reverse = matsimLinkMapper.queryReverseLink(stats.getLinkId());
//                // 对面路也添加一条
//                LinkStats twoWay = new LinkStats();
//                BeanUtils.copyProperties(link, twoWay);
//                twoWay.setLinkId(reverse.getId());
//                twoWay.setScar(0.);
//                twoWay.setStruck(0.);
//                twoWay.setMcar(0.);
//                twoWay.setMtruck(0.);
//                twoWay.setLcar(0.);
//                twoWay.setLtruck(0.);
//                twoWay.setPcuH(0.);
////                linkStatsMapper.insert(twoWay);
//            } catch (Exception e) {
//                log.warn("找不到反向道路。");
//            }
//        }

        return linkStatsMapper.insert(stats);
    }

    @Override
    public boolean update(LinkStats stats) {
        LinkStats resource = linkStatsMapper.selectById(stats.getId());
        if (resource == null) {
            throw new RuntimeException("找不到要修改的对象");
        }
        // 计算 pcu/h
        BigDecimal pcu = BigDecimal.ZERO;
        pcu = pcu.add(BigDecimal.valueOf(stats.getScar())).add(BigDecimal.valueOf(stats.getStruck()));
        pcu = pcu.add(BigDecimal.valueOf(stats.getMcar()).multiply(M)).add(BigDecimal.valueOf(stats.getMtruck()).multiply(M));
        pcu = pcu.add(BigDecimal.valueOf(stats.getLcar()).multiply(L)).add(BigDecimal.valueOf(stats.getLtruck()).multiply(L));
        pcu = pcu.divide(BigDecimal.valueOf(stats.getEndTime().getTime() - stats.getBeginTime().getTime()), 64, RoundingMode.UP).multiply(HOURS);

        stats.setPcuH(pcu.setScale(2, RoundingMode.DOWN).doubleValue());

        MatsimLink link = matsimLinkMapper.selectById(stats.getLinkId());
        stats.setWayId(link.getOrigid());
        // 中心点
        double[] xy = GeomUtil.point2xy(link.getCenter());
        stats.setX(xy[0]);
        stats.setY(xy[1]);

//        if (stats.getIsTwoWay()) {
//
//            MatsimLink reverse = matsimLinkMapper.queryReverseLink(stats.getLinkId());
//            if (reverse != null) {
//                // 对面路也添加一条
//                LinkStats twoWay = new LinkStats();
//                BeanUtils.copyProperties(link, twoWay);
//                twoWay.setLinkId(reverse.getId());
//                twoWay.setScar(0.);
//                twoWay.setStruck(0.);
//                twoWay.setMcar(0.);
//                twoWay.setMtruck(0.);
//                twoWay.setLcar(0.);
//                twoWay.setLtruck(0.);
//                twoWay.setPcuH(0.);
//                linkStatsMapper.insert(twoWay);
//            }
//        }

        BeanUtils.copyProperties(stats, resource);
        return linkStatsMapper.update(resource);
    }

    @Override
    public boolean delete(Long id) {
        return linkStatsMapper.delete(id);
    }

    @Override
    public List<LinkStats> queryAllMaker(Date beginTime, Date endTime, String type) {
        return linkStatsMapper.queryAllMaker(beginTime, endTime, type);
    }

    @Override
    public Page<LinkStats> queryByArea(double[][] xyarr, Boolean all, Page<LinkStats> page) {
        return linkStatsMapper.queryByGeometry(GeomUtil.genPolygon(xyarr, GeomUtil.MKT), all, page);
    }

    @Override
    public Page<LinkStats> queryByLinkId(String linkId, Page<LinkStats> page) {
        return linkStatsMapper.queryByLinkId(linkId, page);
    }

    @Override
    public List<LinkStats> queryByIds(Collection<Long> ids) {
        return linkStatsMapper.queryByIds(ids);
    }

    @Override
    public List<LinkStatsAvg> queryAvgStats(Long[] ids, String linkId, String type) {
        return linkStatsMapper.queryAvgStats(ids, linkId, type);
    }

    @Override
    public LinkStats queryById(Long id) {
        return linkStatsMapper.selectById(id);
    }
}
