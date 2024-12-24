package com.convelming.roadflow.service;

import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.model.MapPicture;
import com.convelming.roadflow.model.vo.MapPictureVo;
import com.convelming.roadflow.model.vo.PictureDirVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;

public interface MapPictureService {

    Page<MapPicture> list(Page<MapPicture> page);

    Collection<PictureDirVo> treeList();

    Collection<MapPictureVo> allMaker();

    MapPicture detail(Long id);

    boolean unzip(MultipartFile file);

    boolean delete(String ids);

    boolean deleteByPath(String path);

}
