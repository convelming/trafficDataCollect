package com.convelming.roadflow.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTReader;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GeojsonRead<T> {

    private static final WKTReader wkt = new WKTReader(JTSFactoryFinder.getGeometryFactory());
    private final List<Geojson<T>> data;
    private CoordinateTransformation ctf;
    private static final String system_crs = "epsg:3857";

    public GeojsonRead(String file, String encoding) {
        this(file, encoding, null);
    }

    public GeojsonRead(InputStream is, String encoding) {
        this(is, encoding, null);
    }

    public GeojsonRead(InputStream is, String encoding, CoordinateTransformation ct) {
        this.ctf = ct;
        this.data = new ArrayList<>();
        try {
            readFromStream(is, encoding);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read GeoJSON from input stream", e);
        }
    }

    // 统一的读取方法
    private void readFromStream(InputStream is, String encoding) throws IOException {
        StringBuilder json = new StringBuilder();
        try (InputStreamReader isr = new InputStreamReader(is, encoding)) {
            char[] chars = new char[1024];
            int len;
            while ((len = isr.read(chars)) != -1) {
                json.append(chars, 0, len);
            }
        }

        JSONObject obj = JSONObject.parseObject(json.toString());
        JSONObject crs_obj = obj.getJSONObject("crs");
        JSONObject crs_properties = crs_obj.getJSONObject("properties");
        String crs = crs_properties.getString("name");

        if (crs != null) {
            if (crs.contains("3857")) {
                this.ctf = null;
            } else if (crs.contains("4526")) {
                this.ctf = TransformationFactory.getCoordinateTransformation("EPSG:4526", system_crs);
            } else if (crs.contains("4326")) {
                this.ctf = TransformationFactory.getCoordinateTransformation("EPSG:4326", system_crs);
            } else {
                this.ctf = TransformationFactory.getCoordinateTransformation(crs, system_crs);
            }
        }

        JSONArray features = obj.getJSONArray("features");
        for (int i = 0; i < features.size(); i++) {
            JSONObject feature = features.getJSONObject(i);
            @SuppressWarnings("unchecked")
            Geojson<T> geojson = new Geojson<>((T) feature.get("properties"), buildGeometry(feature.getJSONObject("geometry")));
            this.data.add(geojson);
        }
    }


    public GeojsonRead(String file, String encoding, CoordinateTransformation ct_4326to3857) {
        this.ctf = ct_4326to3857;
        this.data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file)) {
            readFromStream(fis, encoding);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read GeoJSON file: " + file, e);
        }
    }

//
//    public void read() throws IOException {
//        StringBuilder json = new StringBuilder();
//        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), encoding);
//        int len = 0;
//        char[] chars = new char[1024];
//        while ((len = isr.read(chars)) != -1) {
//            json.append(chars, 0, len);
//        }
//
//        JSONObject obj = JSONObject.parseObject(json.toString());
//        JSONArray features = obj.getJSONArray("features");
//        for (int i = 0; i < features.size(); i++) {
//            JSONObject feature = features.getJSONObject(i);
//            Geojson<T> geojson = new Geojson<>((T) feature.get("properties"), buildGeometry(feature.getJSONObject("geometry")));

    /// /            if () { // 如果T是map对象，直接强转
    /// /                geojson = new Geojson<>((T) feature.get("properties"), buildGeometry(feature.getJSONObject("geometry")));
    /// /            } else {
    /// /                geojson = new Geojson<>(feature.getObject("properties"), buildGeometry(feature));
    /// /            }
