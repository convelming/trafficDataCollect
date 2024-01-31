package com.convelming.roadflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum LinkStatsType {

    /**
     * 人工
     */
    ARTIFICIAL("人工", "1"),

    /**
     * 视频识别
     */
    VIDEO_RECOGNITION("视频识别", "2"),
    GD_CRAWL("高德地图爬取", "3"),

    /**
     * 其他
     */
    OTHER("其他", "0");

    private final String name;

    private final String value;

    public static LinkStatsType getOfValue(String value){

        for(LinkStatsType type : LinkStatsType.values()){
            if(Objects.equals(type.getValue(), value)){
                return type;
            }
        }

        throw new RuntimeException("找不到枚举值: " + value);
    }

}
