package com.convelming.roadflow.model;

import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelTarget("cossroads_status")

@Table("cossroads_status")
@EntityProxy
public class CossroadsStatus {

    @Column(primaryKey = true)
    private Long id;

    private Long cossroadsId;

    private String inLink;

    private String outLink;

    private Double pcuH;

    private String pcuDetail;

    private Double count;

}
