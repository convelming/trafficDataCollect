package com.convelming.roadflow;

import com.alibaba.fastjson.JSON;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.controller.CrossroadsController;
import com.convelming.roadflow.mapper.CrossroadsMapper;
import com.convelming.roadflow.mapper.CrossroadsStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.model.*;
import com.convelming.roadflow.model.vo.PictureDirVo;
import com.convelming.roadflow.util.FileUtil;
import com.convelming.roadflow.util.IdUtil;
import com.convelming.roadflow.util.VideoUtil;
import com.convelming.roadflow.yolo.Yolo;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.junit.runner.RunWith;
import org.matsim.api.core.v01.Coord;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {

    // AVRO
    private final BigDecimal HOURS = new BigDecimal("3600000");
    @Resource
    private EasyEntityQuery eeq;
    @Resource
    private CrossroadsStatsMapper statsMapper;
    @Resource
    private MatsimLinkMapper linkMapper;
    @Resource
    private MatsimNodeMapper nodeMapper;
    @Resource
    private CrossroadsMapper roadsMapper;
    @Resource
    private IdUtil idUtil;

    public static void main(String[] args) throws Exception {
        OutputStream os = new FileOutputStream("C:\\Users\\zengren\\Desktop\\code.txt");
        output(new File("E:\\project\\convelming\\LinkStats\\src"), os);
        os.close();
    }


    @org.junit.Test
    public void unzip() throws Exception {
        String zip = "C:\\Users\\zengren\\Desktop\\江高茅山河照片.zip";
        String destDir = "C:\\Users\\zengren\\Desktop\\新建文件夹";
        FileUtil.unzip(zip, destDir);
    }

    @org.junit.Test
    public void buildPicTree() throws IOException {
        Map<String, PictureDirVo> dirmap = new HashMap<>();
        List<MapPicture> piclist = eeq.queryable(MapPicture.class).toList();
        l:
        for (MapPicture mp : piclist) {
            File picfile = new File(Constant.DATA_PATH + mp.getPath());
            File parentfile = picfile.getParentFile();
            while (parentfile.list((dir, name) -> name.endsWith(".zip")) == null || parentfile.list((dir, name) -> name.endsWith(".zip")).length == 0) {
                if (parentfile.getName().endsWith("/picture/")) {
                    break l;
                }
                parentfile = parentfile.getParentFile();
            }
            parentfile = parentfile.listFiles((name, dir) -> !dir.endsWith(".zip"))[0];
            PictureDirVo dir = dirmap.get(picfile.getPath());
            if (dir == null) {
                dir = new PictureDirVo();
                dir.setName(parentfile.getName());
                dir.setPath("/" + parentfile.getPath().replace("\\", "/").replace(Constant.DATA_PATH, ""));
                dir.setCreateTime(mp.getCreateTime());
                dirmap.put(dir.getPath(), dir);
            }
        }
        // dirmap 构建子目录
        for (Map.Entry<String, PictureDirVo> entry : dirmap.entrySet()) {
            PictureDirVo root = entry.getValue();
            for (MapPicture mp : piclist) {
                String mpath = mp.getPath();
                if (mpath.startsWith(root.getPath())) {
                    String subpath = mpath.replace(root.getPath(), "");
                    tree(root, subpath, mp);
                }
            }
        }
        System.out.println(dirmap);
    }

    public void tree(PictureDirVo root, String subpath, MapPicture mp) {
        int index = subpath.indexOf("/", 1);
        if (index > 0) {
            String name = subpath.substring(0, index);
            subpath = subpath.substring(index);
            String dirpath = root.getPath() + name;
            PictureDirVo dir = root.getSubdirByPath(dirpath);
            if (dir == null) {
                dir = new PictureDirVo();
                root.getSubdir().add(dir);
            }
            dir.setPath(root.getPath() + name);
            dir.setName(name.replace("/", ""));
            dir.setCreateTime(mp.getCreateTime());
            tree(dir, subpath, mp);
        } else {
            mp.setPath(mp.getPath().replace(root.getPath(), ""));
            root.getPictures().add(mp);
        }
    }

    @org.junit.Test
    public void getPictureInfo() {
        File file = new File("C:\\Users\\zengren\\Desktop\\江高茅山河照片2\\江高茅山河照片\\DJI_20240927105047_0002_Z.jpeg");
        PictureTag ptag = PictureTag.readPicture(file);
        if (ptag == null) {
            System.out.println("获取图片信息出错");
            return;
        }
        System.out.println(ptag);
        System.out.println(ptag.getWidth());
        System.out.println(ptag.getHeight());
        System.out.println(ptag.getLat());
        System.out.println(ptag.getLon());
        System.out.println(ptag.getAltitude());
        System.out.println(ptag.getDateTime());
        System.out.println(ptag.getFileName());
        System.out.println(ptag.getFileSize());
        System.out.println(ptag.getMapDatum());
    }


    public static void output(File file, OutputStream os) throws IOException {
        if (file.isDirectory()) {
            for (File f : Objects.requireNonNull(file.listFiles())) {
                output(f, os);
            }
        } else {
            if (file.getName().endsWith(".java")) {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                String row;
                while ((row = raf.readLine()) != null) {
                    os.write(row.getBytes(StandardCharsets.ISO_8859_1));
                    os.write("\n".getBytes());
                }
                os.write("\n\n".getBytes(StandardCharsets.ISO_8859_1));
                raf.close();
            }
        }
    }

    @org.junit.Test
    public void kdbtest() throws SQLException {
        List<String> maps = eeq.sqlQuery("select st_asewkt(geom3857) from osm_way where id = ?", String.class, List.of("1207443771"));
        PGgeometry pgg = new PGgeometry(maps.get(0));
        System.out.println(pgg.getGeometry());
    }

    @org.junit.Test
    public void runsuccessupdatepcu() {
        Long crossroadsId = 94L;
        Crossroads crossroads = roadsMapper.selectById(crossroadsId);
        // 更新车辆数量
        Map<String, CrossroadsStats> maps = statsMapper.selectByCrossroadsId(crossroadsId).stream().collect(Collectors.toMap(CrossroadsStats::getResultId, x -> x, (a, b) -> a)); // todo 如果有重复去掉
        File results_csv = new File(Constant.DATA_PATH + "/data/" + crossroadsId + "/output_result/results.csv");
        try (RandomAccessFile raf = new RandomAccessFile(results_csv, "rw")) {
            raf.readLine(); // 跳过标题行
            String row;
            while ((row = raf.readLine()) != null) {
                row = new String(row.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                String[] data = row.split(","); //id,car,bus,van,truck
                CrossroadsStats stats = maps.get(data[0]);
                if (stats == null) {
                    continue;
                }
                stats.setCar(Integer.parseInt(data[1]));
                stats.setBus(Integer.parseInt(data[2]));
                stats.setVan(Integer.parseInt(data[3]));
                stats.setTruck(Integer.parseInt(data[4]));
                long time = crossroads.getEndTime().toInstant().getEpochSecond() - crossroads.getBeginTime().toInstant().getEpochSecond();
                stats.setPcuH(calcPcu(stats, time));
                stats.setCount(stats.getCar() + stats.getBus() + stats.getVan() + stats.getTruck());
            }
            log.info("");
//            statsMapper.batchUpdate(maps.values());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @org.junit.Test
    public void YoloRun() {
        Crossroads crossroads = new Crossroads();
        crossroads.setId(11111L);
        boolean bool = Yolo.run(crossroads);
        System.out.println(bool);
    }

    @org.junit.Test
    public void idConcurrent() {
        String table = "crossroads";
        Long id = idUtil.getId(table);
        List<Long> list = new ArrayList<>();
        for (long i = 0; i < 100000000; i++) {
            list.add(i);
        }
        list.parallelStream().forEach(i -> idUtil.getId(table));
        System.out.println(idUtil.getId(table) - id);
    }

    @org.junit.Test
    public void deleteStatus() {
        statsMapper.deleteByCrossroadsId(30L);
    }

    @org.junit.Test
    public void updateStatusPoint() {
        Long crossroadsId = 13L;
        Crossroads roads = roadsMapper.selectById(crossroadsId);
        List<CrossroadsStats> stats = statsMapper.selectByCrossroadsId(crossroadsId);
        List<CrossroadsController.LineBo> lines = JSON.parseArray(roads.getLines(), CrossroadsController.LineBo.class);
        for (CrossroadsStats stat : stats) {
            // 计算交点
            Coord inIntersect = null, outIntersect = null;
            MatsimLink link = linkMapper.selectById(stat.getInLink());
            MatsimNode startNode = nodeMapper.selectById(link.getFromNode());
            MatsimNode endNode = nodeMapper.selectById(link.getToNode());
            for (int i = 0; i < lines.size() && inIntersect == null; i++) {
                CrossroadsController.LineBo line = lines.get(i);
                inIntersect = doIntersect(new Coord[]{new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy())},
                        new Coord[]{new Coord(startNode.getX(), startNode.getY()), new Coord(endNode.getX(), endNode.getY())});
            }
            if (inIntersect != null) {
                stat.setStartPoint(JSON.toJSONString(new double[]{inIntersect.getX(), inIntersect.getY()}));
            }

            link = linkMapper.selectById(stat.getOutLink());
            startNode = nodeMapper.selectById(link.getFromNode());
            endNode = nodeMapper.selectById(link.getToNode());
            for (int i = 0; i < lines.size() && outIntersect == null; i++) {
                CrossroadsController.LineBo line = lines.get(i);
                outIntersect = doIntersect(new Coord[]{new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy())},
                        new Coord[]{new Coord(startNode.getX(), startNode.getY()), new Coord(endNode.getX(), endNode.getY())});
            }
            if (outIntersect != null) {
                stat.setEndPoint(JSON.toJSONString(new double[]{outIntersect.getX(), outIntersect.getY()}));
            }
        }
        statsMapper.batchUpdate(stats);
    }

    @org.junit.Test
    public void outputBezier() {
        int width = 4000;
        int height = 4000;
        Long cossroadsId = 13L;
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
        List<CrossroadsStats> stats = statsMapper.selectByCrossroadsId(cossroadsId);
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
            List<Double> startPoint = JSON.parseArray(stat.getStartPoint(), Double.class);
            MatsimNode ibtn = nodes.get(links.get(stat.getInLink()).getToNode());
            MatsimNode obfn = nodes.get(links.get(stat.getOutLink()).getFromNode());
            List<Double> endPoint = JSON.parseArray(stat.getEndPoint(), Double.class);
            CubicCurve2D.Double bezierCurve = new CubicCurve2D.Double(
                    (startPoint.get(0) - minx) * 10, (maxy - startPoint.get(1)) * 10,
                    (ibtn.getX() - minx) * 10, (maxy - ibtn.getY()) * 10,
                    (obfn.getX() - minx) * 10, (maxy - obfn.getY()) * 10,
                    (endPoint.get(0) - minx) * 10, (maxy - endPoint.get(1)) * 10);
//            CubicCurve2D.Double bezierCurve = new CubicCurve2D.Double(
//                    (obtn.getX() - minx) * 10, (obtn.getY() - miny) * 10, // P0 x,y
//                    (obfn.getX() - minx) * 10, (obfn.getY() - miny) * 10, // P2 x,y
//                    (ibfn.getX() - minx) * 10, (ibfn.getY() - miny) * 10,//
//                    (ibtn.getX() - minx) * 10, (ibtn.getY() - miny) * 10); // P1 x,y
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


//    @Resource
//    private EasyEntityQuery eeq;

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

    private static Coord doIntersect(Coord[] line1, Coord[] line2) {
        double[] p1 = {line1[0].getX(), line1[0].getY()};
        double[] v1 = {line1[1].getX() - line1[0].getX(), line1[1].getY() - line1[0].getY()};
        double[] p2 = {line2[0].getX(), line2[0].getY()};
        double[] v2 = {line2[1].getX() - line2[0].getX(), line2[1].getY() - line2[0].getY()};
        double[] intersection = new double[2];
        // 计算两条线段的方向向量的叉积
        double crossProduct = v1[0] * v2[1] - v1[1] * v2[0];
        // 如果叉积接近于零，则两条线段平行，没有交点
        if (Math.abs(crossProduct) <= 1e-9) {
            intersection[0] = -1.0;
            intersection[1] = -1.0;
        }
        // 计算参数 t1 和 t2
        // 计算参数 t1 和 t2
        double t1 = ((p2[0] - p1[0]) * v2[1] - (p2[1] - p1[1]) * v2[0]) / crossProduct;
        // 计算交点坐标
        intersection[0] = p1[0] + t1 * v1[0];
        intersection[1] = p1[1] + t1 * v1[1];
        if (intersection[0] == -1.0 && intersection[1] == -1.0) {
            // 没有交点
            return null;
        } else {
            return new Coord(intersection);
        }
    }

    private static double calcPcu(CrossroadsStats stats, double second) {
        if (second <= 0) {
            second = 3600L; // 默认算一个小时
        }
//        if (stats.getPcuH() != null && stats.getPcuH() > 0) {
//            return stats.getPcuH();
//        }
        BigDecimal pcuh = new BigDecimal("0");
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getCar()));
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getBus()).multiply(BigDecimal.valueOf(2)));
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getVan()));
        pcuh = pcuh.add(BigDecimal.valueOf(stats.getTruck()).multiply(BigDecimal.valueOf(2)));
        if (pcuh.doubleValue() <= 0 && stats.getPcuH() != null) {
            return stats.getPcuH();
        }
        return pcuh.multiply(BigDecimal.valueOf(3600)).divide(BigDecimal.valueOf(second), RoundingMode.HALF_UP).setScale(2, RoundingMode.DOWN).doubleValue();
//        return pcuh.setScale(2, RoundingMode.DOWN).doubleValue();
    }

    @Data
    private static class PicDir {
        private String name;
        private String path;
        private String createTime;
        private PictureDirVo nextDir;
        List<MapPicture> pictures = new ArrayList<>();
    }

}

