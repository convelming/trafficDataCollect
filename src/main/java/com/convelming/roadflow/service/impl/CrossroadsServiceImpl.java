package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson.JSON;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.common.Page;
import com.convelming.roadflow.controller.CrossroadsController;
import com.convelming.roadflow.mapper.*;
import com.convelming.roadflow.model.Crossroads;
import com.convelming.roadflow.model.CrossroadsStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.model.vo.CrossroadsVo;
import com.convelming.roadflow.model.vo.VoideFrameVo;
import com.convelming.roadflow.service.CrossroadsService;
import com.convelming.roadflow.util.GeomUtil;
import com.convelming.roadflow.util.VideoUtil;
import com.convelming.roadflow.yolo.Yolo;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CrossroadsServiceImpl implements CrossroadsService {

    @Resource
    private HttpServletRequest request;

    @Resource
    private CrossroadsMapper mapper;

    @Resource
    private CrossroadsStatsMapper statsMapper;

    @Resource
    private IntersectionMapper intersectionMapper;

    @Resource
    private MatsimLinkMapper linkMapper;

    @Resource
    private MatsimNodeMapper nodeMapper;

    @Override
    public Page<Crossroads> list(Page<Crossroads> page) {
        return mapper.list(page);
    }

    @Override
    public Crossroads insert(CrossroadsController.CossroadsBo bo) {

        Crossroads crossroads = new Crossroads();
        crossroads.setVideo(bo.getVideo());
        crossroads.setType(bo.getType());
        crossroads.setRemark(bo.getRemark());
        crossroads.setVideoType(bo.getVideoType());
        crossroads.setIpAddr(request.getRemoteAddr());
        crossroads.setMapInfo(bo.getMapInfo());
        crossroads.setIntersectionId(bo.getIntersectionId());
        if (bo.getAnnex() != null) {
            crossroads.setAnnex(JSON.toJSONString(bo.getAnnex()));
        }
        crossroads.setBeginTime(bo.getBeginTime());
        crossroads.setEndTime(bo.getEndTime());
//        PGgeometry polygon = GeomUtil.genPolygon(vertex, GeomUtil.MKT);
//        List<MatsimLink> links = linkMapper.selectIntersects(polygon);
//        crossroads.setVertex(JSON.toJSONString(vertex));
//        crossroads.setCenter(JSON.toJSONString(bo.getCenter()));
//        crossroads.setInLinkId(JSON.toJSONString(links.stream().map(MatsimLink::getId).toList()));
        if (mapper.insert(crossroads)) {
            long count = mapper.countByIntersectionId(crossroads.getIntersectionId());
            intersectionMapper.updateStatus(crossroads.getIntersectionId(), count > 0 ? 1 : 0);
        }
        return crossroads;
    }

    @Override
    public Crossroads updateById(Crossroads crossroads) {
        Crossroads targ = mapper.selectById(crossroads.getId());
        if (targ == null) {
            return null;
        }
        double oldTime = targ.getEndTime().toInstant().getEpochSecond() - targ.getBeginTime().toInstant().getEpochSecond();
        crossroads.setDeleted(targ.getDeleted());
        crossroads.setVersion(targ.getVersion() + 1);
        crossroads.setUpdateTime(new Date());
        crossroads.setIpAddr(request.getRemoteAddr());
        if (mapper.updateById(crossroads)) { // 更新pcuh
            double time = crossroads.getEndTime().toInstant().getEpochSecond() - crossroads.getBeginTime().toInstant().getEpochSecond();
            List<CrossroadsStats> stats = statsMapper.selectByCrossroadsId(crossroads.getId());
            stats.forEach(stat -> {
                if (oldTime != time) {
                    stat.setPcuH(calcPcu(stat, time));
                }
            });
            statsMapper.batchUpdate(stats);
        }
        return targ;
    }

    @Override
    public CrossroadsVo detail(Long crossroadsId) {
        Crossroads crossroads = mapper.selectById(crossroadsId);
        VoideFrameVo frame = frame(crossroadsId);
        return new CrossroadsVo(crossroads, frame);
    }

    @Override
    public VoideFrameVo frame(Long crossroadsId) {
        Crossroads crossroads = mapper.selectById(crossroadsId);
        if (crossroads == null || !crossroads.getType().equals("2")) {
            return null;
        }
        String video = Constant.VIDEO_PATH + crossroads.getVideo();
        File vf = new File(video);
        if (!vf.exists() || !vf.isFile()) {
            return null;
        }

        String toimage = video + "_0.jpg";
        File tif = new File(toimage);
        if (!tif.exists()) {
            VideoUtil.saveImage(video, toimage, VideoUtil.ImageType.JPG);
        }
        int[] wh = VideoUtil.widthight(toimage);
        VoideFrameVo vo = new VoideFrameVo();
        vo.setUrl("/file/download?url=" + toimage.replace(Constant.VIDEO_PATH, ""));
        vo.setName(toimage.substring(toimage.lastIndexOf("/") + 1));
        vo.setWidth(wh[0]);
        vo.setHeight(wh[1]);
//        vo.setCenter(JSON.parseArray(crossroads.getCenter(), BigDecimal.class).stream().mapToDouble(BigDecimal::doubleValue).toArray());
        return vo;
    }

    @Override
    public boolean deleteByIds(String[] crossroadId) {
        Long[] ids = new Long[crossroadId.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Long.parseLong(crossroadId[i]);
        }
        Crossroads crossroads = mapper.selectById(ids[0]);
        if (mapper.deleteByIds(ids)) {
            long count = mapper.countByIntersectionId(crossroads.getIntersectionId());
            intersectionMapper.updateStatus(crossroads.getIntersectionId(), count > 0 ? 1 : 0);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean saveline(CrossroadsController.CrossroadsLineBo bo) {
        Crossroads crossroads = mapper.selectById(bo.getCossroadsId());
        if (crossroads.getLines() != null) {
            crossroads.setVersion(crossroads.getVersion() + 1);
        }
        if (!crossroads.getType().equals("1")) {
            crossroads.setStatus(1); // 设置状态已绘制检测线
        } else {
            crossroads.setStatus(5); // 非视频设置等待录入
        }
        crossroads.setUpdateTime(new Date());
        crossroads.setVertex(JSON.toJSONString(bo.getVertex()));
        crossroads.setLines(JSON.toJSONString(bo.getLines()));
        crossroads.setMapInfo(bo.getMapInfo());

        PGgeometry polygon = GeomUtil.genPolygon(bo.getVertex(), GeomUtil.MKT);
        if (GeomUtil.getArea(polygon) > Constant.MAX_AREA) {
            throw new RuntimeException("范围过大，请缩小范围再提交");
        }
        double[] center = GeomUtil.getCentroid(polygon);
        Coord centerCoord = new Coord(center);
//        crossroads.setCenter(JSON.toJSONString(center));
        List<MatsimLink> links = linkMapper.selectIntersects(polygon);
        List<String> nodeIds = new ArrayList<>(links.stream().map(MatsimLink::getToNode).toList());
        nodeIds.addAll(links.stream().map(MatsimLink::getFromNode).toList());
        List<MatsimNode> nodes = nodeMapper.selectByIds(nodeIds);

        // 圈出来的路网
        Network miniNetWork = NetworkUtils.createNetwork();
        nodes.forEach(node -> miniNetWork.addNode(NetworkUtils.createNode(Id.createNodeId(node.getId()), new Coord(node.getX(), node.getY()))));
        links.forEach(link -> miniNetWork.addLink(NetworkUtils.createLink(
                Id.createLinkId(link.getId()),
                miniNetWork.getNodes().get(Id.createNodeId(link.getFromNode())),
                miniNetWork.getNodes().get(Id.createNodeId(link.getToNode())),
                miniNetWork, link.getLength(), link.getFreespeed(), link.getCapacity(), link.getLane()
        )));
//        List<Link> ins = new ArrayList<>(), outs = new ArrayList<>(); // 进入、离开框选范围的link
        List<Link> intersectIns = new ArrayList<>(), intersectOuts = new ArrayList<>(); // 绘制线相交的in, out
        List<MatsimLink> lineIntersect = new ArrayList<>(); // 与绘制线相交的link
        for (CrossroadsController.LineBo line : bo.getLines()) {
            List<MatsimLink> intersectsLinks = linkMapper.selectIntersects(GeomUtil.genLine(new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy()), 3857));
            intersectsLinks.forEach(link -> link.setLineName(line.getLineName()));
            lineIntersect.addAll(intersectsLinks);
        }
        lineIntersect.sort((a, b) ->
                (int) (calcDistance3857(miniNetWork.getLinks().get(Id.createLinkId(a.getId())).getToNode().getCoord(), centerCoord)
                        - calcDistance3857(miniNetWork.getLinks().get(Id.createLinkId(b.getId())).getToNode().getCoord(), centerCoord)));

        Map<String, MatsimLink> linkMap = lineIntersect.stream().collect(Collectors.toMap(MatsimLink::getId, x -> x, (a, b) -> a));

//        miniNetWork.getLinks().forEach(((id, link) -> {
//            // 十字路所有进出口
//            if (!LineUtil.crossJudgment(bo.getVertex(), new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()})) { // 边界相交
////                    && !LineUtil.isPointOnline(new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()}, bo.getLines())) { // 绘制线相交
////            if (!LineUtil.isPointOnline(new Coord[]{link.getFromNode().getCoord(), link.getToNode().getCoord()}, bo.getLines())) { // 绘制线相交
//                return;
//            }
//            if (link.getFromNode().getInLinks().isEmpty()) {
//                ins.add(link);
//            }
//            if (link.getToNode().getOutLinks().isEmpty()) {
//                outs.add(link);
//            }
//        }));

        Table<String, Id<Link>, Coord> pointIntersect = HashBasedTable.create();
//        for (MatsimLink link : lineIntersect) {
//            Link l = miniNetWork.getLinks().get(Id.createLinkId(link.getId()));
//            if (l == null) {
//                continue;
//            }
//            double todist = calcDistance3857(l.getToNode().getCoord(), centerCoord);
//            double fromdist = calcDistance3857(l.getFromNode().getCoord(), centerCoord);
//
//            // 计算交点
//            Coord intersect = null;
//            for (int i = 0; i < bo.getLines().size() && intersect == null; i++) {
//                CrossroadsController.LineBo line = bo.getLines().get(i);
//                intersect = doIntersect(new Coord[]{new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy())},
//                        new Coord[]{l.getFromNode().getCoord(), l.getToNode().getCoord()});
//            }
//
//            if (todist > fromdist) {
//                intersectOuts.add(l);
//                pointIntersect.put("out", l.getId(), intersect);
//            } else {
//                intersectIns.add(l);
//                pointIntersect.put("in", l.getId(), intersect);
//            }
//        }

        for (MatsimLink ml1 : lineIntersect) {
            Link l1 = miniNetWork.getLinks().get(Id.createLinkId(ml1.getId()));
            if (l1 == null) {
                continue;
            }
            for (MatsimLink ml2 : lineIntersect) {
                Link l2 = miniNetWork.getLinks().get(Id.createLinkId(ml2.getId()));
                if (l2 == null || l1.getId().equals(l2.getId())) {
                    continue;
                }
                if (calcRouteAccessible(l1, l2, new Stack<>())) {
                    boolean turn = false;
                    for (Link temp : intersectIns) {
                        if (temp.getFromNode().equals(l1.getToNode()) && temp.getToNode().equals(l1.getFromNode())) {
                            turn = true;
                        }
                    }
                    if (intersectIns.stream().noneMatch(link -> l1.getId().equals(link.getId())) && !turn) {
                        Coord intersect = null;
                        for (int i = 0; i < bo.getLines().size() && intersect == null; i++) {
                            CrossroadsController.LineBo line = bo.getLines().get(i);
                            intersect = doIntersect(new Coord[]{new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy())},
                                    new Coord[]{l1.getFromNode().getCoord(), l1.getToNode().getCoord()});
                        }
                        intersectIns.add(l1);
                        pointIntersect.put("in", l1.getId(), intersect);
                    }
                }
                if (intersectOuts.stream().noneMatch(link -> l2.getId().equals(link.getId()))) {
                    Coord intersect = null;
                    for (int i = 0; i < bo.getLines().size() && intersect == null; i++) {
                        CrossroadsController.LineBo line = bo.getLines().get(i);
                        intersect = doIntersect(new Coord[]{new Coord(line.getMktBeginx(), line.getMktBeginy()), new Coord(line.getMktEndx(), line.getMktEndy())},
                                new Coord[]{l2.getFromNode().getCoord(), l2.getToNode().getCoord()});
                    }
                    intersectOuts.add(l2);
                    pointIntersect.put("out", l2.getId(), intersect);
                }
            }
        }

        // 每个进口 * 出口构成一个流量数据 129171
        List<CrossroadsStats> stats = new ArrayList<>();
        for (Link in : intersectIns) {
            MatsimLink inLink = linkMap.get(in.getId().toString());
            for (Link out : intersectOuts) {
                // in -> out 能在mininetwork中走通才添加
                if (in.getId().equals(out.getId())) {
                    continue; // 如果起点和终点是同一段link跳过
                }
                Stack<Id<Link>> stack = new Stack<>();
                stack.push(in.getId());
                if (calcRouteAccessible(in, out, stack)) {
                    MatsimLink outLink = linkMap.get(out.getId().toString());
//                    log.info("{}->{}:{}", inLink.getLineName(), outLink.getLineName(), stack);
                    CrossroadsStats crossroadsStats = new CrossroadsStats();
                    crossroadsStats.setCrossroadsId(bo.getCossroadsId());
                    crossroadsStats.setOutLink(outLink.getId());
                    crossroadsStats.setInLink(inLink.getId());
//                    crossroadsStats.setPcuDetail(CrossroadsStats.DEFAULT_DETAIL); // 默认小中大客/货车
                    crossroadsStats.setResultId(inLink.getLineName() + outLink.getLineName());
                    crossroadsStats.setName(crossroadsStats.getResultId());
                    crossroadsStats.setInLine(inLink.getLineName());
                    crossroadsStats.setOutLine(outLink.getLineName());
                    crossroadsStats.setName(crossroadsStats.getResultId());
                    // 绘制线与link交点作为贝塞尔曲线的起点与终点
                    // in 为起点，out为终点
                    Coord startCoord = pointIntersect.get("in", in.getId());
                    if (startCoord != null) {
                        crossroadsStats.setStartPoint(JSON.toJSONString(new double[]{startCoord.getX(), startCoord.getY()}));
                    }
                    Coord endCoord = pointIntersect.get("out", out.getId());
                    if (endCoord != null) {
                        crossroadsStats.setEndPoint(JSON.toJSONString(new double[]{endCoord.getX(), endCoord.getY()}));
                    }
                    stats.add(crossroadsStats);
                }
            }
        }
        // 新增全部流量
        statsMapper.deleteByCrossroadsId(crossroads.getId()); // 调整位置之后把之前新增全部删除
        statsMapper.batchInsert(stats);
//
        return mapper.saveLines(crossroads) > 0;
    }

    @Override
    public List<CrossroadsStats> corssStatsTable(Long crossroadsId) {
        return statsMapper.selectByCrossroadsId(crossroadsId);
    }

    @Override
    public boolean deleteStats(String[] crossroadStatsId) {
        Long[] ids = new Long[crossroadStatsId.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = Long.parseLong(crossroadStatsId[i]);
        }
        return statsMapper.deleteByIds(ids);
    }

    @Override
    public boolean insertStats(CrossroadsStats stats) {

        if (mapper.selectById(stats.getCrossroadsId()) == null) {
            throw new RuntimeException("十字路不存在");
        }

        if (statsMapper.countCrossroadsInOrOutLink(stats.getCrossroadsId(), stats.getInLink(), stats.getOutLink()) <= 0) {
            throw new RuntimeException("所选link不在十字路范围内");
        }

        if (statsMapper.countCrossroadsInOutLink(stats.getCrossroadsId(), stats.getInLink(), stats.getOutLink()) > 0) {
            throw new RuntimeException("该进出link已存在");
        }

        // 计算pcu/h
//        if (stats.getPcuDetail() != null && !stats.getPcuDetail().isEmpty()) {
//            BigDecimal pcuh = new BigDecimal("0");
//            JSONArray pcus = JSONArray.parse(stats.getPcuDetail());
//            for (Object obj : pcus) {
//                JSONObject pcu = (JSONObject) obj;
//                Integer num = pcu.getInteger(CrossroadsStats.DETAIL_NUM);
//                BigDecimal ratio = pcu.getBigDecimal(CrossroadsStats.DETAIL_RATIO);
//                pcuh = pcuh.add(ratio.multiply(BigDecimal.valueOf(num)));
//            }
//            stats.setPcuH(pcuh.doubleValue());
//        }
        Crossroads crossroads = mapper.selectById(stats.getCrossroadsId());
        long time = 3600;
        if (crossroads != null) {
            time = crossroads.getEndTime().toInstant().getEpochSecond() - crossroads.getBeginTime().toInstant().getEpochSecond();
        }
        stats.setPcuH(calcPcu(stats, time));

        return statsMapper.insert(stats);
    }

    @Override
    public boolean updateStats(CrossroadsStats stats) {
        // 计算pcu/h
//        if (stats.getPcuDetail() != null && !stats.getPcuDetail().isEmpty()) {
//            BigDecimal pcuh = new BigDecimal("0");
//            JSONArray pcus = JSONArray.parse(stats.getPcuDetail());
//            for (Object obj : pcus) {
//                JSONObject pcu = (JSONObject) obj;
//                Integer num = pcu.getInteger(CrossroadsStats.DETAIL_NUM);
//                BigDecimal ratio = pcu.getBigDecimal(CrossroadsStats.DETAIL_RATIO);
//                pcuh = pcuh.add(ratio.multiply(BigDecimal.valueOf(num)));
//            }
//            stats.setPcuH(pcuh.doubleValue());
//        }

        Crossroads crossroads = mapper.selectById(stats.getCrossroadsId());
        long time = 3600;
        if (crossroads != null) {
            if (!"2".equals(crossroads.getType())) {
                mapper.updateStatus(crossroads.getId(), 6);
            }
            time = crossroads.getEndTime().toInstant().getEpochSecond() - crossroads.getBeginTime().toInstant().getEpochSecond();
        }
        stats.setPcuH(calcPcu(stats, time));
        stats.setDeleted(0L);
        return statsMapper.updateById(stats);
    }

    @Override
    public Map<String, Collection<String>> inoutlink(Long crossroadsId) {
        List<CrossroadsStats> stats = statsMapper.selectByCrossroadsId(crossroadsId);
        return new HashMap<>() {{
            put("inlink", stats.stream().map(CrossroadsStats::getInLink).collect(Collectors.toSet()));
            put("outlink", stats.stream().map(CrossroadsStats::getOutLink).collect(Collectors.toSet()));
        }};
    }

    @Override
    public boolean runVehicleCounts(Long crossroadsId) {
        Crossroads crossroads = mapper.selectById(crossroadsId);
        if (!crossroads.getType().equals("2")) { // 只有视频录入才支持视频识别
            throw new RuntimeException("运行失败，只有视频录入才支持视频识别");
        }
        if (crossroads.getVideo() == null) {
            throw new RuntimeException("运行失败，未上传视频");
        }
        if (crossroads.getStatus() == 0) {
            throw new RuntimeException("运行失败，未绘制检测线");
        }
        if (crossroads.getStatus() == 2) {
            throw new RuntimeException("正在运行，不能重复运行");
        }
        if (crossroads.getStatus() == 3) {
            throw new RuntimeException("已运行，不能重复运行");
        }
        if (crossroads.getStatus() == 1 || crossroads.getStatus() == 4) {
            mapper.updateStatus(crossroadsId, 2);
            new Thread(() -> {
                boolean result = Yolo.run(crossroads);
                if (result) { // 成功运行
                    mapper.updateStatus(crossroadsId, 3); // 运行成功
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
                        statsMapper.batchUpdate(maps.values());
                    } catch (IOException e) {
                        log.error("读取results.csv失败");
                    }
                    log.info("运行视频识别成功");
                } else {
                    mapper.updateStatus(crossroadsId, 4); // 运行失败
                    log.error("运行视频识别失败");
                }
            }).start();
        }
        return true;
    }

    @Override
    public void analyzeVideo(Long crossroadsId, HttpServletResponse response) {
        //

    }

    @Override
    public void statusFlowImage(Long crossroadsId, HttpServletResponse response) {

    }

    /**
     * 计算墨卡托坐标距离
     */
    private static double calcDistance3857(Coord coord1, Coord coord2) {
        return Math.sqrt(Math.pow(coord1.getX() - coord2.getX(), 2) + Math.pow(coord1.getY() - coord2.getY(), 2));
    }

    /**
     * 计算link直接是否连通
     *
     * @param start 起点
     * @param end   终点
     * @param stack 存储路径
     */
    private static boolean calcRouteAccessible(Link start, Link end, Stack<Id<Link>> stack) {
        stack.push(start.getId());
        if (start.getId().equals(end.getId())) {
            return true;
        }
        for (Map.Entry<Id<Link>, ? extends Link> entry : start.getToNode().getOutLinks().entrySet()) {
            Id<Link> id = entry.getKey();
            if (stack.contains(id) && !id.equals(start.getId())) {
                stack.pop();
                return false;
            }
            Link link = entry.getValue();
            if (start.getFromNode().getId().equals(link.getToNode().getId()) && !stack.isEmpty() && !start.getId().equals(stack.get(0))) {
                continue;
            }
            if (calcRouteAccessible(link, end, stack)) {
                return true;
            }
            if (stack.isEmpty()) {
                return false;
            }
            stack.pop();
        }
        return false;
    }


    /**
     * 计算pcu
     *
     * @param stats  流量
     * @param second 时长
     */
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
            if (Math.min(line2[0].getX(), line2[1].getX()) <= intersection[0] &&
                    intersection[0] <= Math.max(line2[0].getX(), line2[1].getX()) &&
                    Math.min(line2[0].getY(), line2[1].getY()) <= intersection[1] &&
                    intersection[1] <= Math.max(line2[0].getY(), line2[1].getY()) &&
                    Math.min(line1[0].getX(), line1[1].getX()) <= intersection[0] &&
                    intersection[0] <= Math.max(line1[0].getX(), line1[1].getX()) &&
                    Math.min(line1[0].getY(), line1[1].getY()) <= intersection[1] &&
                    intersection[1] <= Math.max(line1[0].getY(), line1[1].getY())) {
                return new Coord(intersection);
            } else {
                return null;
            }
        }
    }

}
