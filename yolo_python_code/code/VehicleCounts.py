import cv2 #opencv-python  4.9.0.80
import pandas as pd #pandas 2.0.3
from ultralytics import YOLO #ultralytics 8.1.35
from collections import defaultdict
import os
import sys

def box_label(image, box, label='', color=(128, 128, 128), txt_color=(255, 255, 255)):
    # 得到目标矩形框的左上角和右下角坐标
    p1, p2 = (int(box[0]), int(box[1])), (int(box[2]), int(box[3]))
    # 绘制矩形框
    cv2.rectangle(image, p1, p2, color, thickness=1, lineType=cv2.LINE_AA)
    if label:
        # 得到要书写的文本的宽和长，用于给文本绘制背景色
        w, h = cv2.getTextSize(label, 0, fontScale=2 / 3, thickness=1)[0]
        # 确保显示的文本不会超出图片范围
        outside = p1[1] - h >= 3
        p2 = p1[0] + w, p1[1] - h - 3 if outside else p1[1] + h + 3
        # cv2.rectangle(image, p1, p2, color, -1, cv2.LINE_AA)     #填充颜色
        # 书写文本
        cv2.putText(image,
                    label, (p1[0], p1[1] - 2 if outside else p1[1] + h + 2),
                    0,
                    0.2,
                    txt_color,
                    thickness=1,
                    lineType=cv2.LINE_AA)


def vehCrossLine(l1x1, l1y1, l1x2, l1y2, lastX, lastY, thisX, thisY):
    # 快速排斥实验首先判断两条线段在x以及y坐标的投影是否有重合。 有一个为真，则代表两线段必不可交。
    if max(l1x1, l1x2) < min(lastX, thisX) or max(l1y1, l1y2) < min(lastY, thisY) or max(lastX, thisX) < min(l1x1,
                                                                                                             l1x2) or max(
            lastY, thisY) < min(l1y1, l1y2):
        return False
    # 跨立实验如果相交则矢量叉积异号或为零，大于零则不相交
    if (((l1x1 - lastX) * (thisY - lastY) - (l1y1 - lastY) * (thisX - lastX)) * (
            (l1x2 - lastX) * (thisY - lastY) - (l1y2 - lastY) * (thisX - lastX))) > 0 or (
            ((lastX - l1x1) * (l1y2 - l1y1) - (lastY - l1y1) * (l1x2 - l1x1)) * (
            (thisX - l1x1) * (l1y2 - l1y1) - (thisY - l1y1) * (l1x2 - l1x1))) > 0:
        return False
    return True

