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
    public final static String PICTURE_PATH;

    /**
     * 十字路框选最大面积
     */
    public final static double MAX_AREA = 1000 * 1000.;

    /**
     * 十字路路径最大长度。超过这个长度判定为走不通
     */
    public final static int MAX_DEEP = 50;

    public final static String FILE_DOWNLOAD_API = "/file/download?url=";

    static {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            PICTURE_PATH = "F:/link_stats/picture/";
            VIDEO_PATH = "F:/link_stats/video/";
            DATA_PATH = "F:/link_stats/";
        } else {
            PICTURE_PATH = "/home/link_stats/picture/";
            VIDEO_PATH = "/home/link_stats/video/";
            DATA_PATH = "/home/link_stats/";
        }
    }

}
