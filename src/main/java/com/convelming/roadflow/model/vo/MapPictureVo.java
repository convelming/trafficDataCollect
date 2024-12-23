package com.convelming.roadflow.model.vo;

import com.convelming.roadflow.model.MapPicture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapPictureVo {

    private long id;
    private String name;
    private String url;
    private double x;
    private double y;

    public MapPictureVo(MapPicture mp) {
        this.id = mp.getId();
        this.name = mp.getName();
        this.url = mp.getUrl();
        this.x = mp.getX();
        this.y = mp.getY();
    }

}
