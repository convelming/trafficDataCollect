package com.convelming.roadflow;

import com.alibaba.fastjson2.JSONObject;
import com.convelming.roadflow.service.PortalService;
import com.convelming.roadflow.util.GeojsonRead;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class PortalTest {

    @Resource
    private PortalService portalService;

    private static final WKTReader wkt = new WKTReader(JTSFactoryFinder.getGeometryFactory());
    public static String base = "http://192.168.60.231:8085";

    // 选取范围
    public static Geometry area;

    static {
        try {
            area = wkt.read("POLYGON ((113.25957434400004 23.14251508500007, 113.71474009000008 23.70451597300007, 113.30030391600008 23.143889235000074, 113.36104399200008 23.185534803000053, 113.25957434400004 23.14251508500007))");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Test
    public void test() {
        String url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E9%81%93%E8%B7%AF%E4%BA%A4%E9%80%9A/%E5%9F%8E%E5%B8%82%E8%B7%AF%E7%BD%91/OSM%E8%B7%AF%E7%BD%91/2025%E5%B9%B4.geojson?time=1779076168924";
        double[][] xyarr = new double[][]{
                new double[]{113.43734, 23.17127},
                new double[]{113.43808, 23.16111},
                new double[]{113.45573, 23.16124},
                new double[]{113.44702, 23.17481},
                new double[]{113.43734, 23.17127}
        };
        Map<String, Object> result =portalService.roadInfo(url, xyarr);
        System.out.println(result);
    }

    @Test
    public void query() {
        // [
        //  [
        //    113.43734,23.17127
        //  ],
        //  [
        //    113.43808,23.16111
        //  ],
        //  [
        //    113.45573,23.16124
        //  ],
        //  [
        //    113.44702,23.17481
        //  ],
        //  [
        //    113.43734,23.17127
        //  ]
        // ]
        String url = "/%E6%95%B0%E6%8D%AE%E5%BA%93/%E8%AE%BE%E6%96%BD%E5%9C%BA%E6%89%80/A%E7%BA%A7%E6%99%AF%E5%8C%BA.geojson";
        try {
            // 范围
            List<GeojsonRead.Geojson<JSONObject>> data = new ArrayList<>();
            URL uri = new URL(base + url);
            InputStream inputStream = uri.openStream();
            GeojsonRead<JSONObject> geojsonRead = new GeojsonRead<>(inputStream, "UTF-8");
            for (GeojsonRead.Geojson<JSONObject> datum : geojsonRead.data()) {
                if (area.intersects(datum.geometry())) {
                    data.add(datum);
                }
            }
            //
            inputStream.close();
            System.out.println("count: " + data.size());

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        address();
    }

    @Test
    public void address() {
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
        String qu_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E5%B9%BF%E5%B7%9E_%E5%8C%BA%E7%BA%A7.geojson";
        GeojsonRead.Geojson<JSONObject> qu = containsOne(read_url(qu_url), point);
        if (qu != null) {
            addr.add(qu.properties().getString("name"));
        }
        // 村
        String cun_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E5%9F%8E%E5%B8%82%E5%BA%95%E5%BA%A7/%E5%8C%BA%E5%88%92%E8%BE%B9%E7%95%8C/%E8%A1%8C%E6%94%BF%E8%BE%B9%E7%95%8C_%E5%B9%BF%E5%B7%9E_%E6%9D%91%E7%BA%A7.geojson";
        GeojsonRead.Geojson<JSONObject> cun = containsOne(read_url(cun_url), point);
        if (cun != null) {
            addr.add(cun.properties().getString("ZLDWMC"));
        }
        System.out.println(addr);
    }

    @Test
    public void road() {
        double mian = area.getArea();
        String road_url = "http://192.168.60.231:8085/%E6%95%B0%E6%8D%AE%E5%BA%93/%E9%81%93%E8%B7%AF%E4%BA%A4%E9%80%9A/%E5%9F%8E%E5%B8%82%E8%B7%AF%E7%BD%91/%E5%9B%9B%E7%BB%B4%E5%9B%BE%E6%96%B0%E5%AF%BC%E8%88%AA%E7%BA%A7%E8%B7%AF%E7%BD%91/%E5%9B%9B%E7%BB%B4%E5%9B%BE%E6%96%B0_2025%E5%B9%B4.geojson";
        List<GeojsonRead.Geojson<JSONObject>> list = intersects(area, read_url(road_url));
        for (GeojsonRead.Geojson<JSONObject> geojson : list) {
            System.out.println(geojson.properties());
        }
    }


    public GeojsonRead.Geojson<JSONObject> containsOne(List<GeojsonRead.Geojson<JSONObject>> data, Geometry geometry) {
        for (GeojsonRead.Geojson<JSONObject> d : data) {
            if (d.geometry().contains(geometry)) {
                return d;
            }
        }
        return null;
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

    public List<GeojsonRead.Geojson<JSONObject>> read_url(String url) {
        try {
            URL uri = new URL(url);
            InputStream inputStream = uri.openStream();
            GeojsonRead<JSONObject> geojsonRead = new GeojsonRead<>(inputStream, "UTF-8");
            return geojsonRead.data();
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

}
