package com.convelming.roadflow.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class DataFileTree {

    private String name;
    private String url;
    private String path;
    private List<DataFileTree> children;

}
