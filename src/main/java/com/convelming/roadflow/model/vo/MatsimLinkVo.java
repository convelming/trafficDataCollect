package com.convelming.roadflow.model.vo;

import com.convelming.roadflow.model.MatsimLink;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatsimLinkVo {

    private List<MatsimLink> to;

    private List<MatsimLink> from;


//    List<MatsimLink> toPath;
//
//    List<MatsimLink> fromPath;


}
