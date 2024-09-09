package com.convelming.roadflow;

import com.convelming.roadflow.mapper.LinkStatsMapper;
import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.util.IdUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Files;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author：Milu
 * 严重落后进度啦......
 * <p>
 * date：2024/1/23
 * project:huangpuScienceCity
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class Compare2Osm2MatsimNetwork {

    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    MatsimNodeMapper nodeMapper;
    @Resource
    LinkStatsMapper statsMapper;
    @Resource
    private IdUtil idUtil;

    private static final CoordinateTransformation ct_4326to3857 = TransformationFactory.getCoordinateTransformation("epsg:4326", "epsg:3857");
    private static final CoordinateTransformation ct_4356to3857 = TransformationFactory.getCoordinateTransformation("epsg:4526", "epsg:3857");
    static Random random = new Random();

    @Test
    public void inputGddata() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        DecimalFormat decimalFormat = new DecimalFormat("#0.00"); // 创建DecimalFormat对象，指定保留两位小数的模式
        String remark = "";

        String[] files = {
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240110.txt",
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240111.txt",
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240112.txt",
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240113.txt",
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240114.txt",
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240115.txt",
                "F:\\高德路况图片识别结果_v3\\roadstatus_20240116.txt",
        };

        Map<String, MatsimLink> linkMap = jdbcTemplate.query(
                        "select * from matsim_link",
                        new BeanPropertyRowMapper<>(MatsimLink.class))
                .stream().collect(Collectors.toMap(MatsimLink::getId, l -> l));
        for (String file : files) {
            log.info("文件:{}导入开始 ...", file);
            BufferedReader reader = Files.newBufferedReader(new File(file).toPath());
            // 从4开始每4个为一小时
            // 例如4-7、8-11
            String[] title = reader.readLine().split(",");
            log.info(Arrays.toString(title));
            List<LinkStats> list = new ArrayList<>();
            while (reader.ready()) {
                String line = reader.readLine();
                String[] data = line.split(",");
                String linkId = data[0];
                String wayId = data[1];
                MatsimLink link = linkMap.get(linkId);
                double lon = Double.parseDouble(data[2]);
                double lat = Double.parseDouble(data[3]);
                Coord c3857 = ct_4326to3857.transform(new Coord(lon, lat));


                for (int i = 4; i < title.length; i += 4) {
                    LinkStats stat = new LinkStats();
                    stat.setId(idUtil.getId("matsim_link"));
                    stat.setLinkId(linkId);
                    stat.setWayId(wayId);
                    stat.setX(c3857.getX());
                    stat.setY(c3857.getY());
                    stat.setType("3");
                    stat.setIpAddr("127.0.0.1");
                    // 一个小时的量

                    Date beginTime = sdf.parse("20" + title[i]);
                    Date endTime = null;
                    int endTimeIndex = i + 4;
                    if (endTimeIndex >= title.length) {
                        endTime = sdf.parse("20" + title[4]); // 到第二天第一次统计
                        endTime = new Date(endTime.getTime() + 1000 * 60 * 60 * 24);
                    } else {
                        endTime = sdf.parse("20" + title[endTimeIndex]);
                    }

                    Double d04_19 = Double.valueOf(data[i]);
                    Double d19_34 = Double.valueOf(data[i + 1]);
                    Double d34_49 = Double.valueOf(data[i + 2]);
                    Double d49_04 = Double.valueOf(data[i + 3]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(beginTime);

                    stat.setPcuH(Double.valueOf(decimalFormat.format(getRndVol(calendar.get(Calendar.HOUR_OF_DAY), d04_19 + d19_34 + d34_49 + d49_04, link.getCapacity()))));
                    stat.setBeginTime(beginTime);
                    stat.setEndTime(endTime);
//                    statsMapper.insert(stat);
                    list.add(stat);
                }
//                statsMapper.insert(stat);
            }
            reader.close();

            int insert_max_nums = 50000;
            if (list.size() > insert_max_nums) {
                for (int i = 0, len = list.size(); i < len; i += insert_max_nums) {
                    int end = i + insert_max_nums;
                    if (end > len) {
                        end = len;
                    }
                    List<LinkStats> temp = list.subList(i, end);
                    jdbcTemplate.batchUpdate(LinkStatsMapper.INSERT_SQL, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            LinkStats stats = temp.get(i);
                            ps.setLong(1, stats.getId());
                            ps.setString(2, stats.getLinkId());
                            ps.setString(3, stats.getWayId());
                            ps.setTimestamp(4, new Timestamp(stats.getBeginTime().getTime()));
                            ps.setTimestamp(5, new Timestamp(stats.getEndTime().getTime()));
                            ps.setString(6, stats.getType());
                            ps.setDouble(7, stats.getPcuH());
                            ps.setDouble(8, stats.getScar());
                            ps.setDouble(9, stats.getStruck());
                            ps.setDouble(10, stats.getMcar());
                            ps.setDouble(11, stats.getMtruck());
                            ps.setDouble(12, stats.getLcar());
                            ps.setDouble(13, stats.getLtruck());
                            ps.setString(14, stats.getVideo());
                            ps.setBoolean(15, stats.getIsTwoWay());
                            ps.setDouble(16, stats.getX());
                            ps.setDouble(17, stats.getY());
                            ps.setString(18, stats.getRemark());
                            ps.setString(19, stats.getIpAddr());
                            ps.setInt(20, 1);
                            ps.setInt(21, 0);
                            ps.setTimestamp(22, new Timestamp(System.currentTimeMillis()));
                            ps.setTimestamp(23, new Timestamp(System.currentTimeMillis()));
                        }

                        @Override
                        public int getBatchSize() {
                            return temp.size();
                        }
                    });
                    log.info("已添加{}/{}行", end, len);
                }
            } else {
//                jdbcTemplate.batchUpdate(LinkStatsMapper.INSERT_SQL, LinkStatsMapper.genArgs(list));
            }

            log.info("文件{}导入完成 ...", file);
        }

    }

    public static double getRndVol(int h, double cgstLevel, double capacity) {
        cgstLevel /= 4;
        if (cgstLevel == 1) {
            if (0 <= h && h <= 5) {
                cgstLevel = random.nextDouble(0.2) + 1;
            } else {
                cgstLevel = random.nextDouble(0.3) + 1.2;
            }
        } else if (cgstLevel < 2) {
            cgstLevel = 1 + 0.5 * (cgstLevel - 1);
        } else if (cgstLevel < 3) {
            cgstLevel = 1.5 + 0.5 * (cgstLevel - 2);
        } else if (cgstLevel <= 4) {
            cgstLevel = 2 + 2 * (cgstLevel - 3);
        }


        double vcRatio = (cgstLevel - 1) / Math.pow(cgstLevel, 2);
        return vcRatio * capacity * 4;

//        Random random = new Random();
//        double vcRatio = cgstLevel / 4.0 * random.nextDouble();
//        return vcRatio * capacity;
    }

    /**
     * idMaps oldId:newId
     */
    @Test
    public void updateLinkId() {
        Network oldNetwork = NetworkUtils.readNetwork("F:/matsimxml/gz230427_fullPath_4526_h9.xml");
        Network newNetwork = NetworkUtils.readNetwork("F:/matsimxml/gzInpoly240126.xml");
        log.info("新旧路网对比开始 ... ");
        String sql = "select * from link_stats where type='3'";
        List<LinkStats> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(LinkStats.class));
        log.info("新旧路网对比开始 ... ");
        Map<String, String> idMaps = mappingOldNewLinkIds(list, oldNetwork, newNetwork);
        log.info("新旧路网对比完成 ... ");
        log.info("修改数据开始 ... ");
        list.forEach(stats -> {
            String oldId = String.valueOf(stats.getLinkId());
            String newId = idMaps.get(oldId);
            if (newId != null && !oldId.equals(newId)) {
                Link newLink = newNetwork.getLinks().get(Id.createLinkId(newId));
                Coord newCoord = ct_4356to3857.transform(newLink.getCoord());
                double x = newCoord.getX(), y = newCoord.getY();
                String update = "update link_stats set link_id=?, x=?, y=?, way_id=?, version = 100 where link_id=? ";
                jdbcTemplate.update(update, new Object[]{newId, x, y, Long.valueOf((String) newLink.getAttributes().getAttribute("origid")), oldId});
            }
        });
        log.info("修改数据完成 ... ");
    }

    @Test
    public void updateLinkIdManual() throws FileNotFoundException {
//        Network oldNetwork = NetworkUtils.readNetwork("F:/matsimxml/gz230427_fullPath_4526_h9.xml");
        Network newNetwork = NetworkUtils.readNetwork("F:/matsimxml/gzInpoly240126.xml");

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\zengren\\Desktop\\1.csv")));
        List<String> lines = reader.lines().toList();
        lines.forEach(line -> {
            // id,link_id,way_id,new_linkid,new_segid,check_dupl
            line = line.replace("\"", "");
            String[] field = line.split(",");
            String id = field[0], newLinkId = field[3], oldLinkId = field[1], newWayId = field[4];
            if ("id".equals(id)) {
                return;
            }
            if (newLinkId != null && !oldLinkId.equals(newLinkId)) {
                Coord newCoord = ct_4356to3857.transform(newNetwork.getLinks().get(Id.createLinkId(newLinkId)).getCoord());
                double x = newCoord.getX(), y = newCoord.getY();
                String update = "update link_stats set link_id=?, x=?, y=?, way_id=?, version = 110 where id=?";
                jdbcTemplate.update(update, new Object[]{newLinkId, x, y, Long.valueOf(newWayId), Long.valueOf(id)});
            }
        });
    }

    private static final long[] count = {0};

    synchronized static void plus(long[] count) {
        count[0]++;
        if (count[0] % 100000000 == 0) {
            log.info("已对比{}次", count[0]);
        }
    }

    public Map<String, String> mappingOldNewLinkIds(List<LinkStats> linkStats, Network oldNetwork, Network newNetwork) {
        log.info("对比路网总比较次数：{}", ((long) linkStats.size()) * newNetwork.getLinks().values().size());
        Map<String, String> idMaps = new HashMap<>();
        linkStats.stream().parallel().forEach(stats -> {
            newNetwork.getLinks().values().forEach(newLink -> {
                plus(count);
                Link oldLink = oldNetwork.getLinks().get(Id.createLinkId(stats.getLinkId()));
                if (oldLink == null) {
                    return;
                }
                if (String.valueOf(oldLink.getAttributes().getAttribute("origid")).equals(newLink.getAttributes().getAttribute("origid"))
                        && Math.abs(oldLink.getFromNode().getCoord().getX() - newLink.getFromNode().getCoord().getX()) < 0.1
                        && Math.abs(oldLink.getFromNode().getCoord().getY() - newLink.getFromNode().getCoord().getY()) < 0.1
                        && Math.abs(oldLink.getToNode().getCoord().getX() - newLink.getToNode().getCoord().getX()) < 0.1
                        && Math.abs(oldLink.getToNode().getCoord().getY() - newLink.getToNode().getCoord().getY()) < 0.1
                ) {
                    idMaps.put(oldLink.getId().toString(), newLink.getId().toString());
                }
            });
        });
        return idMaps;
    }

    public static Map<String, String> mappingOldNewLinkIds(Network oldNetwork, Network newNetwork) {
        log.info("对比路网总比较次数：{}", ((long) oldNetwork.getLinks().values().size()) * newNetwork.getLinks().values().size());
        Map<String, String> idMaps = new HashMap<>();
        oldNetwork.getLinks().values().stream().parallel().forEach(oldLink -> {
            newNetwork.getLinks().values().forEach(newLink -> {
                plus(count);
                if (oldLink.getAttributes().getAttribute("origid").equals(newLink.getAttributes().getAttribute("origid"))
                        && Math.abs(oldLink.getFromNode().getCoord().getX() - oldLink.getFromNode().getCoord().getX()) < 0.1
                        && Math.abs(oldLink.getFromNode().getCoord().getY() - oldLink.getFromNode().getCoord().getY()) < 0.1
                        && Math.abs(oldLink.getToNode().getCoord().getX() - oldLink.getToNode().getCoord().getX()) < 0.1
                        && Math.abs(oldLink.getToNode().getCoord().getY() - oldLink.getToNode().getCoord().getY()) < 0.1
                ) {
                    idMaps.put(oldLink.getId().toString(), newLink.getId().toString());
                }
            });
        });
        return idMaps;
    }

    public static void updateOldLinkIdsInPostgreSQL(Map<String, String> oldNewLinkIds) throws SQLException {

        // JDBC URL, username, and password of your database
        String jdbcUrl = "jdbc:postgresql://192.168.60.231:5432/gisdata";
        String username = "postgres";
        String password = "postgres";
        Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
        // SQL query to select and update linkId values
        String selectQuery = "SELECT link_id FROM link_stats";
        String updateQuery = "UPDATE link_stats SET link_id = ? WHERE link_id = ?";
        try (
                // Create a PreparedStatement for the SELECT query
                PreparedStatement selectStatement = connection.prepareStatement(selectQuery);
                // Create a PreparedStatement for the UPDATE query
                PreparedStatement updateStatement = connection.prepareStatement(updateQuery)
        ) {
            // Execute the SELECT query to get the linkId values
            ResultSet resultSet = selectStatement.executeQuery();
            // Iterate through the result set and update linkId values
            while (resultSet.next()) {
                String oldLinkId = resultSet.getString("linkId");
                // Check if the oldLinkId is in the map
//                if (linkIdMap.containsKey(oldLinkId)) {
//                    // Get the corresponding newLinkId from the map
//                    String newLinkId = linkIdMap.get(oldLinkId);
//
//                    // Set parameters for the UPDATE query
//                    updateStatement.setString(1, newLinkId);
//                    updateStatement.setString(2, oldLinkId);
//
//                    // Execute the UPDATE query
//                    updateStatement.executeUpdate();
//                }
            }

            System.out.println("LinkId values updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * deprecated...
     * this  change the new network id to old one's id, provided that segment id and
     *
     * @param oldNetwork
     * @param newNetwork
     * @param output
     */
    public static void pair2AndCreateNewNetwork(Network oldNetwork, Network newNetwork, String output) {
        Map<Id<Link>, Id<Link>> idMaps = new HashMap<>();
        Map<String, List<Link>> newNetOsmIdMap = getOsmIds(newNetwork);
        oldNetwork.getLinks().forEach((oldLinkId, oldLink) -> {
            String oldOsmId = oldLink.getAttributes().getAttribute("origid").toString();
            if (newNetOsmIdMap.containsKey(oldOsmId)) {
                List<Link> tmpLinks = newNetOsmIdMap.get(oldOsmId);
                for (Link newLink : tmpLinks) {
                    double nodesDis = CoordUtils.calcEuclideanDistance(newLink.getFromNode().getCoord(), oldLink.getFromNode().getCoord()) +
                            CoordUtils.calcEuclideanDistance(newLink.getToNode().getCoord(), oldLink.getToNode().getCoord());
                    if (nodesDis < 0.001) {
                        idMaps.put(newLink.getId(), oldLinkId);

                    }
                }
            }
        });
        Set<Id<Link>> test = new HashSet<>();
        for (Map.Entry t : idMaps.entrySet()) {
            test.add((Id<Link>) t.getValue());
        }
        System.out.println(test.size() + "," + idMaps.size());
        for (Map.Entry newLink : newNetwork.getLinks().entrySet()) {
            if (idMaps.containsKey(newLink.getKey())) {
                Id<Link> oldLinkId = idMaps.get((Id<Link>) newLink.getKey());
                if (newNetwork.getLinks().containsKey(oldLinkId)) {
                    Link tmpLink = newNetwork.getLinks().get(oldLinkId);
                    NetworkUtils.createAndAddLink(newNetwork,
                            Id.createLinkId(oldLinkId + "_new"), tmpLink.getFromNode(), tmpLink.getToNode(),
                            tmpLink.getLength(), tmpLink.getFreespeed(), tmpLink.getCapacity(),
                            tmpLink.getNumberOfLanes(), tmpLink.getAttributes().getAttribute("origid").toString(),
                            tmpLink.getAttributes().getAttribute("type").toString());
                    newNetwork.removeLink(tmpLink.getId());
                }
                Link currentLink = (Link) newLink.getValue();

                NetworkUtils.createAndAddLink(newNetwork,
                        oldLinkId, currentLink.getFromNode(), currentLink.getToNode(),
                        currentLink.getLength(), currentLink.getFreespeed(), currentLink.getCapacity(),
                        currentLink.getNumberOfLanes(), currentLink.getAttributes().getAttribute("origid").toString(),
                        currentLink.getAttributes().getAttribute("type").toString());
                newNetwork.removeLink((Id<Link>) newLink.getKey());
            }
        }
        new NetworkWriter(newNetwork).write(output);
    }

    public static Map<String, List<Link>> getOsmIds(Network network) {
        Map<String, List<Link>> osmIdMap = new HashMap<>();
        network.getLinks().forEach((linkId, link) -> {
            String osmId = link.getAttributes().getAttribute("origid").toString();
            if (osmIdMap.containsKey(osmId)) {
                osmIdMap.get(osmId).add(link);
            } else {
                List<Link> linkList = new ArrayList<>();
                linkList.add(link);
                osmIdMap.put(osmId, linkList);
            }
        });
        return osmIdMap;
    }

}
