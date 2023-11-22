package com.convelming.roadflow.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Page<T> {

    private List<T> data;

    private Long total;

    private Integer pageNum;

    private Integer pageSize;

    @JsonIgnore
    private Map<String, Object> param = new HashMap<>();

    public Page(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public Page<T> build(List<T> data) {
        this.data = data;
        this.total = (long) data.size();
        return this;
    }

    public Page<T> build(List<T> data, Number total) {
        this.data = data;
        this.total = (long) total;
        return this;
    }

    @JsonIgnore
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }

    public void param(Object[]... params){
        for(Object[] param  : params){
            this.param.put(String.valueOf(param[0]), param[1]);
        }
    }

}
