package com.convelming.roadflow.enums;

public enum NetworkSource {

    SWTX, OSM, DX;


    public static NetworkSource networkSource(String url) {
        if (url.contains("OSM")) {
            return OSM;
        } else if (url.contains("四维图新")) {
            return SWTX;
        } else if (url.contains("单线路网")) {
            return DX;
        }
        throw new RuntimeException("未适配的路网来源");
    }



}
