package com.convelming.roadflow;

import com.convelming.roadflow.mapper.LinkStatsMapper;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.OSMWayMapper;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.OSMWay;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LinkMapperTest {

    @Resource
    private MatsimLinkMapper linkMapper;
    @Resource
    private LinkStatsMapper linkStatsMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private OSMWayMapper wayMapper;

    private static final CoordinateTransformation ct_4526to3857 = TransformationFactory.getCoordinateTransformation("epsg:4526", "epsg:3857");

    //    @Test
    public void test() {
        List<MatsimLink> links = linkMapper.queryByOrigid("25680335");

        List<MatsimLink> toLinks = new ArrayList<>();
        // fromNode \ toNode 唯一的两个点分别是 from\to 的起点\终点
        // 760905 \ 760916


        MatsimLink start = getStartLink(links);
        links.remove(start);
        toLinks.add(start);
        for (MatsimLink next : links) {
            if (Objects.equals(start.getToNode(), next.getFromNode()) && !Objects.equals(start.getFromNode(), next.getToNode())) {
                toLinks.add(next);
                start = next;
            }
        }


        System.out.println(toLinks);

    }


    public void sqlTest() throws SQLException {
        long id = -1;
        MatsimLink node = new MatsimLink();
//        MatsimLink node = new MatsimLink(id, 1, 1L, 1L, 1., 1., 1., new PGgeometry("LINESTRING((1 1),(2 2))"), 1L, "", null);
        linkMapper.deleteById(id);
        linkMapper.insert(node);
        MatsimLink result = linkMapper.selectById(id);
        System.out.println(result);
    }

    @Test
    public void initLink() {
        // 文件中 srid 为 3857
        // 1373440 16:38:33
        String path = "C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2024-01\\gz_idRemap_subway_pt_remod_240124_slice.xml";
        Network network = NetworkUtils.readNetwork(path);
        int count = 0, total = network.getLinks().values().size();
        List<MatsimLink> links = new ArrayList<>();

        Map<String, OSMWay> wayMap = wayMapper.queryByPolygon(null).stream().collect(Collectors.toMap(OSMWay::getId, w -> w));

        for (org.matsim.api.core.v01.network.Link link : network.getLinks().values()) {
            MatsimLink l = new MatsimLink();
            l.setId(String.valueOf(link.getId().toString()));
            l.setSrid(GeomUtil.MKT);
            l.setFromNode(String.valueOf(link.getFromNode().getId().toString()));
            l.setToNode(String.valueOf(link.getToNode().getId().toString()));
            l.setLength(link.getLength());
            l.setFreespeed(link.getFreespeed());
            l.setCapacity(link.getCapacity());
            l.setLane((int) link.getNumberOfLanes());
            Attributes attributes = link.getAttributes();
            l.setType(String.valueOf(attributes.getAttribute("type")));
            l.setOrigid(Long.valueOf(attributes.getAttribute("origid") == null ? "-1" : attributes.getAttribute("origid").toString()));
            l.setGeom(GeomUtil.genLine(
                    ct_4526to3857.transform(link.getFromNode().getCoord()),
                    ct_4526to3857.transform(link.getToNode().getCoord()),
                    l.getSrid())
            );
            OSMWay way = wayMap.get(l.getOrigid());
            if(way != null){
                l.setName(way.getName());
            }
            links.add(l);
//            jdbcTemplate.update("update matsim_link set lane = ? where id = ?", l.getLane(), l.getId());
//            linkMapper.insert(l);
//            System.out.println(count + " / " + total);
        }
        linkMapper.batchInsert(links);

//        linkMapper.batchInsert(links);
    }

    private MatsimLink getStartLink(List<MatsimLink> links) {
        for (int i = 0; i < links.size(); i++) {
            MatsimLink il = links.get(i);
            boolean flag = true;
            for (int j = 0; j < links.size(); j++) {
                MatsimLink jl = links.get(j);
                if (j != i && Objects.equals(il.getFromNode(), jl.getFromNode())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return il;
            }
        }
        return null;
    }

    //    @Test
    public void validLinkId() {
        String fileName = "C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2024-01\\gz230427_fullPath_4526_h9_withSubwayPtMapped.xml";
        Network network = NetworkUtils.readNetwork(fileName);

        Map<String, Integer> map = new HashMap<>();

        network.getLinks().values().forEach(link -> {
            map.put(link.getId().toString(), 3);
        });
        List<MatsimLink> links = jdbcTemplate.query("select * from matsim_link", new BeanPropertyRowMapper<>(MatsimLink.class));
        Map<String, MatsimLink> linkMap = links.stream().collect(Collectors.toMap(MatsimLink::getId, l -> l));
        links.forEach(link -> {
            String id = link.getId().toString();
            map.merge(id, 2, Integer::sum);
        });
        int[] count = {0};
        map.forEach((k, v) -> {
            if (v == 2) {
                System.out.println("流量调查单独存在：" + k);
            } else if (v == 3) {
//                System.out.println("xml 单独存在：" + k);
            } else {
                MatsimLink ml = linkMap.get(Long.valueOf(k));
                Link lk = network.getLinks().get(Id.createLinkId(k));
                if (ml.getOrigid().toString().equals(lk.getAttributes().getAttribute("origid").toString())
                        && ml.getToNode().toString().equals(lk.getToNode().getId().toString())
                        && ml.getFromNode().toString().equals(lk.getFromNode().getId().toString())
                ) {

                } else {
                    count[0]++;
                    System.out.println("LinkId: " + k + " , 绑定节点与osm道路不一致");
                }
            }
        });
        System.out.println("节点与osm道路不一致数量：" + count[0]);
        System.out.println("流量调查单独存在：" + map.values().stream().filter(i -> i == 2).count());
    }

}
