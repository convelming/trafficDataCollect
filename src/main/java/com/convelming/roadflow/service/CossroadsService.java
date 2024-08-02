package com.convelming.roadflow.service;

import com.convelming.roadflow.controller.CossroadsController;
import com.convelming.roadflow.model.Cossroads;

public interface CossroadsService {

    boolean insert(CossroadsController.CossroadsBo cossroads, double[][] vertex);

}
