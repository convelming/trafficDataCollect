package com.convelming.roadflow.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class FileUtil {

    /**
     * 解压zip文件
     *
     * @param zip    zip文件地址
     * @param output 输出目录
     */
    public static void unzip(String zip, String output) throws IOException {
        new File(output).mkdirs();
        ZipFile zipFile = new ZipFile(zip);
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
