package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson2.JSON;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.LinkProjectController;
import com.convelming.roadflow.mapper.LinkProjectMapper;
import com.convelming.roadflow.mapper.LinkProjectStatsMapper;
import com.convelming.roadflow.mapper.LinkStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.model.LinkProject;
import com.convelming.roadflow.model.LinkProjectStats;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.service.LinkProjectService;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LinkProjectServiceImpl implements LinkProjectService {

    @Resource
    LinkProjectMapper mapper;
    @Resource
    MatsimLinkMapper matsimLinkMapper;
    @Resource
    LinkProjectStatsMapper linkProjectStatsMapper;
    @Resource
    HttpServletRequest request;

    @Resource
    private LinkStatsMapper linkStatsMapper;

    final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public Page<LinkProject> list(Page<LinkProject> page) {
        return mapper.page(page);
    }

    @Override
    public LinkProject insert(LinkProject linkProject, MultipartFile file, double[][] xyarr) {
        // 保存shp
        if (file != null) {
            String name = file.getOriginalFilename();
            String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "/" + System.currentTimeMillis(); // 日期/当前毫秒数
            String dir = Constant.SHP_PATH + date + "/";
            new File(dir).mkdirs(); // 创建目录
            String shp = dir + name;
            try {
                FileCopyUtils.copy(file.getBytes(), new File(shp));
                // 处理shp
                List<String> geomStr = new ArrayList<>();
                FileDataStore dataStore = new ShapefileDataStoreFactory().createDataStore(Path.of(shp).toUri().toURL());
                String typeName = dataStore.getTypeNames()[0];
                // SimpleFeatureType schema = dataStore.getSchema(typeName);
                SimpleFeatureCollection featureCollection = dataStore.getFeatureSource(typeName).getFeatures();
                try (org.geotools.data.simple.SimpleFeatureIterator features = featureCollection.features()) {
                    while (features.hasNext()) {
                        SimpleFeature feature = features.next();
                        Geometry geometry = (Geometry) feature.getDefaultGeometry();
                        // 处理每个feature的属性和几何形状
                        String multilinestring = geometry.toString();
                        geomStr.add(multilinestring);
                    }
                }

                linkProject.setGeomFile("/" + shp.replaceAll("\\\\", "/").replaceAll(Constant.DATA_PATH, ""));
                linkProject.setGeomStr(JSON.toJSONString(geomStr));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("上传文件出错：" + e.getMessage());
            }
        } else {
            // MULTILINESTRING ((113.4492334 23.1814872, 113.44906 23.18166))
            Polygon geometry = createPolygon(xyarr);
            linkProject.setGeomStr(JSON.toJSONString(List.of(geometry.toString())));
        }
        if (!mapper.insert(linkProject)) {
            throw new RuntimeException("新增失败");
        }
        return linkProject;
    }

    @Override
    public boolean update(LinkProject linkProject) {


        return mapper.update(linkProject);
    }

    @Override
    public boolean delete(Long id) {
        return mapper.delete(id);
    }

    @Override
    public boolean addSample(LinkProjectController.Sample sample) {
        List<MatsimLink> links = matsimLinkMapper.selectContains(GeomUtil.genPolygon(sample.getXyarr(), 3857));
        // 如果框选到重复link先删除再添加
        List<LinkProjectStats> existData = linkProjectStatsMapper.queryExistData
                (links.stream().map(MatsimLink::getId).toList(), sample.getProjectId());
        linkProjectStatsMapper.batchDelete(existData.stream().map(LinkProjectStats::getId).toList());
        // 添加展示用数据
        List<LinkProjectStats> insertList = new ArrayList<>();
        String ipAddr = request.getRemoteAddr();
        for (MatsimLink matsimLink : links) {
            LinkProjectStats lps = new LinkProjectStats();
            lps.setProjectId(sample.getProjectId());
            lps.setLinkId(matsimLink.getId());
            lps.setService(sample.getService());
            lps.setSaturation(sample.getSaturation());
            lps.setStyle(sample.getStyle());
            lps.setIpAddr(ipAddr);
            insertList.add(lps);
        }
        return linkProjectStatsMapper.batchInsert(insertList);
    }

    @Override
    public List<LinkProjectStats> querySample(Long[] projectIds, String[] linkIds) {
        if (projectIds == null || projectIds.length == 0) {
            projectIds = new Long[]{0L}; // 默认项目
        }
        List<LinkProjectStats> list = linkProjectStatsMapper.query(projectIds, linkIds);

        List<LinkStats> linkStats = linkStatsMapper.queryByProjectIds(projectIds);
        // 计算平均值

        linkStats.forEach(ls -> {
            list.add(new LinkProjectStats(null, ls.getProjectId(), ls.getLinkId(), ls.getSaturation(), ls.getService(), "", null, null, ls));
        });

        if (!list.isEmpty()) {
            List<MatsimLink> links = matsimLinkMapper.queryByIds(list.stream().map(LinkProjectStats::getLinkId).toList());
            Map<String, MatsimLink> map = links.stream().collect(Collectors.toMap(MatsimLink::getId, x -> x, (x1, x2) -> x2));
            list.forEach(lp -> {
                lp.setLink(map.get(lp.getLinkId()));
            });
        }

        // 计算平均值
        List<LinkProjectStats> result = new ArrayList<>();
        Map<String, List<LinkProjectStats>> map = list.stream().collect(Collectors.groupingBy(LinkProjectStats::getLinkId));
        map.forEach((k, v) -> {
            LinkProjectStats lps = new LinkProjectStats();
            MatsimLink link = null;
            double saturation = 0.;
            for (LinkProjectStats l : v) {
                link = l.getLink();
                if (l.getSaturation() == null) {
                    LinkStatsServiceImpl.calcSetSaturation(l.getLinkStats(), l.getLink());
                    l.setSaturation(l.getLinkStats().getSaturation());
                }
                saturation += l.getSaturation();
                lps.setStyle(l.getStyle());
            }
            lps.setSaturation(saturation / v.size());
            lps.setService(calcService(lps.getSaturation()));
            lps.setLink(link);
            lps.setLinkId(k);
            result.add(lps);
        });

        return result;
    }


    public Polygon createPolygon(double[][] xyarr) {
        Coordinate[] shellPoints = new Coordinate[xyarr.length];
        for (int i = 0; i < xyarr.length; i++) {
            shellPoints[i] = new Coordinate(xyarr[i][0], xyarr[i][1]);
        }
        LinearRing shell = new LinearRing(new CoordinateArraySequence(shellPoints), geometryFactory);
        return new Polygon(shell, null, geometryFactory);
    }

    private String calcService(double saturation) {
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
        return service;
    }

}
