package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.mapper.MapPictureMapper;
import com.convelming.roadflow.model.MapPicture;
import com.convelming.roadflow.model.PictureTag;
import com.convelming.roadflow.model.vo.MapPictureVo;
import com.convelming.roadflow.service.MapPictureService;
import com.convelming.roadflow.util.FileUtil;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class MapPictureServiceImpl implements MapPictureService {

    private static final CoordinateTransformation ct_4326to3857 = TransformationFactory.getCoordinateTransformation("epsg:4326", "epsg:3857");

    @Resource
    private HttpServletRequest request;

    @Resource
    private MapPictureMapper mapper;

    @Override
    public Page<MapPicture> list(Page<MapPicture> page) {
        return mapper.page(page);
    }

    @Override
    public Collection<MapPictureVo> allMaker() {
        Collection<MapPicture> list = mapper.all();
        return list.stream().map(MapPictureVo::new).toList();
    }

    @Override
    public MapPicture detail(Long id) {
        MapPicture mp = mapper.selectById(id);
        if (mp != null) {
            mp.setUrl(Constant.FILE_DOWNLOAD_API + mp.getPath());
        }
        return mp;
    }

    @Override
    public boolean unzip(MultipartFile file) {
        String name = file.getOriginalFilename();
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "/" + System.currentTimeMillis(); // 日期/当前毫秒数
        String dir = Constant.PICTURE_PATH + date + "/";
        new File(dir).mkdirs(); // 创建目录
        String zip = dir + name;
        try {
            FileCopyUtils.copy(file.getBytes(), new File(zip));
        } catch (IOException e) {
            log.error("保存文件出错", e);
            throw new RuntimeException("上传文件出错：" + e.getMessage());
        }
        String output = zip.substring(0, zip.lastIndexOf("."));
        try {
            FileUtil.unzip(dir + name, output);
        } catch (IOException e) {
            log.error("解压zip文件出错", e);
            throw new RuntimeException("解压zip文件出错：" + e.getMessage());
        }
        // 保存到数据库
        File outputdir = new File(output);
        List<File> filelist = getDirAllFile(outputdir);

        List<MapPicture> list = new ArrayList<>();
        for (File pf : filelist) {
            PictureTag ptag = PictureTag.readPicture(pf);
            if (ptag == null) {
                continue;
            }
            MapPicture mp = new MapPicture();
            mp.setLat(ptag.getLat());
            mp.setLon(ptag.getLon());
            Coord coord3857 = ct_4326to3857.transform(new Coord(mp.getLon(), mp.getLat()));
            mp.setName(ptag.getFileName());
            mp.setPath("/" + pf.getAbsolutePath().replaceAll("\\\\", "/").replaceAll(Constant.DATA_PATH, ""));
            mp.setIpAddr(request.getRemoteAddr());
            mp.setX(coord3857.getX());
            mp.setY(coord3857.getY());
            mp.setGeom(GeomUtil.genPoint(mp.getX(), mp.getY(), 3857));
            list.add(mp);
        }
        if(list.isEmpty()){
            throw new RuntimeException("上传的zip压缩包中图片没有拍摄位置信息");
        }
        return mapper.batchInsert(list) > 0;
//        return false;
    }

    @Override
    public boolean delete(String ids) {
        List<Long> list = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
        return mapper.batchDeleteById(list) > 0;
    }

    private List<File> getDirAllFile(File dir) {
        List<File> list = new ArrayList<>();
        File[] filelist = dir.listFiles();
        if (filelist != null) {
            for (File file : filelist) {
                if (file.isDirectory()) {
                    list.addAll(getDirAllFile(file));
                } else {
                    list.add(file);
                }
            }
        }
        return list;
    }
}
