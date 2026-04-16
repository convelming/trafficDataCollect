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
import org.apache.commons.lang3.StringUtils;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
        Map<String, PictureDirVo> dirmap = new ConcurrentHashMap<>();
        Collection<MapPicture> piclist = mapper.list(param);
        piclist = piclist.stream().filter(
                mp -> {
                    String path = mp.getPath();
                    String name = mp.getPath().substring(34, path.lastIndexOf("/")); // /picture/2024-12-25/1735095400448/ 图片相对路径加时间戳长度
                    return name.contains(keyword);
                }
        ).toList();
        for (MapPicture mp : piclist) {
            String path = mp.getPath();
            String parentPath = path.substring(0, 34 + (path.substring(34).indexOf("/")));
            String name = parentPath.substring(parentPath.lastIndexOf("/") + 1);
            PictureDirVo dir = dirmap.get(Constant.DATA_PATH + mp.getPath());
            if (dir == null) {
                dir = new PictureDirVo();
                dir.setName(name);
                dir.setType(mp.getType());
                dir.setPath(parentPath.replace("\\", "/").replace(Constant.DATA_PATH, ""));
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
        }

        return dirmap.values();
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
    public boolean unzip(MultipartFile file, String projectName, String type) {
        String name = file.getOriginalFilename();
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "/" + System.currentTimeMillis(); // 日期/当前毫秒数
        String dir = Constant.PICTURE_PATH + date + "/";
        new File(dir).mkdirs(); // 创建目录
        String zip = dir + name;
        try {
            //
            byte[] bytes = new byte[10240];
            int len = 0;
            InputStream is = file.getInputStream();
            OutputStream os = new FileOutputStream(zip);
            while ((len = is.read(bytes)) != -1) {
                os.write(bytes, 0, len);
            }
            os.close();
            os.flush();
            is.close();
//            FileCopyUtils.copy(file.getBytes(), new File(zip));
        } catch (IOException e) {
            log.error("保存文件出错", e);
            throw new RuntimeException("上传文件出错：" + e.getMessage());
        }
        String output = zip.substring(0, zip.lastIndexOf("/")) + "/" + (StringUtils.isNotBlank(projectName) ? projectName : name.substring(0, name.lastIndexOf(".")));
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
            mp.setType(type);
            list.add(mp);
        }
        if (list.isEmpty()) {
            throw new RuntimeException("上传的zip压缩包中图片没有拍摄位置信息");
        }
        return mapper.batchInsert(list) > 0;
//        return false;
    }

    @Override
    public boolean uploadimg(MultipartFile file, String path) {
        String pf = Constant.DATA_PATH + path + "/" + file.getOriginalFilename();
        if (!FileUtil.isDir(Constant.DATA_PATH + path)) {
            throw new RuntimeException("路径不存在");
        }
        try {
            Files.copy(file.getInputStream(), Paths.get(pf), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("上传文件出错：" + e.getMessage());
        }
        PictureTag ptag = PictureTag.readPicture(new File(pf));
        if (ptag == null) {
            throw new RuntimeException("获取图片经纬度出错，请确认图片包含经纬度信息");
        }

        MapPicture mp = new MapPicture();
        String mpPath = pf.replaceAll("\\\\", "/").replaceAll(Constant.DATA_PATH, "");
        if (ptag.getFileName().toLowerCase().endsWith("heic")) {
            try {
                String jpgpath = mpPath + ".JPEG";
                BufferedImage image = ImageIO.read(new File(mpPath));
                ImageIO.write(image, "JPEG", new File(jpgpath));
                mp.setPath(jpgpath);
            } catch (Exception e) {
                mp.setPath(mpPath);
                log.error("HEIC转JPEG出错", e);
            }
        } else {
            mp.setPath(mpPath);
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
        mp.setType(mapper.queryTypeByPath(path));
        return mapper.batchInsert(List.of(mp)) > 0;
    }

    @Override
    public boolean rename(String path, String name) {
        if (!FileUtil.isDir(Constant.DATA_PATH + path)) {
            throw new RuntimeException("路径不存在");
        }
        Path oldPath = Paths.get(Constant.DATA_PATH + path);
        Path newPath = Paths.get(oldPath.getParent().toString() + File.separator + name);
        try {
            Files.move(oldPath, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        // 修改数据库
        Collection<MapPicture> list = mapper.list(new HashMap<>() {{
            put("path", path);
        }});
        list.forEach(mapPicture -> {
            String temp = mapPicture.getPath();
            temp = temp.replace(path, path.substring(0, path.lastIndexOf("/")) + "/" + name);
            mapPicture.setPath(temp);
        });
        long row = 0;
        try {
            row = mapper.batchUpdate(list);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.warn("修改数据库失败，尝试回滚文件");
            try {
                Files.move(newPath, oldPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return row > 0;
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
            dir.setType(mp.getType());
            tree(dir, subpath, mp);
        } else {
            root.getPictures().add(mp);
        }
    }

}
