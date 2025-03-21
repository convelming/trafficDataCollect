package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.mapper.MapPictureMapper;
import com.convelming.roadflow.model.MapPicture;
import com.convelming.roadflow.model.PictureTag;
import com.convelming.roadflow.model.vo.MapPictureVo;
import com.convelming.roadflow.model.vo.PictureDirVo;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    public Collection<PictureDirVo> treeList(Map<String, Object> param) {
        String keyword = (String) param.get("name");
        Map<String, PictureDirVo> dirmap = new HashMap<>();
        Collection<MapPicture> piclist = mapper.list(param);
        l:
        for (MapPicture mp : piclist) {
            File picfile = new File(Constant.DATA_PATH + mp.getPath());
            File parentfile = picfile.getParentFile();
            while (parentfile.list((dir, name) -> name.endsWith(".zip")) == null || parentfile.list((dir, name) -> name.endsWith(".zip")).length == 0) {
                if (parentfile.getName().endsWith("/picture/")) {
                    break l;
                }
                parentfile = parentfile.getParentFile();
            }
            File[] pfs = parentfile.listFiles((name, dir) -> !dir.endsWith(".zip"));
            if (pfs != null) {
                parentfile = pfs[0];
            } else {
                continue; //
            }
            PictureDirVo dir = dirmap.get(picfile.getPath());
            if (dir == null) {
                dir = new PictureDirVo();
                dir.setName(parentfile.getName());
                dir.setPath("/" + parentfile.getPath().replace("\\", "/").replace(Constant.DATA_PATH, ""));
                dir.setCreateTime(mp.getDataTime());
                dirmap.put(dir.getPath(), dir);
            }
        }
        // dirmap 构建子目录
        for (Map.Entry<String, PictureDirVo> entry : dirmap.entrySet()) {
            PictureDirVo root = entry.getValue();
            for (MapPicture mp : piclist) {
                String mpath = mp.getPath();
                if (mpath.startsWith(root.getPath())) {
                    String subpath = mpath.replace(root.getPath(), "");
                    tree(root, subpath, mp);
                }
            }
            // 关键字过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                root.filter(keyword);
            }
        }
        return dirmap.values().stream().filter(dir -> !dir.getSubdir().isEmpty() || !dir.getPictures().isEmpty()).toList();
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

            String path = "/" + pf.getAbsolutePath().replaceAll("\\\\", "/").replaceAll(Constant.DATA_PATH, "");

            if (ptag.getFileName().toLowerCase().endsWith("heic")) {
                try {
                    String jpgpath = path + ".JPEG";
                    BufferedImage image = ImageIO.read(new File(path));
                    ImageIO.write(image, "JPEG", new File(jpgpath));
                    mp.setPath(jpgpath);
                } catch (Exception e) {
                    mp.setPath(path);
                    log.error("HEIC转JPEG出错", e);
                }
            } else {
                mp.setPath(path);
            }

            mp.setLat(ptag.getLat());
            mp.setLon(ptag.getLon());
            Coord coord3857 = ct_4326to3857.transform(new Coord(mp.getLon(), mp.getLat()));
            mp.setDataTime(ptag.getDateTime());
            mp.setName(ptag.getFileName());
            mp.setIpAddr(request.getRemoteAddr());
            mp.setX(coord3857.getX());
            mp.setY(coord3857.getY());
            mp.setGeom(GeomUtil.genPoint(mp.getX(), mp.getY(), 3857));
            list.add(mp);
        }
        if (list.isEmpty()) {
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

    @Override
    public boolean deleteByPath(String path) {
        if (path != null) {
            path = path.trim();
            if (!path.isEmpty()) {
                return mapper.deleteByPath(path) > 0;
            }
        }
        return false;
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

    public void tree(PictureDirVo root, String subpath, MapPicture mp) {
        int index = subpath.indexOf("/", 1);
        if (index > 0) {
            String name = subpath.substring(0, index);
            subpath = subpath.substring(index);
            String dirpath = root.getPath() + name;
            PictureDirVo dir = root.getSubdirByPath(dirpath);
            if (dir == null) {
                dir = new PictureDirVo();
                root.getSubdir().add(dir);
            }
            dir.setPath(root.getPath() + name);
            dir.setName(name.replace("/", ""));
            dir.setCreateTime(mp.getDataTime());
            tree(dir, subpath, mp);
        } else {
            root.getPictures().add(mp);
        }
    }

}
