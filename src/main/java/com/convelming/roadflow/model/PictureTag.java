package com.convelming.roadflow.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@ToString
public class PictureTag {

    private PictureTag() {
    }

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    /**
     * 图片高度
     */
    private String image_height;
    /**
     * 图片宽度
     */
    private String image_width;
    /**
     * 纬度
     */
    private String gps_latitude;
    /**
     * 经度
     */
    private String gps_longitude;
    /**
     * gps坐标系
     */
    private String gps_map_datum;
    /**
     * 高度
     */
    private String gps_altitude;
    /**
     * 拍摄时间
     */
    private String date_time;
    /**
     * 文件名
     */
    private String file_name;
    /**
     * 文件大小
     */
    private String file_size;

    public static PictureTag readPicture(File pic) {
        PictureTag ptag = new PictureTag();
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(pic);
        } catch (IOException | ImageProcessingException e) {
            log.error(e.getMessage(), e);
            return null;
        }
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                switch (tag.getTagName()) {
                    case "Exif Image Height":
                        ptag.image_height = tag.getDescription();
                        break;
                    case "Exif Image Width":
                        ptag.image_width = tag.getDescription();
                        break;
                    case "GPS Latitude":
                        ptag.gps_latitude = tag.getDescription();
                        break;
                    case "GPS Longitude":
                        ptag.gps_longitude = tag.getDescription();
                        break;
                    case "GPS Map Datum":
                        ptag.gps_map_datum = tag.getDescription();
                        break;
                    case "GPS Altitude":
                        ptag.gps_altitude = tag.getDescription();
                        break;
                    case "Date/Time":
                        ptag.date_time = tag.getDescription();
                        break;
                    case "File Name":
                        ptag.file_name = tag.getDescription();
                        break;
                    case "File Size":
                        ptag.file_size = tag.getDescription();
                        break;
                    default:
                        break;
                }
            }
        }
        return ptag;
    }

    /**
     * 获取图片宽度
     */
    public double getWidth() {
        if (image_width != null) {
            try {
                String width = image_width.split(" ")[0];
                return Double.parseDouble(width);
            } catch (Exception e) {
                log.error("获取图片宽度出错", e);
                return 0.;
            }
        }
        return 0.;
    }

    /**
     * 获取图片高度
     */
    public double getHeight() {
        if (image_height != null) {
            try {
                String height = image_height.split(" ")[0];
                return Double.parseDouble(height);
            } catch (Exception e) {
                log.error("获取图片高度出错", e);
                return 0.;
            }
        }
        return 0.;
    }

    /**
     * 获取拍摄经度
     */
    public double getLon() {
        if (gps_longitude != null) {
            try {
                String[] lon = gps_longitude.
                        replaceAll("\"", "").
                        replaceAll("°", "").
                        replaceAll("'", "").
                        split(" ");
                BigDecimal bd = new BigDecimal(lon[0]) // 度
                        .add(BigDecimal.valueOf(Double.parseDouble(lon[1]) / 60)) // 分
                        .add(BigDecimal.valueOf(Double.parseDouble(lon[2]) / 3600));// 秒
                return bd.doubleValue();
            } catch (Exception e) {
                log.error("经度获取出错", e);
                return 0.;
            }
        }
        return 0.;
    }

    /**
     * 获取拍摄纬度
     */
    public double getLat() {
        if (gps_latitude != null) {
            try {
                String[] lat = gps_latitude.
                        replaceAll("\"", "").
                        replaceAll("°", "").
                        replaceAll("'", "").
                        split(" ");
                BigDecimal bd = new BigDecimal(lat[0]) // 度
                        .add(BigDecimal.valueOf(Double.parseDouble(lat[1]) / 60)) // 分
                        .add(BigDecimal.valueOf(Double.parseDouble(lat[2]) / 3600));// 秒
                return bd.doubleValue();
            } catch (Exception e) {
                log.error("纬度获取出错", e);
                return 0.;
            }
        }
        return 0.;
    }

    /**
     * 获取拍摄时间
     */
    public Date getDateTime() {
        try {
            return sdf.parse(date_time);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取文件大小
     */
    public String getFileSize() {
        return file_size;
    }

    /**
     * 获取拍摄高度
     */
    public double getAltitude() {
        if (gps_altitude != null) {
            String altitude = gps_altitude.split(" ")[0];
            try {
                return Double.parseDouble(altitude);
            } catch (Exception e) {
                log.error("获取拍摄高度出错", e);
                return 0.;
            }
        }
        return 0.;
    }

    /**
     * 获取文件名
     */
    public String getFileName() {
        return file_name;
    }

    /**
     * 获取坐标系
     */
    public String getMapDatum() {
        return gps_map_datum;
    }

}
