package com.convelming.roadflow;


import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.mapper.OSMNodeMapper;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.model.OSMNode;
import com.convelming.roadflow.util.GeomUtil;
import com.convelming.roadflow.util.XmlUtil;
import jakarta.annotation.Resource;
import net.postgis.jdbc.PGgeometry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class NodeMapperTest {
    @Resource
    private MatsimNodeMapper matsimNodeMapper;

    @Resource
    private OSMNodeMapper osmNodeMapper;
    private static final CoordinateTransformation ct_4526to3857 = TransformationFactory.getCoordinateTransformation("epsg:4526", "epsg:3857");


    public void sqlTest() throws SQLException {
//        long id = -1;
//        MatsimNode node = new MatsimNode(id, 1., 1., 4326, new PGgeometry("POINT(1 1)"));
//        matsimNodeMapper.deleteById(id);
//        matsimNodeMapper.insert(node);
//        MatsimNode result = matsimNodeMapper.selectById(id);
//        System.out.println(result);
        long id = -1;
        OSMNode node = new OSMNode(id, 1, new Date(), 1L, "1", 1L, 1., 1., 1., 1., new PGgeometry("SRID=4326;POINT(1 1)"), new PGgeometry("SRID=3857;POINT(1 1)"), "");
        osmNodeMapper.deleteById(id);
        osmNodeMapper.insert(node);
        OSMNode result = osmNodeMapper.selectById(id);
        System.out.println(result);

    }


    public void initMatsiNode() {
        // 文件中 srid 为 3857
        String path = "C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-10\\gz230427_fullPath_4526_h9.xml";
        Network network = NetworkUtils.readNetwork(path);

        List<MatsimNode> nodes = new ArrayList<>();
        for (org.matsim.api.core.v01.network.Node node : network.getNodes().values()) {
            MatsimNode n = new MatsimNode();
            n.setId(Long.valueOf(node.getId().toString()));
            n.setSrid(GeomUtil.MKT);
            Coord coord = ct_4526to3857.transform(node.getCoord());
            n.setX(coord.getX());
            n.setY(coord.getY());
            n.setGeom(GeomUtil.genPoint(node, n.getSrid()));
            nodes.add(n);
        }

        matsimNodeMapper.batchInsert(nodes);
    }

//    @Test
    public void initOSMNode() {
        try {
            List<OSMNode> nodes = XmlUtil.loadNode("C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-11\\gzInpoly221123.osm", "node");
            osmNodeMapper.batchInsert(nodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
