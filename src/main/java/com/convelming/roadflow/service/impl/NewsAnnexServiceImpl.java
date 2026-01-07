package com.convelming.roadflow.service.impl;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.mapper.NewsAnnexMapper;
import com.convelming.roadflow.model.NewsAnnex;
import com.convelming.roadflow.service.NewsAnnexService;
import com.convelming.roadflow.util.FileUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class NewsAnnexServiceImpl implements NewsAnnexService {

    @Resource
    private NewsAnnexMapper newsAnnexMapper;


    @Override
    public NewsAnnex insert(MultipartFile file, long type) {

        String fileName = file.getOriginalFilename();
        long fileSize = file.getSize();

        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) + "/" + System.currentTimeMillis(); // 日期/当前毫秒数
        String path = Constant.VIDEO_PATH + "/" + date + "/" + fileName;
        String dir = Constant.VIDEO_PATH + date + "/";
        new File(dir).mkdirs();
        File out = new File(dir + file.getOriginalFilename());
        try (OutputStream os = new FileOutputStream(out)) {
            InputStream is = file.getInputStream();
            int len;
            byte[] bytes = new byte[1024 * 10];
            while ((len = is.read(bytes)) > 0) {
                os.write(bytes, 0, len);
            }
            os.flush();
            is.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new NewsAnnex();
        }

        NewsAnnex newsAnnex = new NewsAnnex();
        newsAnnex.setName(fileName);
        newsAnnex.setByteSize(fileSize);
        newsAnnex.setPath(path);
        newsAnnex.setSize(FileUtil.formatSize(fileSize));
        newsAnnex.setType(type);

        if (newsAnnexMapper.insert(newsAnnex) > 0) {
            newsAnnex.setUrl(Constant.FILE_DOWNLOAD_API + newsAnnex.getPath());
        }

        return newsAnnex;
    }

    @Override
    public boolean delete(String ids) {
        String[] idsArr = ids.split(",");
        List<Long> idList = new ArrayList<>(idsArr.length);
        for (String id : idsArr) {
            idList.add(Long.parseLong(id));
        }

        return newsAnnexMapper.delete(idList) > 0;
    }

    @Override
    public void batchDownload(String ids, HttpServletResponse response) {
        List<Long> idList = Arrays.stream(ids.split(",")).map(Long::parseLong).toList();
        List<NewsAnnex> list = newsAnnexMapper.selectByIds(idList);
        File[] files = new File[idList.size()];
        for (int i = 0; i < list.size(); i++) {
            files[i] = new File(Constant.VIDEO_PATH + list.get(i).getPath());
        }
        try {
            String fileName = "%E9%99%84%E4%BB%B6.zip"; // 附件.zip
            InputStream inputStream = FileUtil.zip(files);
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            OutputStream os = response.getOutputStream();
            int len;
            byte[] bytes = new byte[1024 * 10];
            while ((len = inputStream.read(bytes)) > 0) {
                os.write(bytes, 0, len);
            }
            response.setHeader("Content-Length", len + "");
            os.flush();
            os.close();
            inputStream.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("打包zip失败");
        }
    }
}
