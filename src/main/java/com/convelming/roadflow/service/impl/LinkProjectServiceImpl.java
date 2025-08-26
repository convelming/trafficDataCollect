package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.LinkProjectController;
import com.convelming.roadflow.mapper.*;
import com.convelming.roadflow.model.*;
import com.convelming.roadflow.model.vo.LinkStatsChartVo;
import com.convelming.roadflow.service.LinkProjectService;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
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
import java.util.*;
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
    MatsimLinkServiceImpl matsimLinkService;
    @Resource
    OSMWayMapper osmWayMapper;
    @Resource
    HttpServletRequest request;

    @Resource
    private LinkStatsMapper linkStatsMapper;

    final GeometryFactory geometryFactory = new GeometryFactory();
    @Resource
    private LinkProjectMapper linkProjectMapper;

    @Override
    public LinkProject detail(Long id) {
        return mapper.selectById(id);
    }

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
                List<List<Double[]>> geomStr = new ArrayList<>();
                FileDataStore dataStore = new ShapefileDataStoreFactory().createDataStore(Path.of(shp).toUri().toURL());
                String typeName = dataStore.getTypeNames()[0];
                // SimpleFeatureType schema = dataStore.getSchema(typeName);
                SimpleFeatureCollection featureCollection = dataStore.getFeatureSource(typeName).getFeatures();
                try (SimpleFeatureIterator features = featureCollection.features()) {
                    while (features.hasNext()) {
                        SimpleFeature feature = features.next();
                        Geometry geometry = (Geometry) feature.getDefaultGeometry();
                        // 处理每个feature的属性和几何形状
                        List<Double[]> polygon = new ArrayList<>();
                        if (geometry.getGeometryType().endsWith("Polygon")) {
                            if (geometry.getSRID() != GeomUtil.MKT) {
                                throw new RuntimeException("请上传全部范围为srid=3857的shp文件");
                            }
                            for (Coordinate coordinate : geometry.getCoordinates()) {
                                polygon.add(new Double[]{coordinate.getX(), coordinate.getY()});
                            }
                        }
                        geomStr.add(polygon);
                    }
                }
                if (geomStr.isEmpty()) {
                    throw new RuntimeException("未识别到范围，请重新上传包含Polygon的shp文件");
                }

                linkProject.setGeomFile("/" + shp.replaceAll("\\\\", "/").replaceAll(Constant.DATA_PATH, ""));
                linkProject.setGeomStr(JSON.toJSONString(geomStr));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("上传文件出错：" + e.getMessage());
            }
        } else {
            // MULTILINESTRING ((113.4492334 23.1814872, 113.44906 23.18166))
//            Polygon geometry = createPolygon(xyarr);
            linkProject.setGeomStr(JSON.toJSONString(List.of(List.of(xyarr))));
        }
        if (!mapper.insert(linkProject)) {
            throw new RuntimeException("新增失败");
        }
        return linkProject;
    }

    @Override
    public LinkProject update(LinkProject linkProject, MultipartFile file, double[][] xyarr) {
        if (file != null) {
            String name = file.getOriginalFilename();
            String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "/" + System.currentTimeMillis(); // 日期/当前毫秒数
            String dir = Constant.SHP_PATH + date + "/";
            new File(dir).mkdirs(); // 创建目录
            String shp = dir + name;
            try {
                FileCopyUtils.copy(file.getBytes(), new File(shp));
                // 处理shp
                List<List<Double[]>> geomStr = new ArrayList<>();
                FileDataStore dataStore = new ShapefileDataStoreFactory().createDataStore(Path.of(shp).toUri().toURL());
                String typeName = dataStore.getTypeNames()[0];
                // SimpleFeatureType schema = dataStore.getSchema(typeName);
                SimpleFeatureCollection featureCollection = dataStore.getFeatureSource(typeName).getFeatures();
                try (SimpleFeatureIterator features = featureCollection.features()) {
                    while (features.hasNext()) {
                        SimpleFeature feature = features.next();
                        Geometry geometry = (Geometry) feature.getDefaultGeometry();
                        // 处理每个feature的属性和几何形状
                        List<Double[]> polygon = new ArrayList<>();
                        if (geometry.getGeometryType().endsWith("Polygon")) {
                            if (geometry.getSRID() != GeomUtil.MKT) {
                                throw new RuntimeException("请上传全部范围为srid=3857的shp文件");
                            }
                            for (Coordinate coordinate : geometry.getCoordinates()) {
                                polygon.add(new Double[]{coordinate.getX(), coordinate.getY()});
                            }
                        }
                        geomStr.add(polygon);
                    }
                }
                if (geomStr.isEmpty()) {
                    throw new RuntimeException("未识别到范围，请重新上传包含Polygon的shp文件");
                }

                linkProject.setGeomFile("/" + shp.replaceAll("\\\\", "/").replaceAll(Constant.DATA_PATH, ""));
                linkProject.setGeomStr(JSON.toJSONString(geomStr));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException("上传文件出错：" + e.getMessage());
            }
        } else {
            // MULTILINESTRING ((113.4492334 23.1814872, 113.44906 23.18166))
//            Polygon geometry = createPolygon(xyarr);
            linkProject.setGeomStr(JSON.toJSONString(List.of(List.of(xyarr))));
        }
        if (!mapper.update(linkProject)) {
            throw new RuntimeException("修改失败");
        }
        return linkProject;
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
    public List<List<LinkStatsChartVo>> querySample(Long areaProjectId, Long[] projectIds) {
        if (projectIds == null || projectIds.length == 0) {
            projectIds = new Long[]{0L}; // 默认项目
        }

        LinkProject areaProject = linkProjectMapper.selectById(areaProjectId);
        JSONArray array = JSONArray.parseArray(areaProject.getGeomStr());
        PGgeometry[] geoms = new PGgeometry[array.size()];
        for (int i = 0; i < array.size(); i++) {
            double[][] doubles = jsonArray2DoubleArray(array.getJSONArray(i));
            doubles = Arrays.copyOf(doubles, doubles.length + 1);
            doubles[doubles.length - 1] = doubles[0]; // 闭环
            geoms[i] = (GeomUtil.genPolygon(doubles, GeomUtil.MKT));
        }

        List<OSMWay> osmWays = osmWayMapper.queryByPolygon(geoms);
        List<MatsimLink> linkList = matsimLinkMapper.queryByOrigids(osmWays.stream().map(OSMWay::getId).toList());
        Map<String, List<MatsimLink>> osmLinkMap = linkList.stream().collect(Collectors.groupingBy(MatsimLink::getOrigid));

        List<LinkProjectStats> list = linkProjectStatsMapper.query(projectIds);
        List<LinkStats> linkStats = linkStatsMapper.queryByProjectIds(projectIds);
        linkStats.forEach(linkStat -> {
            list.add(new LinkProjectStats(linkStat.getProjectId(), linkStat.getLinkId(), linkStat.getSaturation(), linkStat.getService(), linkStat));
        });
        Map<String, MatsimLink> linkMap = matsimLinkMapper.queryByIds(list.stream().map(LinkProjectStats::getLinkId).toList()).stream().collect(Collectors.toMap(MatsimLink::getId, x -> x, (x1, x2) -> x1));
        // 填充 saturation service
        Map<String, List<LinkProjectStats>> statsMap = list.stream().collect(Collectors.groupingBy(LinkProjectStats::getLinkId));
        Map<String, LinkProjectStats> map = new HashMap<>();
        statsMap.forEach((k, v) -> {
            LinkProjectStats lps = new LinkProjectStats();
            double saturation = 0.;
            for (LinkProjectStats t : v) {
                MatsimLink link = linkMap.get(t.getLinkId());
                if (t.getSaturation() == null) {
                    LinkStatsServiceImpl.calcSetSaturation(t.getLinkStats(), link == null ? matsimLinkMapper.selectById(t.getLinkId()) : link);
                    t.setSaturation(t.getLinkStats().getSaturation());
                }
                saturation += t.getSaturation();
                lps.setStyle(t.getStyle());
            }
            lps.setSaturation(saturation);
            lps.setService(calcService(saturation));
            lps.setLinkId(k);
            map.put(k, lps);
        });

        // 分组
        List<List<MatsimLink>> osmLinks = new ArrayList<>();
        osmWays.parallelStream().forEach(osmway -> {
            try {
                osmLinks.addAll(matsimLinkService.buildTwoWay(osmLinkMap.get(osmway.getId()), osmway));
            } catch (Exception e) {
                log.error(e.getMessage(), osmLinkMap.get(osmway.getId()));
            }
        });

        List<List<LinkStatsChartVo>> result = new ArrayList<>();
        for (List<MatsimLink> olinks : osmLinks) {
            List<LinkStatsChartVo> voList = new ArrayList<>();
            for (MatsimLink link : olinks) {
                LinkProjectStats lps = map.get(link.getId());
                LinkStatsChartVo vo = new LinkStatsChartVo();
                vo.setOrigid(link.getOrigid());
                vo.setLinkId(link.getId());
                vo.setToxy(link.getToxy());
                vo.setFromxy(link.getFromxy());
                vo.setOneWay(link.getOneWay());
                if (lps != null) {
                    vo.setSaturation(lps.getSaturation());
                    vo.setService(lps.getService());
                }
                voList.add(vo);
            }
            result.add(voList);
        }
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

    public double[][] jsonArray2DoubleArray(JSONArray jsonArray) {
        double[][] result = new double[jsonArray.size()][];
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray item = jsonArray.getJSONArray(i);
            result[i] = new double[]{item.getDouble(0), item.getDouble(1)};
        }
        return result;
    }

}
