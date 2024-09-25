package com.convelming.roadflow.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LinkStatsType {

    /**
     * 人工
     */
    ARTIFICIAL("人工", "1"),

    /**
     * 视频识别
     */
    VIDEO_RECOGNITION("视频识别", "2"),
    GD_CRAWL("高德爬取的数据", "3"),

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
