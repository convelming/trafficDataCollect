package com.convelming.roadflow.util;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class VideoUtil {

    /**
     * 保存视频封面
     *
     * @param video   视频
     * @param toimage 保存位置
     * @param type    png/jpg
     */
    public static void saveImage(String video, String toimage, ImageType type) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video);
            grabber.start();
            Frame frame;
            frame = grabber.grabImage();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            BufferedImage image = converter.getBufferedImage(frame);
            ImageIO.write(image, type.name(), new File(toimage));
            grabber.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 抽帧保存
     *
     * @param video 视频
     * @param f     第几帧
     * @param type  png/jpg
     */
    public static void extractionFrame(String video, String toimage, long f, ImageType type) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(video);
            grabber.start();
            Frame frame;
            int count = 0;
            while ((frame = grabber.grabFrame()) != null) {
                if (count == f) {
                    Java2DFrameConverter converter = new Java2DFrameConverter();
                    BufferedImage image = converter.getBufferedImage(frame);
                    ImageIO.write(image, type.name(), new File(toimage));
                    break;
                }
                count++;
            }
            grabber.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取封面宽高
     *
     * @param image 图片全地址
     * @return [width, hegiht]
     */
    public static int[] widthight(String image) {
        File imageFile = new File(image);
        try {
            BufferedImage img = ImageIO.read(imageFile);
            int width = img.getWidth();
            int height = img.getHeight();
            return new int[]{width, height};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum ImageType {
        PNG,
        JPG
    }

}