def vehCount(input_line,input_video,input_model,output_result):

    folder = input_line
    for file_name in os.listdir(folder):
        lines = pd.read_csv(folder + '/' + file_name, header=None)

    folder = input_video
    for file_name in os.listdir(folder):
        video_capture = cv2.VideoCapture(folder + '/' + file_name)

    fps = video_capture.get(cv2.CAP_PROP_FPS)#帧率

    turning = [line[0] + line2[0] for _, line in lines.iterrows() for _, line2 in lines.iterrows() if line[0] != line2[0]]
    turningCountDict = {t: {"0": 0, "1": 0, "2": 0, "3": 0} for t in turning}

    folder = input_model
    for file_name in os.listdir(folder):
        model = YOLO(folder + '/' + file_name)

    # model = YOLO(input_model)
    # model.to('cuda')
    track_history = defaultdict(lambda: [])

    flowCountWithoutType = {line[0]: {"0": 0, "1": 0, "2": 0, "3": 0} for _, line in lines.iterrows()}
    # turningInfoDict = {'bk': "右进左转", 'dg': '下进左转', 'hl': '上进左转', 'fj': '左进左转'}
    vehiCrosHistoryDict = {}  # {'vehId':{'previous':'I','current':'VI'}}
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    output_video = cv2.VideoWriter(output_result+'/output_video.mp4',fourcc,30.0,(int(video_capture.get(3)) , int(video_capture.get(4))))

    all_track_ids = []
    # 逐帧处理视频
    dict_car_type = {'0': '中小客车','1': '大客车','2': '中小货车','3': '大货车'}
    iFrame = 0
    while (video_capture.isOpened()):

        ret, frame = video_capture.read()

        if ret == True:

            # cv2.imwrite("/users/convel/desktop/firstFrame.jpg",frame)
            # cv2.line(frame, (100, 100), (500, 100), (0, 255, 0), 2)  # 从 (100, 100) 到 (500, 100) 画一条绿色的线，线宽为 2
            results = model.track(frame, conf=0.01, persist=True, device='cpu')
            if results[0].boxes.id is not None:

                track_ids = results[0].boxes.id.int().cpu().tolist()
                track_types = results[0].boxes.cls.int().cpu().tolist()
                all_track_ids.extend(track_ids)
                # 遍历该帧的所有目标
                #         for track_id, box in zip(track_ids, results[0].boxes.data):
                for track_id, box, track_type in zip(track_ids, results[0].boxes.data, track_types):
                    # print(track_id, track_type)
                    # 绘制该目标的矩形框
                    box_label(frame, box, '#' + str(track_id) + ' car', (167, 146, 11))
                    # 得到该目标矩形框的中心点坐标(x, y)
                    x1, y1, x2, y2 = box[:4]
                    x = (x1 + x2) / 2
                    y = (y1 + y2) / 2
                    # 提取出该ID的以前所有帧的目标坐标，当该ID是第一次出现时，则创建该ID的字典
                    track = track_history[track_id]
                    track.append((float(x), float(y)))
                    if len(track) > 1:
                        for index, row in lines.iterrows():  # go through and check i
                            if vehCrossLine(row[1], row[2], row[3], row[4], track[-2][0], track[-2][1], track[-1][0],
                                            track[-1][1]):
                                # flowCountWithType[line[0]][box-1] += 1
                                flowCountWithoutType[row[0]][str(track_type)] += 1
                                if track_id in vehiCrosHistoryDict:
                                    if 'previous' in vehiCrosHistoryDict[track_id]:
                                        vehiCrosHistoryDict[track_id]['current'] = row[0]
                                else:
                                    vehiCrosHistoryDict[track_id] = {'previous': row[0]}
            for key, value in vehiCrosHistoryDict.items():
                if 'current' in value and 'previous' in value and 'counted' not in value:
                    #  {'bk': "右进左转", 'dg': '下进左转', 'hl': '上进左转', 'fj': '左进左转'}
                    tmpStr = value['previous'] + value['current']
                    for index_num in range(len(turning)):
                        # print(index_num,turning[index_num] )
                        turnStr = turning[index_num]
                        if tmpStr == turnStr:
                            turningCountDict[turnStr][str(track_type)] += 1
                            value['counted'] = 1
            if iFrame<(10*fps):#输出10秒视频
            # 在视频指定位置画线
                for index, row in lines.iterrows():
                    cv2.line(frame, (row[1], row[2]), (row[3], row[4]), (0, 255, 0), 3)
                    cv2.putText(frame, 'ID:' + row[0], (row[1], row[2]), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 0, 255),
                                1, cv2.LINE_AA)  # 在 (x,y) 处添加红色的文字，字体大小为 1，线宽为 2
                    cv2.putText(frame,  str(list(flowCountWithoutType[row[0]].values())), (row[1], row[2]-15),
                                cv2.FONT_HERSHEY_SIMPLEX,
                                0.5, (0, 0, 255), 1, cv2.LINE_AA)
                # if box[-1] == 0:
                #     print('car')
                #         break

                cv2.putText(frame,'[car,bus,van,truck]',(10,20),cv2.FONT_HERSHEY_SIMPLEX,0.5,(0,0,255),1,cv2.LINE_AA)
                output_video.write(frame)
            # 按 'q' 键退出循环
            if cv2.waitKey(25) & 0xFF == ord('q'):
                break
        else:
            break
        # print(turningCountDict)
        iFrame += 1
    video_capture.release()

    #画轨迹
    if len(all_track_ids) > 1:
        all_track_ids = list(set(all_track_ids))
    folder = input_video
    for file_name in os.listdir(folder):
        video_capture = cv2.VideoCapture(folder + '/' + file_name)
    while(video_capture.isOpened()):
        ret, frame = video_capture.read()
        if ret == True:
            output_frame =  frame
            break
    video_capture.release()
    for track_id in all_track_ids:
        track = track_history[track_id]
        if len(track)>1:
            for track_num in range(1, len(track)):
                # print(track[track_num-1][0],track[track_num-1][1])
                cv2.line(output_frame,(round(track[track_num-1][0]),round(track[track_num-1][1])),(round(track[track_num][0]),round(track[track_num][1])),(0,0,255),1)
    cv2.imwrite(output_result+"/track.jpg", output_frame)

    #转向流量
    df_turn = pd.DataFrame([[k]+list(d.values()) for k,d in turningCountDict.items()] , columns=['id', 'car','bus','van','truck']).sort_values(by='car', ascending=False)
    df_flow = pd.DataFrame([[k]+list(d.values()) for k,d in flowCountWithoutType.items()] , columns=['id', 'car','bus','van','truck']).sort_values(by='car', ascending=False)
    df_result = pd.concat([df_turn, df_flow]).sort_values(by='car', ascending=False)
    df_result.to_csv(output_result+'/results.csv', index=False)
    print("successfully .")

if __name__ == "__main__":
    vehCount(sys.argv[1],sys.argv[2],sys.argv[3],sys.argv[4])