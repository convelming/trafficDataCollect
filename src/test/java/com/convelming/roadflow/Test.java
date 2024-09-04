package com.convelming.roadflow;

import com.convelming.roadflow.mapper.CrossroadsStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.util.VideoUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {

    // AVRO
    private final BigDecimal HOURS = new BigDecimal("3600000");

    @Resource
    private CrossroadsStatsMapper statsMapper;
    @Resource
    private MatsimLinkMapper linkMapper;
    @Resource
    private MatsimNodeMapper nodeMapper;

    @org.junit.Test
    public void outputBezier() {
        int width = 2000;
        int height = 2000;
        Long cossroadsId = 2L;
        // 创建一个类型为预定义图像类型之一的 BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文以于绘制
        Graphics2D g2d = image.createGraphics();
//        CubicCurve2D.Double bezierCurve = new CubicCurve2D.Double(
//                10.0, 10.0, // P0 x,y
//                100.0, 10.0, // P1 x,y
//                100.0, 100.0, // P2 x,y
//                10.0, 100.0); // P3 x,y
//        g2d.draw(bezierCurve);
        List<CrossroadsStats> stats = statsMapper.selectByCossroadsId(cossroadsId);
        List<String> linkIds = new ArrayList<>();
        linkIds.addAll(stats.stream().map(CrossroadsStats::getInLink).toList());
        linkIds.addAll(stats.stream().map(CrossroadsStats::getOutLink).toList());
        Map<String, MatsimLink> links = linkMapper.selectInId(linkIds).stream().collect(Collectors.toMap(MatsimLink::getId, x -> x));
        List<String> nodeIds = new ArrayList<>();
        nodeIds.addAll(links.values().stream().map(MatsimLink::getFromNode).toList());
        nodeIds.addAll(links.values().stream().map(MatsimLink::getToNode).toList());
        Map<String, MatsimNode> nodes = nodeMapper.selectInId(nodeIds).stream().collect(Collectors.toMap(MatsimNode::getId, x -> x));
        double minx = nodes.values().stream().mapToDouble(MatsimNode::getX).min().orElse(0) - 5;
        double maxy = nodes.values().stream().mapToDouble(MatsimNode::getY).max().orElse(0) + 5;
        for (CrossroadsStats stat : stats) {
            // 获取from to link 位置
            MatsimNode ibfn = nodes.get(links.get(stat.getInLink()).getFromNode());
            MatsimNode ibtn = nodes.get(links.get(stat.getInLink()).getToNode());
            MatsimNode obfn = nodes.get(links.get(stat.getOutLink()).getFromNode());
            MatsimNode obtn = nodes.get(links.get(stat.getOutLink()).getToNode());
            CubicCurve2D.Double bezierCurve = new CubicCurve2D.Double(
                    (ibfn.getX() - minx) * 10, (maxy - ibfn.getY()) * 10,
                    (ibtn.getX() - minx) * 10, (maxy - ibtn.getY()) * 10,
                    (obfn.getX() - minx) * 10, (maxy - obfn.getY()) * 10,
                    (obtn.getX() - minx) * 10, (maxy - obtn.getY()) * 10);
//            CubicCurve2D.Double bezierCurve = new CubicCurve2D.Double(
//                    (obtn.getX() - minx) * 10, (obtn.getY() - miny) * 10, // P0 x,y
//                    (obfn.getX() - minx) * 10, (obfn.getY() - miny) * 10, // P2 x,y
//                    (ibfn.getX() - minx) * 10, (ibfn.getY() - miny) * 10,//
//                    (ibtn.getX() - minx) * 10, (ibtn.getY() - miny) * 10); // P1 x,y
            if ("703270".equals(stat.getInLink())) {
            }
            g2d.setColor(Color.WHITE);
            g2d.draw(bezierCurve);

        }

        try {
            File outputFile = new File("output.png");
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        // 使用 Graphics2D 进行绘制（例如：绘制一个填充的矩形）
        // g2d.setColor(Color.GREEN);
        // g2d.fillRect(0, 0, width, height);
        // g2d.dispose(); // 释放图形上下文

        // 将图片保存到文件
//        try {
//            File outputFile = new File("output.png");
//            ImageIO.write(image, "png", outputFile);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @org.junit.Test
    public void videoFrame() throws IOException {
        long time = System.currentTimeMillis();
        VideoUtil.saveImage("F:\\流量视频\\IMG_7802.MOV", "F:\\流量视频\\IMG_7802.MOV.jpg", VideoUtil.ImageType.JPG);
        System.out.println("time: " + (System.currentTimeMillis() - time));
    }

    @org.junit.Test
    public void pcu() {
        BigDecimal pcu = BigDecimal.ZERO;
        pcu = pcu.add(new BigDecimal(399)).add(new BigDecimal(1));
        pcu = pcu.add(new BigDecimal(2).multiply(new BigDecimal("1.5"))).add(new BigDecimal(5).multiply(new BigDecimal("1.5")));
        pcu = pcu.add(new BigDecimal(13).multiply(new BigDecimal("2"))).add(new BigDecimal(7).multiply(new BigDecimal("2")));
        pcu = pcu.divide(new BigDecimal(15 * 60000), 64, RoundingMode.UP).multiply(HOURS);

        BigDecimal d = new BigDecimal("2.3656");
        System.out.println(d.setScale(2, RoundingMode.DOWN));

        System.out.println(pcu.doubleValue());
    }


    @Resource
    private EasyEntityQuery eeq;

    @org.junit.Test
    public void easyQuery() {
//        String sql = "select id, origid from " + TABLE_NAME + " where id||'' like ? order by id limit 1000";
//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MatsimLink.class), "%" + id + "%");
        String keyword = "22087";
        List<MatsimLink> list = eeq.queryable(MatsimLink.class).where(t -> {
            t.id().like(keyword);
        }).toList();
        System.out.println(list);
    }

}
