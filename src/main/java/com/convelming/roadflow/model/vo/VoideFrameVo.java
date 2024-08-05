package com.convelming.roadflow.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoideFrameVo {

    /**
     * 地址
     */
    private String url;

    /**
     * 图片名
     */
    private String name;

    /**
     * 宽
     */
    private Integer width;

    /**
     * 高
     */
    private Integer height;

}
