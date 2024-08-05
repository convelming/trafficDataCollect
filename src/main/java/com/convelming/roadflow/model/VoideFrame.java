package com.convelming.roadflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoideFrame {

    /**
     * 地址
     */
    private String url;

    /**
     * 图片名
     */
    private String ame;

    /**
     * 宽
     */
    private Integer width;

    /**
     * 高
     */
    private Integer height;

}
