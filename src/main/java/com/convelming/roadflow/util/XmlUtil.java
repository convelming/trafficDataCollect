package com.convelming.roadflow.util;

import com.alibaba.fastjson.JSONObject;
import com.convelming.roadflow.model.OSMNode;
import com.convelming.roadflow.model.OSMWay;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.jfree.data.json.impl.JSONArray;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XmlUtil {

    private static final CoordinateTransformation ct_4326to3857 = TransformationFactory.getCoordinateTransformation("epsg:4326", "epsg:3857");
    private static final CoordinateTransformation ct_4526to3857 = TransformationFactory.getCoordinateTransformation("epsg:4526", "epsg:3857");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public static void main(String[] args) throws ParseException {
//        List<OSMWay> osmways = loadWay("C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-11\\gzInpoly221123.osm", "way");
//        System.out.println(osmways);
        List<OSMNode> osmways = loadNode("C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-11\\gzInpoly221123.osm", "node");
        System.out.println(osmways);
    }

    public static List<OSMNode> loadNode(String filename, String nodeName) throws ParseException {
        Document document = load(filename);
        Element root = document.getRootElement();
        List<OSMNode> result = new ArrayList<>();
        List<Element> list = root.elements(nodeName);
        for (Element node : list) {
            OSMNode osm = new OSMNode();
            osm.setId(Long.valueOf(node.attributeValue("id")));
            osm.setVersion(Integer.valueOf(node.attributeValue("version")));
            osm.setTimestamp(sdf.parse(node.attributeValue("timestamp")));
            osm.setUid(Long.valueOf(node.attributeValue("uid")));
            osm.setUser(String.valueOf(node.attributeValue("user")));
            osm.setChangeset(Long.valueOf(node.attributeValue("changeset")));
            osm.setLat(Double.valueOf(node.attributeValue("lat")));
            osm.setLon(Double.valueOf(node.attributeValue("lon")));
            // 经纬度转化墨卡托
            Coord coord = new Coord(osm.getLon(), osm.getLat());
            coord = ct_4326to3857.transform(coord);
            osm.setX(coord.getX());
            osm.setY(coord.getY());
            // 构建空间信息
            osm.setGeom4326(GeomUtil.genPoint(osm.getLon(), osm.getLat(), 4326));
            osm.setGeom3857(GeomUtil.genPoint(osm.getX(), osm.getY(), GeomUtil.MKT));

            List<Element> nnodes = node.elements("tag");
            Map<String, Object> other = new HashMap<>();
            for (Element tag : nnodes) { // node 下面的 tag
                other.put(tag.attributeValue("k"), tag.attributeValue("v"));
            }
            osm.setOther(JSONObject.toJSONString(other));
            result.add(osm);
        }


        return result;
    }

    public static List<OSMWay> loadWay(String filename, String nodeName) throws ParseException {
        List<OSMWay> result = new ArrayList<>();
        Document document = load(filename);
        Element root = document.getRootElement();
        List<Element> list = root.elements(nodeName);
        Map<Long, OSMNode> nodeMap = loadNode(filename, "node").stream().collect(Collectors.toMap(OSMNode::getId, (p) -> p));
        for (Element way : list) {
            OSMWay osmWay = new OSMWay();
            osmWay.setId(Long.valueOf(way.attributeValue("id")));
            osmWay.setVersion(Integer.valueOf(way.attributeValue("version")));
            osmWay.setTimestamp(sdf.parse(way.attributeValue("timestamp")));
            osmWay.setUid(Long.valueOf(way.attributeValue("uid")));
            osmWay.setUser(way.attributeValue("user"));
            osmWay.setChageset(Long.valueOf(way.attributeValue("changeset")));

            List<OSMNode> nds = new ArrayList<>();

            List<Element> ndes = way.elements("nd"); // 路上所有点
            // 拿到全部 构建路空间信息
            for (Element nd : ndes) {
                Long nodeId = Long.valueOf(nd.attributeValue("ref"));
                if (nodeMap.get(nodeId) != null) {
                    nds.add(nodeMap.get(nodeId));
                }
            }
            if (nds.size() <= 1) { // 一条路至少有两个不为空的点
                continue;
            }
            List<Element> tags = way.elements("tag"); // 其他信息

            Map<String, Object> tagMap = new HashMap<>();
            for (Element tag : tags) {
                tagMap.put(tag.attributeValue("k"), tag.attributeValue("v"));
            }

//            if (tagMap.get("building") != null || tagMap.get("natural") != null || tagMap.get("man_made") != null || tagMap.get("amenity") != null) { // 建筑物 自然景观 人造景观 重要设施
//                continue;
//            }

            osmWay.setOneway("yes".equals(tagMap.get("oneway")));
            osmWay.setHighway((String) tagMap.get("highway"));
            osmWay.setOther(JSONObject.toJSONString(tagMap));

            osmWay.setNodes(JSONArray.toJSONString(nds.stream().map(OSMNode::getId).toList()));
            osmWay.setGeom3857(GeomUtil.genWay(nds, GeomUtil.MKT));
            osmWay.setGeom4326(GeomUtil.genWay(nds, 4326));

            result.add(osmWay);
        }
        return result;
    }

    private static Document load(String filename) {
        Document document = null;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(new File(filename)); // 读取XML文件,获得document对象
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return document;
    }


}
