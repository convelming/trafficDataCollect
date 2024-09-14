package com.convelming.roadflow.common;

public class Constant {

    /**
     * 数据目录
     * ./data 视频识别输入输出目录
     * ./video 视频上传目录
     * ./code 视频识别python代码目录
     */
    public final static String DATA_PATH;
    public final static String VIDEO_PATH;

    /**
     * 十字路框选最大面积
     */
    public final static double MAX_AREA = 1000 * 1000.;

    /**
     * 十字路路径最大长度。超过这个长度判定为走不通
     */
    public final static int MAX_DEEP = 50;

    static {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            VIDEO_PATH = "F:/link_stats/video/";
            DATA_PATH = "F:/link_stats/";
        } else {
            VIDEO_PATH = "/home/link_stats/video/";
            DATA_PATH = "/home/link_stats/";
        }
    }

}
