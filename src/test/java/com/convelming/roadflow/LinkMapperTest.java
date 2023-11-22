package com.convelming.roadflow;

import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.util.GeomUtil;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.objectattributes.attributable.Attributes;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LinkMapperTest {

    @Resource
    private MatsimLinkMapper linkMapper;

    private static final CoordinateTransformation ct_4526to3857 = TransformationFactory.getCoordinateTransformation("epsg:4526", "epsg:3857");

    @Test
    public void test() {
        List<MatsimLink> links = linkMapper.queryByOrigid(25680335L);

        List<MatsimLink> toLinks = new ArrayList<>();
        // fromNode \ toNode 唯一的两个点分别是 from\to 的起点\终点
        // 760905 \ 760916



        MatsimLink start = getStartLink(links);
        links.remove(start);
        toLinks.add(start);
        for(MatsimLink next : links){
            if(Objects.equals(start.getToNode(), next.getFromNode()) && !Objects.equals(start.getFromNode(), next.getToNode())){
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

//    @Test
    public void initLink() {
        // 文件中 srid 为 3857
        String path = "C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-10\\gz230427_fullPath_4526_h9.xml";
        Network network = NetworkUtils.readNetwork(path);

        List<MatsimLink> links = new ArrayList<>();
        for (org.matsim.api.core.v01.network.Link link : network.getLinks().values()) {
            MatsimLink l = new MatsimLink();
            l.setId(Long.valueOf(link.getId().toString()));
            l.setSrid(GeomUtil.MKT);
            l.setFromNode(Long.valueOf(link.getFromNode().getId().toString()));
            l.setToNode(Long.valueOf(link.getToNode().getId().toString()));
            l.setLength(link.getLength());
            l.setFreespeed(link.getFreespeed());
            l.setCapacity(link.getCapacity());
            Attributes attributes = link.getAttributes();
            l.setType(String.valueOf(attributes.getAttribute("type")));
            l.setOrigid(Long.valueOf((String) attributes.getAttribute("origid")));
            l.setGeom(GeomUtil.genLine(
                    ct_4526to3857.transform(link.getFromNode().getCoord()),
                    ct_4526to3857.transform(link.getToNode().getCoord()),
                    l.getSrid())
            );
            links.add(l);
        }

        linkMapper.batchInsert(links);
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
}