//            this.data.add(geojson);
//        }
//    }
    public List<Geojson<T>> data() {
        return this.data;
    }

    private String buildGeometry(JSONObject obj) {
        String type = obj.getString("type");
        switch (type) {
            case "Point": {
                JSONArray coord = obj.getJSONArray("coordinates");
                ct_transform(coord);
                String geometry = "POINT(";
                geometry += coord.getString(0);
                geometry += " ";
                geometry += coord.getString(1);
                geometry += ")";
                return geometry;
            }
            case "MultiPoint": {
                String geometry = "MULTIPOINT";
                String points = obj.getJSONArray("coordinates").toString()
                        .replaceAll("\\[", "(")
                        .replaceAll("]", ")")
                        .replaceAll(",", " ");
                return geometry + points;
            }
            case "LineString": {
                JSONArray coordinates = obj.getJSONArray("coordinates");
                StringBuilder geometry = new StringBuilder("LINESTRING(");
                for (int i = 0; i < coordinates.size(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    ct_transform(coord);
                    geometry.append(coord.get(0)).append(" ").append(coord.get(1)).append(",");
                }
                geometry.deleteCharAt(geometry.length() - 1);
                geometry.append(")");
                return geometry.toString();
            }
            case "MultiLineString": {
                StringBuilder multilinestring = new StringBuilder("MULTILINESTRING(");
                JSONArray linestrings = obj.getJSONArray("coordinates");
                for (int i = 0; i < linestrings.size(); i++) {
                    JSONArray coordinates = linestrings.getJSONArray(i);
                    multilinestring.append("(");
                    for (int j = 0; j < coordinates.size(); j++) {
                        JSONArray coord = coordinates.getJSONArray(j);
                        ct_transform(coord);
                        multilinestring.append(coord.get(0)).append(" ").append(coord.get(1)).append(",");
                    }
                    multilinestring.deleteCharAt(multilinestring.length() - 1);
                    multilinestring.append("),");
                }
                multilinestring.deleteCharAt(multilinestring.length() - 1);
                multilinestring.append(")");
                return multilinestring.toString();
            }
            case "Polygon": {
                JSONArray coordinates = obj.getJSONArray("coordinates").getJSONArray(0);
                StringBuilder polygon = new StringBuilder("POLYGON((");
                for (int i = 0; i < coordinates.size(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    polygon.append(coord.get(0)).append(" ").append(coord.get(1)).append(",");
                }
                polygon.setLength(polygon.length() - 1);
                polygon.append("))");
                return polygon.toString();
            }
            case "MultiPolygon": {
                StringBuilder multipolygon = new StringBuilder("MULTIPOLYGON(");
                JSONArray polygons = obj.getJSONArray("coordinates");
                for (int i = 0; i < polygons.size(); i++) {
                    JSONArray coordinates = polygons.getJSONArray(i);
                    multipolygon.append("(");
                    for (int j = 0; j < coordinates.size(); j++) {
                        multipolygon.append("(");
                        JSONArray coords = coordinates.getJSONArray(j);
                        for (int k = 0; k < coords.size(); k++) {
                            JSONArray coord = coords.getJSONArray(k);
                            ct_transform(coord);
                            multipolygon.append(coord.get(0)).append(" ").append(coord.get(1)).append(",");
                        }
                        multipolygon.setLength(multipolygon.length() - 1);
                        multipolygon.append("),");
                    }
                    multipolygon.setLength(multipolygon.length() - 1);
                    multipolygon.append("),");
                }
                multipolygon.setLength(multipolygon.length() - 1);
                multipolygon.append(")");
                return multipolygon.toString();
            }
        }
        return "";
    }

    private void ct_transform(JSONArray array) {
        if (ctf == null) {
            return;
        }
        Coord coord = ctf.transform(new Coord(array.getDouble(0), array.getDouble(1)));
        array.set(0, coord.getX());
        array.set(1, coord.getY());
    }

    public static class Geojson<T> {

        private final T properties;
        private final String geometry;

        private Geojson(T properties, String geometry) {
            this.properties = properties;
            this.geometry = geometry;
        }

        public T properties() {
            return this.properties;
        }

        public String wkt() {
            return this.geometry;
        }

        public Geometry geometry() {
            try {
                return wkt.read(geometry);
            } catch (Exception e) {
                return null;
            }
        }

        public String toString() {
            return "properties:" + properties + ", geometry:" + geometry;
        }

    }


}
