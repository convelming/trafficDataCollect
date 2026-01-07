package com.convelming.roadflow.service;

import com.convelming.roadflow.model.NewsAnnex;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface NewsAnnexService {

    NewsAnnex insert(MultipartFile file, long type);

    boolean delete(String ids);

    void batchDownload(String ids, HttpServletResponse response);
}
