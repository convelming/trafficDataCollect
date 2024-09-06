package com.convelming.roadflow.util;

import com.convelming.roadflow.model.OSMNode;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Node;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class GeomUtil {

    /**
     * 墨卡托坐标代码
     */
    public static final int MKT = 3857;

    private static GeometryFactory geometryFactory = new GeometryFactory();

    public static void main(String[] args) throws SQLException {
//        PGgeometry ggeometry = new PGgeometry("POINT(1 2)");
//        System.out.println(Arrays.toString(point2xy(ggeometry)));
    }

    public static double getArea(PGgeometry pg){
        WKTReader reader = new WKTReader(geometryFactory);
        try {
            Polygon polygon = (Polygon) reader.read(pg.toString().replace("SRID=3857;", ""));
            return polygon.getArea();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    public static double[] getCentroid(PGgeometry pg) {
        WKTReader reader = new WKTReader(geometryFactory);
        try {
            Polygon polygon = (Polygon) reader.read(pg.toString().replace("SRID=3857;", ""));
            Point point = polygon.getCentroid();
            return new double[]{point.getX(), point.getY()};
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException();
        }
    }

    public static PGgeometry genPolygon(double[][] xyarr, int srid) {
        String text = "SRID=#{srid}; POLYGON(#{polygon})";
        text = text.replace("#{srid}", String.valueOf(srid));
        StringBuilder polygon = new StringBuilder("(");
        for (double[] xy : xyarr) {
            polygon.append(xy[0]).append(" ").append(xy[1]).append(",");
        }
        polygon = new StringBuilder(polygon.substring(0, polygon.length() - 1)).append(")");
        text = text.replace("#{polygon}", polygon);
        try {
            return new PGgeometry(text);
        } catch (Exception e) {
            log.error("genpolygon err : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static PGgeometry genPoint(double x, double y, int srid) {
        String text = "SRID=#{srid}; POINT(#{x} #{y})";
        text = text.replace("#{srid}", String.valueOf(srid));
        text = text.replace("#{x}", String.valueOf(x));
        text = text.replace("#{y}", String.valueOf(y));
        try {
            return new PGgeometry(text);
        } catch (Exception e) {
            log.error("genpotin err : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static PGgeometry genPoint(Node node, int srid) {
        Coord coord = node.getCoord();
        String text = "SRID=#{srid}; POINT(#{x} #{y})";
        text = text.replace("#{srid}", String.valueOf(srid));
        text = text.replace("#{x}", String.valueOf(coord.getX()));
        text = text.replace("#{y}", String.valueOf(coord.getY()));
        try {
            return new PGgeometry(text);
        } catch (Exception e) {
            log.error("genpotin err : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }


    public static PGgeometry genWay(List<OSMNode> nodes, int srid) {
        String text = "SRID=#{srid}; LINESTRING(#{points})";
        text = text.replace("#{srid}", String.valueOf(srid));
        StringBuilder sb = new StringBuilder();
        for (OSMNode node : nodes) {
            switch (srid) {
                case MKT: { // 墨卡托
                    sb.append("(").append(node.getX()).append(" ").append(node.getY()).append("),");
                    break;
                }
                case 4326: { // 经纬度
                    sb.append("(").append(node.getLon()).append(" ").append(node.getLat()).append("),");
                    break;
                }
            }
        }
        text = text.replace("#{points}", sb.substring(0, sb.length() - 1));
        try {
            return new PGgeometry(text);
        } catch (Exception e) {
            log.error("genWay err : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static PGgeometry genLine(Link link, int srid) {
        String text = "SRID=#{srid}; LINESTRING(#{points})";
        text = text.replace("#{srid}", String.valueOf(srid));
        StringBuilder points = new StringBuilder();
        Coord from = link.getFromNode().getCoord();
        points.append(from.getX()).append(" ").append(from.getY()).append(",");
        Coord to = link.getToNode().getCoord();
        points.append(to.getX()).append(" ").append(to.getY());
        text = text.replace("#{points}", points);
        try {
            return new PGgeometry(text);
        } catch (Exception e) {
            log.error("genline err : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static PGgeometry genLine(Coord from, Coord to, int srid) {
        String text = "SRID=#{srid}; LINESTRING(#{points})";
        text = text.replace("#{srid}", String.valueOf(srid));
        StringBuilder points = new StringBuilder();
        points.append(from.getX()).append(" ").append(from.getY()).append(",");
        points.append(to.getX()).append(" ").append(to.getY());
        text = text.replace("#{points}", points);
        try {
            return new PGgeometry(text);
        } catch (Exception e) {
            log.error("genline err : " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public static double[] point2xy(PGgeometry geometry) {
        double x, y;
        if (geometry.getGeoType() != 1) {
            throw new RuntimeException("geometry type is not point .");
        }
        String val = geometry.getValue();
        val = val.substring(val.indexOf("(") + 1, val.indexOf(")"));
        String[] strxy = val.split(" ");
        x = Double.parseDouble(strxy[0]);
        y = Double.parseDouble(strxy[1]);
        return new double[]{x, y};
    }


}
