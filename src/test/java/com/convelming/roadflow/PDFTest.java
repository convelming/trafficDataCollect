package com.convelming.roadflow;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PDFTest {

    private static final float CM_TO_POINT = 72.0f / 2.54f;

//    @Test
    public void watermark() {
        try {
            FileInputStream file = new FileInputStream("C:\\Users\\zengren\\Desktop\\pdf\\FMETransformers.pdf");
            String water = "watermark";
            PDDocument document = PDDocument.load(file);
            // 遍历PDF中的所有页面
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                PDPage page = document.getPage(i);
                PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                addPageWatermark(contentStream, water);
                contentStream.close();
            }
            // 保存修改后的PDF文件
            document.save(new File("C:\\Users\\zengren\\Desktop\\pdf\\output.pdf"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public void addPageWatermark(PDPageContentStream contentStream, String water) throws IOException {
        contentStream.saveGraphicsState();

        // 设置透明度
        PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
        gs.setNonStrokingAlphaConstant(0.1f); // 设置填充透明度 (0.0 - 1.0)
        contentStream.setGraphicsStateParameters(gs);

        // 设置字体颜色
        contentStream.setNonStrokingColor(Color.BLACK);
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 36);

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
