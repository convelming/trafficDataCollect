package com.convelming.roadflow.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/pdf")
public class PDFController {

    // CM 到 Point 的换算系数 (72 points per inch / 2.54 cm per inch)
    private static final float CM_TO_POINT = 72.0f / 2.54f;


    @PostMapping("/watermark")
    public void watermark(MultipartFile pdf, String water, HttpServletResponse response) {
        try {

            // 字体
            PDDocument document = PDDocument.load(pdf.getInputStream());
            PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/font/仿宋_GB2312.ttf"));
            // 遍历PDF中的所有页面
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                addPageWatermark(contentStream, water, font);
                contentStream.close();
            }
            // 设置响应
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(pdf.getOriginalFilename(), StandardCharsets.UTF_8));

            // 输出添加水印后的pdf
            OutputStream outputStream = response.getOutputStream();
            document.save(outputStream);
            outputStream.flush();
            outputStream.close();
            document.close();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException(e);
        }
    }


    public void addPageWatermark(PDPageContentStream contentStream, String water, PDType0Font font) throws IOException {
        contentStream.saveGraphicsState();
        // 设置透明度
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant(0.1f); // 设置填充透明度 (0.0 - 1.0)
        contentStream.setGraphicsStateParameters(gs);

        // 设置字体颜色
        contentStream.setNonStrokingColor(Color.BLACK);

        contentStream.setFont(font, 30);

        // 旋转
        float angleInDegrees = 30;
        float angleInRadians = (float) Math.toRadians(angleInDegrees);
        float cos = (float) Math.cos(angleInRadians);
        float sin = (float) Math.sin(angleInRadians);

        // 变换矩阵: [cos, sin, -sin, cos, 0, 0]
        contentStream.transform(new Matrix(cos, sin, -sin, cos, 0, 0));

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 10; j++) {
                contentStream.beginText();
                // Python: x = (i - 2) * 10 * cm
                float x = (i - 2) * 10 * CM_TO_POINT;
                // Python: y = (j - 5) * 5 * cm
                float y = (j - 5) * 5 * CM_TO_POINT;
                contentStream.newLineAtOffset(x, y);
                contentStream.showText(water);
                contentStream.endText();
            }
        }
    }


}
