package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @PostMapping("/upload")
    public Result upload(MultipartFile file) {

        String name = file.getOriginalFilename();
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String result = "/" + date + "/" + name;
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
            return Result.fail();
        }
        return Result.ok(result);
    }

    @GetMapping("/download")
    public void download(String url, HttpServletResponse response) throws IOException {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        response.addHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName));
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream is = new FileInputStream(Constant.VIDEO_PATH + "/" + url)
        ) {
            int len;
            byte[] b = new byte[1024 * 10];
            while ((len = is.read(b)) > 0) {
                os.write(b, 0, len);
            }
            os.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
