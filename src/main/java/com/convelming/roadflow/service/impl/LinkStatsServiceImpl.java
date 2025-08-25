package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.enums.HighwayType;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class
LinkStatsServiceImpl implements LinkStatsService {

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
        // 当没有填写pcuh并且车辆数量有填写时计算 pcu/h
//        if ((stats.getPcuH() == null) && (stats.getScar() != null || stats.getMcar() != null || stats.getLcar() != null)) {
//            BigDecimal pcu = BigDecimal.ZERO;
//            pcu = pcu.add(BigDecimal.valueOf(stats.getScar())).add(BigDecimal.valueOf(stats.getStruck()));
//            pcu = pcu.add(BigDecimal.valueOf(stats.getMcar()).multiply(M)).add(BigDecimal.valueOf(stats.getMtruck()).multiply(M));
//            pcu = pcu.add(BigDecimal.valueOf(stats.getLcar()).multiply(L)).add(BigDecimal.valueOf(stats.getLtruck()).multiply(L));
//            pcu = pcu.divide(BigDecimal.valueOf(stats.getEndTime().getTime() - stats.getBeginTime().getTime()), 64, RoundingMode.UP).multiply(HOURS);
//            stats.setPcuH(pcu.setScale(2, RoundingMode.DOWN).doubleValue());
//        }

        calcSetSaturation(stats, link);

        return linkStatsMapper.insert(stats);
    }

    @Override
    public boolean update(LinkStats stats) {
        LinkStats resource = linkStatsMapper.selectById(stats.getId());
        if (resource == null) {
            throw new RuntimeException("找不到要修改的对象");
        }
        // 当没有填写pcuh并且车辆数量有填写时计算 pcu/h
//        if ((stats.getPcuH() == null) && (stats.getScar() != null || stats.getMcar() != null || stats.getLcar() != null)) {
//            BigDecimal pcu = BigDecimal.ZERO;
//            pcu = pcu.add(BigDecimal.valueOf(stats.getScar())).add(BigDecimal.valueOf(stats.getStruck()));
//            pcu = pcu.add(BigDecimal.valueOf(stats.getMcar()).multiply(M)).add(BigDecimal.valueOf(stats.getMtruck()).multiply(M));
//            pcu = pcu.add(BigDecimal.valueOf(stats.getLcar()).multiply(L)).add(BigDecimal.valueOf(stats.getLtruck()).multiply(L));
//            pcu = pcu.divide(BigDecimal.valueOf(stats.getEndTime().getTime() - stats.getBeginTime().getTime()), 64, RoundingMode.UP).multiply(HOURS);
//            stats.setPcuH(pcu.setScale(2, RoundingMode.DOWN).doubleValue());
//        }

        MatsimLink link = matsimLinkMapper.selectById(stats.getLinkId());
        stats.setWayId(link.getOrigid());

        calcSetSaturation(stats, link);

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
    @Transactional
    public boolean reinstated(LinkStats stats, List<String> linkIds) {

        boolean flag = true;
        if (stats.getId() == null) {
            flag = this.insert(stats);
        }

        for (String linkId : linkIds) {
            LinkStats temp = new LinkStats();
            BeanUtils.copyProperties(stats, temp);
            temp.setId(null);
            temp.setLinkId(linkId);
            temp.setType("4");
            temp.setVersion(1);
            flag = insert(temp);
            if (!flag) {
                throw new RuntimeException("");
            }
        }

        return flag;
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
        page = linkStatsMapper.queryByLinkId(linkId, page);
        if (!page.getData().isEmpty()) {
            MatsimLink link = matsimLinkMapper.selectById(linkId);
            page.getData().forEach(stats -> {
                calcSetSaturation(stats, link);
                stats.setReal(true);
            });
        }
        return page;
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


    public static void calcSetSaturation(LinkStats linkStats, MatsimLink link) {
        // pcu / (车道数 * 通行能力)
        double saturation = 0.;
        Double capacity = linkStats.getCapacity();
        if (capacity == null || capacity <= 0) {
            HighwayType ht = HighwayType.getOfCode(link.getType());
            if (ht == null) {
                capacity = 300.;
            } else {
                capacity = ht.getCapacity();
            }
        }
        saturation = linkStats.getPcuH() / (capacity * (link.getLane() == null ? 1. : link.getLane()));
        linkStats.setSaturation(saturation);
        String service = "A";
        if (0 <= saturation && saturation <= 0.4) {
            service = "A";
        } else if (0.4 < saturation && saturation <= 0.6) {
            service = "B";
        } else if (0.6 < saturation && saturation <= 0.75) {
            service = "C";
        } else if (0.75 < saturation && saturation <= 0.85) {
            service = "D";
        } else if (0.85 < saturation && saturation <= 0.95) {
            service = "E";
        } else if (0.95 < saturation) {
            service = "F";
        }
        linkStats.setService(service);
    }

}
