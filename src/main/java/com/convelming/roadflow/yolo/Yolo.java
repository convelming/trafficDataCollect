package com.convelming.roadflow.yolo;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.convelming.roadflow.common.Constant;
import com.convelming.roadflow.model.Crossroads;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * <p>docker运行车辆视频识别python</p>
 * <p>使用ultralytics/ultralytics镜像</p>
 * <p>python代码cp值镜像/code目录</p>
 * <p>视频识别所需文件从目录映射到/data目录</p>
 * docker run -it -v F:\link_stats:/data --name yolo ultralytics/ultralytics:8.2.86 /bin/bash; exit;
 * docker start yolo
 * docker exec yolo python /code/VehicleCounts.py /data/{id}/input_line/ /data/{id}/input_video /data/{id}/input_model /data/{id}/output_result
 */

@Slf4j
public class Yolo {


    private static final String _log = Constant.DATA_PATH + "/data/{id}/run.log";
    private static final String errlog = Constant.DATA_PATH + "/data/{id}/err.log";

    private static void initialization(Crossroads crossroads) {
        String strId = String.valueOf(crossroads.getId());
        // 新建input目录
        mkdir(Constant.DATA_PATH + "/data/" + strId + "/input_line/");
        mkdir(Constant.DATA_PATH + "/data/" + strId + "/input_video/");
        mkdir(Constant.DATA_PATH + "/data/" + strId + "/input_model/");
        mkdir(Constant.DATA_PATH + "/data/" + strId + "/output_result/");
        // cp video
        String videoName = crossroads.getVideo().substring(crossroads.getVideo().lastIndexOf("/"));
        cp(Constant.VIDEO_PATH + crossroads.getVideo(), Constant.DATA_PATH + "/data/" + strId + "/input_video/" + videoName);
        // cp model
        cp(Constant.DATA_PATH + "/code/input_model/best.pt", Constant.DATA_PATH + "/data/" + strId + "/input_model/best.pt");
        outputLine(crossroads);
    }

    public static boolean run(Crossroads crossroads) {
        initialization(crossroads);
        String strId = String.valueOf(crossroads.getId());
        boolean result = false;
        try {
            String[] command = {"docker", "exec", "yolo", "python", "/data/code/VehicleCounts.py", "/data/data/{id}/input_line/", "/data/data/{id}/input_video", "/data/data/{id}/input_model", "/data/data/{id}/output_result"};
            command[5] = command[5].replace("{id}", strId);
            command[6] = command[6].replace("{id}", strId);
            command[7] = command[7].replace("{id}", strId);
            command[8] = command[8].replace("{id}", strId);
            log.info("运行命令：{}", Arrays.toString(command).replace(",", ""));
            Process process = new ProcessBuilder(command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // 输出错误日志
            new Thread(() -> {
                BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errlog;
                // 输出错误日志
                try (OutputStream errout = new FileOutputStream(_log.replace("{id}", strId))) {
                    while ((errlog = error.readLine()) != null) {
                        errout.write((errlog + "\n").getBytes(StandardCharsets.UTF_8));
                    }
                    errout.flush();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }).start();

            OutputStream logout = new FileOutputStream(_log.replace("{id}", strId)); // 输出日志
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("successfully")) {
                    result = true;
                }
                log.info(line);
                logout.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            }
            logout.flush();
            logout.close();
            process.waitFor();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return result;
    }

    private static void mkdir(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private static void cp(String org, String targ) {
        File orgf = new File(org);
        if (!orgf.exists()) {
            return;
        }
        File targf = new File(targ);
        if (!targf.getParentFile().exists()) {
            targf.getParentFile().mkdirs();
        }
        try (InputStream is = new FileInputStream(orgf); OutputStream os = new FileOutputStream(targf)) {
            byte[] b = new byte[2048];
            int len;
            while ((len = is.read(b)) > 0) {
                os.write(b, 0, len);
            }
            os.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void outputLine(Crossroads crossroads) {
        String strId = String.valueOf(crossroads.getId());
        try (OutputStream lineOs = new FileOutputStream(Constant.DATA_PATH + "/data/" + strId + "/input_line/line.csv")) {
            JSONArray array = JSONArray.parseArray(crossroads.getLines());
            for (Object obj : array) {
                JSONObject json = (JSONObject) obj;
                // 线名
                String row = json.getString("lineName") + ",";
                // 线起点横坐标（距离左上角向右的像素点量）
                row += json.getBigDecimal("beginx").intValue() + ",";
                // 线起点纵坐标（距离左上角向下的像素点量）
                row += json.getBigDecimal("beginy").intValue() + ",";
                // 线终点横坐标
                row += json.getBigDecimal("endx").intValue() + ",";
                // 线终点纵坐标
                row += json.getBigDecimal("endy").intValue() + ",";
                // 图片名
                row += json.getString("imageName") + ",";
                // 图片宽度（像素点量）
                row += json.getBigDecimal("width").intValue() + ",";
                // 图片高度
                row += json.getBigDecimal("height").intValue() + "\n";
                lineOs.write(row.getBytes());
            }
            lineOs.flush();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
