package com.convelming.roadflow.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Enumeration;

@Slf4j
public class FileUtil {

    public static final String[] ZIP_SUFFIX = {"zip", "rar"};
    public static final String[] size_unit = {"B", "KB", "MB", "GB"};

    public static String formatSize(long fileSize) {
        double size = fileSize;
        int index = 0;
        while (size > 1024) {
            index++;
            size /= 1024;
            if (index > size_unit.length - 1) {
                break;
            }
        }
        return BigDecimal.valueOf(size).setScale(2, RoundingMode.HALF_UP) + size_unit[index];
    }


    public static boolean isDir(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    public static void unpack(String file, String output) {
        try {
            if (file.endsWith(".zip")) {
                unzip(file, output);
            }
            if (file.endsWith(".rar")) {
//                unrar(file, output);
            }
        } catch (Exception e) {
            log.error("解压文件失败", e);
        }
    }


    /**
     * 解压zip文件
     *
     * @param zip    zip文件地址
     * @param output 输出目录
     */
    public static void unzip(String zip, String output) throws IOException {
        new File(output).mkdirs();
        ZipFile zipFile = new ZipFile(zip, "GBK");
        Enumeration<ZipArchiveEntry> enumeration = zipFile.getEntries();
        ZipArchiveEntry entry;
        while (enumeration.hasMoreElements()) {
            entry = enumeration.nextElement();
            String entryName = entry.getName();
            if (entry.isDirectory()) {
                File dir = new File(output + File.separator + entryName);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            } else {
                InputStream inputStream = zipFile.getInputStream(entry);
                String outputFile = output + File.separator + entryName;
                File dir = new File(outputFile).getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream outputStream = new FileOutputStream(outputFile);
                byte[] buffer = new byte[1024 * 10];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.close();
                inputStream.close();
            }
        }
        zipFile.close();
    }


}
