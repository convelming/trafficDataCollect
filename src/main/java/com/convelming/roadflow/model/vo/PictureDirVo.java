package com.convelming.roadflow.model.vo;

import com.convelming.roadflow.model.MapPicture;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.*;

@Data
public class PictureDirVo {
    private String name;
    private String path;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    private List<PictureDirVo> subdir = new ArrayList<>();
    List<MapPicture> pictures = new ArrayList<>();

    public PictureDirVo getSubdirByPath(String path) {
        final PictureDirVo[] pdv = new PictureDirVo[1];
        subdir.forEach(p -> {
            if (p.path.equals(path)) {
                pdv[0] = p;
            }
        });
        return pdv[0];
    }

}
