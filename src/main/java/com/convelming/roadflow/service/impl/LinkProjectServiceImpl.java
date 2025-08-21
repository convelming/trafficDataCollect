package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson2.JSON;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.LinkProjectController;
import com.convelming.roadflow.mapper.LinkProjectMapper;
import com.convelming.roadflow.mapper.LinkProjectStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.model.LinkProject;
import com.convelming.roadflow.model.LinkProjectStats;
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

    final GeometryFactory geometryFactory = new GeometryFactory();

    @Override
    public Page<LinkProject> list(Page<LinkProject> page) {
        return mapper.page(page);
    }

    @Override
    public boolean insert(LinkProject linkProject, MultipartFile file, double[][] xyarr) {
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

        return mapper.insert(linkProject);
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
    public List<LinkProjectStats> querySample(Long projectId, String linkId) {
        return linkProjectStatsMapper.query(projectId, linkId);
    }


    public Polygon createPolygon(double[][] xyarr) {
        Coordinate[] shellPoints = new Coordinate[xyarr.length];
        for (int i = 0; i < xyarr.length; i++) {
            shellPoints[i] = new Coordinate(xyarr[i][0], xyarr[i][1]);
        }
        LinearRing shell = new LinearRing(new CoordinateArraySequence(shellPoints), geometryFactory);
        return new Polygon(shell, null, geometryFactory);
    }

}
