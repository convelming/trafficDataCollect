package com.convelming.roadflow.model.vo;

import com.convelming.roadflow.model.Crossroads;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrossroadsVo {

    private Crossroads crossroads;

    private VoideFrameVo frame;

}
