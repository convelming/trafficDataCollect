package com.convelming.roadflow.util;

import com.beust.jcommander.internal.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class FileUtil {

    public static final String[] ZIP_SUFFIX = {"zip", "rar"};

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
                dir.mkdirs();
            } else {
                InputStream inputStream = zipFile.getInputStream(entry);
                FileOutputStream outputStream = new FileOutputStream(output + File.separator + entryName);
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
