package com.convelming.roadflow.common;

public class Constant {

    public final static String DATA_PATH;
    public final static String VIDEO_PATH;

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
