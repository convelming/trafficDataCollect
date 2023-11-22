package com.convelming.roadflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HighwayType {

    /**
     * 高速公路
     */
    MOTORWAY("motorway", "高速公路", "高速公路，过江隧道", "高架及快速路"),

    /**
     * 高速公路-连接
     */
    MOTORWAY_LINK("motorway_link", "高速公路-连接", "高速公路立交，匝道", "高架及快速路"),

    /**
     * 干道
     */
    TRUNK("trunk", "干道", "高架快速路，机场进站快速路，过江隧道，桥上快速路", "高架及快速路"),

    /**
     * 干道-连接
     */
    TRUNK_LINK("trunk_link", "干道-连接", "立交，匝道，桥上引道，机场进站快速路，国道改道", "高架及快速路"),

    /**
     * 主要道路
     */
    PRIMARY("primary", "主要道路", "城市主要车行道路", "城市主干路"),

    /**
     * 主要道路-连接
     */
    PRIMARY_LINK("primary_link", "主要道路-连接", "城市主要车行道路立交，城市主要车行道路匝道(数据量少，部分零散分布未知类别)", "城市主干路"),

    /**
     * 次要道路
     */
    SECONDARY("secondary", "次要道路", "城市次要车行道路，机场外围车行道路", "城市主干路"),

    /**
     * 次要道路_连接
     */
    SECONDARY_LINK("secondary_link", "次要道路_连接", "城市次要车行道路立交、匝道(数据量少，部分零散分布未知类别)", "城市主干路"),

    /**
     * 第三级道路
     */
    TERTIARY("tertiary", "第三级道路", "城市支路", "城市次干路"),

    /**
     * 第三级道路_连接
     */
    TERTIARY_LINK("tertiary_link", "第三级道路_连接", "匝道，机场集散车行道路(数据量极少，多是未知道路)", "城市次干路"),

    /**
     * 居住区道路
     */
    RESIDENTIAL("residential", "居住区道路", "居住区车行道路", "城市支路"),

    /**
     * 未分类道路
     */
    UNCLASSIFIED("unclassified", "未分类道路", "居住区车行道路，滨水车行道路，机场机动车通道", "城市支路"),

    /**
     * 小路
     */
    TRACK("track", "小路", "郊区、乡村、工矿区、田间、林间小路", "郊区乡村道路"),

    /**
     * 小路级别1
     */
    TRACK_GRADE1("track_grade1", "小路级别1", "郊区、乡村、工矿区、田间、林间小路", "郊区乡村道路"),

    /**
     * 小路级别2
     */
    TRACK_GRADE2("track_grade2", "小路级别2", "郊区、乡村、工矿区、田间、林间小路", "郊区乡村道路"),

    /**
     * 小路级别3
     */
    TRACK_GRADE3("track_grade3", "小路级别3", "郊区、乡村、工矿区、田间、林间小路", "郊区乡村道路"),

    /**
     * 小路级别4
     */
    TRACK_GRADE4("track_grade4", "小路级别4", "郊区、乡村、工矿区、田间、林间小路", "郊区乡村道路"),

    /**
     * 小路级别5
     */
    TRACK_GRADE5("track_grade5", "小路级别5", "郊区、乡村、工矿区、田间、林间小路", "郊区乡村道路"),

    /**
     * 马道
     */
    BRIDLEWAY("bridleway", "马道", "体育场馆内部专用道路(数据量极少，零星道路在公园、居住区内部)", "内部道路"),

    /**
     * 生活街道
     */
    LIVING_STREET("living_street", "生活街道", "居住区车行道路，公园车行道路", "内部道路"),

    /**
     * 小道
     */
    PATH("path", "小道", "公园车行道路，居住区车行道路 (分布零碎，量少)", "内部道路"),

    /**
     * 服务性道路
     */
    SERVICE("service", "服务性道路", "居住区车行道路，火车站集散车行道，公园车行道路，公共建筑集散车行道，公交枢纽入口车行道路，停车场入口车行道路", "内部道路"),

    /**
     * 人行道
     */
    FOOTWAY("footway", "人行道", "滨水绿道，公园步行道，广场步行道，大学步行道路，人行道，火车站人行集散道路", "人行道路"),

    /**
     * 步行街道
     */
    PEDESTRIAN("pedestrian", "步行街道", "步行街，广场步行道路，公园步行道路居住区步行道路", "人行道路"),

    /**
     * 台阶踏步
     */
    STEPS("steps", "台阶踏步", "人行过街天桥台阶，广场台阶、公共建筑入口台阶，登山台阶", "人行道路"),

    /**
     * 自行车道
     */
    CYCLEWAY("cycleway", "自行车道", "滨水绿道，非机动车道，公园自行车道", "自行车道"),

    /**
     * 未知道路
     */
    UNKNOWN("unknown", "未知道路", "滨水车行道路，校园广场车行道路，乡道(数据量少，比较难判别道路类型)", "其它"),

    ;

    /**
     * 编码
     */
    private final String code;

    /**
     * 名称
     */
    private final String name;

    /**
     * 描述
     */
    private final String desc;

    /**
     * 分类
     */
    private final String classify;


    public static HighwayType getOfCode(String code){
        for(HighwayType highwayType : HighwayType.values()){
            if(highwayType.getCode().equals(code)){
                return highwayType;
            }
        }
        throw new RuntimeException("找不到枚举值: " + code);
    }

}
