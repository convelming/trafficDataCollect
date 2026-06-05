package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.convelming.roadflow.enums.HighwayType;
import com.convelming.roadflow.service.PortalService;
import com.convelming.roadflow.util.GeojsonRead;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PortalServiceImpl implements PortalService {

    Map<String, List<GeojsonRead.Geojson<JSONObject>>> addrMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> roadInfo(String url, double[][] xyarr) {
        if (addrMap.isEmpty()) {
            String qu_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E5%B9%BF%E5%B7%9E_%E5%8C%BA%E7%BA%A7.geojson";
            String jiedao = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E5%B9%BF%E5%B7%9E_%E8%A1%97%E9%81%93%E7%BA%A7.geojson";
            String cun_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E5%B9%BF%E5%B7%9E_%E6%9D%91%E7%BA%A7.geojson";
            // 添加区、街道、村
            addrMap.put("qu", read_url(qu_url));
            addrMap.put("jiedao", read_url(jiedao));
            addrMap.put("cun", read_url(cun_url));
        }
        // 圈选范围geometry
        Geometry geometry = arr2geometry(xyarr);
        List<String> addr = addr(geometry.getCentroid());
        // 路网
        List<GeojsonRead.Geojson<JSONObject>> roadElements = read_url(url);
        // 分类
        Map<String, Integer> roadMap = new ConcurrentHashMap<>();
        // 长度
        BigDecimal roadLength = new BigDecimal("0");
        // 数量
        int count = 0;
        for (GeojsonRead.Geojson<JSONObject> element : roadElements) {
            if (geometry.intersects(element.geometry())) {
                count++;
                String tags = element.properties().getString("other_tags");
                // 获取属性
                if (StringUtils.isBlank(tags)) {
                    JSONObject jobj = element.properties();

                    double length = calcDistance3857(element.geometry());
                    String oneway = jobj.getString("oneway");
                    if ("F".equals(oneway)) {
                        // 双线_长度/2
                        roadLength = roadLength.add(new BigDecimal(length).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP));
                    } else {
                        roadLength = roadLength.add(new BigDecimal(length));
                    }
                    String highway = jobj.getString("fclass");
                    HighwayType ht = HighwayType.getOfCode(highway);
                    String name = ht == null ? highway : ht.getName();
                    roadMap.merge(name, 1, Integer::sum);
                } else {
                    JSONObject jobj = tags2map(tags);
                    String length = jobj.getString("length");
                    String oneway = jobj.getString("oneway");
                    if ("no".equals(oneway)) {
                        roadLength = roadLength.add(new BigDecimal(length).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP));
                    } else {
                        roadLength = roadLength.add(new BigDecimal(length));
                    }
                    String highway = jobj.getString("highway");
                    HighwayType ht = HighwayType.getOfCode(highway);
                    String name = ht == null ? highway : ht.getName();
                    roadMap.merge(name, 1, Integer::sum);
                }
            }
        }
        roadLength = roadLength.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
        // 面积 km²
        BigDecimal area = BigDecimal.valueOf(area(geometry)).divide(new BigDecimal("1000000"), 2, RoundingMode.HALF_UP);
        if(area.doubleValue() == 0.){
            area = new BigDecimal("0.01"); // 防止出现 by zero
        }
        Map<String, Object> result = new HashMap<>();
        result.put("所在行政区", addr);
        result.put("道路里程", roadLength.doubleValue());
        result.put("道路密度", roadLength.divide(area, 2, RoundingMode.HALF_UP));
        result.put("道路数量", count);

        BigDecimal _100 = new BigDecimal("100").setScale(2, RoundingMode.HALF_UP);
        BigDecimal _count = new BigDecimal(roadMap.values().stream().reduce(0, Integer::sum)).setScale(2, RoundingMode.HALF_UP);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : roadMap.entrySet()) {
            Map<String, Object> map = new HashMap<>();
            Integer num = entry.getValue();
            String name = entry.getKey();
            Double r = BigDecimal.valueOf(num)
                    .multiply(_100)
                    .divide(_count, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            map.put("name", name);
            map.put("r", r);
            map.put("num", num);
            list.add(map);
        }
        list.sort(Comparator.comparing((a) -> -(Integer) a.get("num")));
        result.put("数据类型分布", list);
        return result;
    }

    private List<String> addr(Geometry point) {
        List<String> list = new ArrayList<>();
        list.add("广东省");
        list.add("广州市");
        // 区
        GeojsonRead.Geojson<JSONObject> qu = containsOne(addrMap.get("qu"), point);
        if (qu != null) {
            list.add(qu.properties().getString("name"));
        }
        // 街道
        GeojsonRead.Geojson<JSONObject> jiedao = containsOne(addrMap.get("jiedao"), point);
        if (jiedao != null) {
            list.add(jiedao.properties().getString("街道名"));
        }
        // 村
        GeojsonRead.Geojson<JSONObject> cun = containsOne(addrMap.get("cun"), point);
        if (cun != null) {
            list.add(cun.properties().getString("ZLDWMC"));
        }
        return list;
    }

    public GeojsonRead.Geojson<JSONObject> containsOne(List<GeojsonRead.Geojson<JSONObject>> data, Geometry geometry) {
        for (GeojsonRead.Geojson<JSONObject> d : data) {
            if (d.geometry().contains(geometry)) {
                return d;
            }
        }
        return null;
    }

    private List<GeojsonRead.Geojson<JSONObject>> read_url(String url) {
        try {
            long time = System.currentTimeMillis();
            URL uri = new URL(url);
            InputStream inputStream = uri.openStream();
            GeojsonRead<JSONObject> geojsonRead = new GeojsonRead<>(inputStream, "UTF-8");
            log.info("加载: {}", url);
            log.info("耗时：{}ms", System.currentTimeMillis() - time);
            return geojsonRead.data();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<GeojsonRead.Geojson<JSONObject>> intersects(Geometry geometry, List<GeojsonRead.Geojson<JSONObject>> data) {
        List<GeojsonRead.Geojson<JSONObject>> list = new ArrayList<>();
        for (GeojsonRead.Geojson<JSONObject> d : data) {
            if (d.geometry().intersects(geometry)) {
                list.add(d);
            }
        }
        return list;
    }

    private Geometry arr2geometry(double[][] xyarr) {
        GeometryFactory factory = new GeometryFactory();
        Coordinate[] points = new Coordinate[xyarr.length];
        for (int i = 0; i < xyarr.length; i++) {
            points[i] = new Coordinate(xyarr[i][0], xyarr[i][1]);
        }
        CoordinateArraySequence cas = new CoordinateArraySequence(points);
        LinearRing shell = new LinearRing(cas, factory);
        return new Polygon(shell, null, factory);
    }

    public void address(Geometry area) {
        Geometry point = area.getCentroid();
//        // 省
//        String sheng_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E7%9C%81_%E5%85%A8%E5%9B%BD.geojson";
//        GeojsonRead.Geojson<JSONObject> sheng = containsOne(read_url(sheng_url), point);
//        if (sheng != null) {
//            System.out.println(sheng.properties().getString("省"));
//        }
//        // 市
//        String shi_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E5%B9%BF%E4%B8%9C_%E5%B8%82%E7%BA%A7.geojson";
//        GeojsonRead.Geojson<JSONObject> shi = containsOne(read_url(shi_url), point);
//        if (shi != null) {
//            System.out.println(shi.properties());
//        }
        List<String> addr = new ArrayList<>(List.of("广东省", "广州市"));
        // 区
        GeojsonRead.Geojson<JSONObject> qu = containsOne(addrMap.get("qu"), point);
        if (qu != null) {
            addr.add(qu.properties().getString("name"));
        }
        // 街道
        GeojsonRead.Geojson<JSONObject> jiedao = containsOne(addrMap.get("jiedao"), point);
        if (qu != null) {
            addr.add(jiedao.properties().getString("name"));
        }
        // 村
        GeojsonRead.Geojson<JSONObject> cun = containsOne(addrMap.get("cun"), point);
        if (cun != null) {
            addr.add(cun.properties().getString("ZLDWMC"));
        }
        System.out.println(addr);
    }

    private JSONObject tags2map(String tags) {
        JSONObject jobj = new JSONObject();
        //"other_tags": "\"modes\"=>\"car,walk,bus\",\"lanes\"=>\"1\",\"oneway\"=>\"no\",\"length\"=>\"13.0\",\"speedClass\"=>\"7\",\"minspeed\"=>\"11\",\"maxspeed\"=>\"30\""
        char[] chars = tags.toCharArray();
        String key = "", value = "";
        int index = 0;
        for (char c : chars) {
            if (c == '"') {
                index++;
            } else {
                if (index < 2) {
                    key += c;
                } else if (index == 3) {
                    value += c;
                } else if (index > 3) {
                    index = 0;
                    jobj.put(key, value);
                    key = "";
                    value = "";
                }
            }
        }
        return jobj;
    }

    /**
     * 根据两个位置的经纬度，来计算两地的距离（单位为KM）
     * 参数为String类型
     *
     * @return
     */
    public static double getDistance(Geometry geometry) {
        double distance = 0;
        Coordinate[] points = geometry.getCoordinates();
        for (int i = 1; i < points.length; i++) {
            Coordinate c1 = points[i - 1];
            Coordinate c2 = points[i];
            double radLat1 = rad(c1.x);
            double radLat2 = rad(c2.x);
            double difference = radLat1 - radLat2;
            double mdifference = rad(c1.y) - rad(c2.y);
            double d1 = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(difference / 2), 2)
                    + Math.cos(radLat1) * Math.cos(radLat2)
                    * Math.pow(Math.sin(mdifference / 2), 2)));
            distance += d1 * EARTH_RADIUS;
        }
        return distance;
    }

    private static final double EARTH_RADIUS = 6378137;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    private static double area(Geometry geometry) {
        try {
//            CoordinateReferenceSystem sourceCRS = CRS.decode("CRS:84");
            // Pseudo-Mercator(转换为地理坐标系)
//            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857");
//            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS, false);
//            Geometry geometryMercator = JTS.transform(geometry, transform);
            return geometry.getArea();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算墨卡托坐标距离
     */
    private static double calcDistance3857(Geometry geometry) {
        double distance = 0;
        Coordinate[] points = geometry.getCoordinates();
        for (int i = 1; i < points.length; i++) {
            Coordinate c1 = points[i - 1];
            Coordinate c2 = points[i];
            distance += Math.sqrt(Math.pow(c1.getX() - c2.getX(), 2) + Math.pow(c1.getY() - c2.getY(), 2));
        }
        return distance;
    }
}
