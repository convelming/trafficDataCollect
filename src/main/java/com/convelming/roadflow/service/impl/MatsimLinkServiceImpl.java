package com.convelming.roadflow.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.convelming.roadflow.mapper.MatsimLinkMapper;
import com.convelming.roadflow.mapper.MatsimNodeMapper;
import com.convelming.roadflow.mapper.OSMWayMapper;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.MatsimNode;
import com.convelming.roadflow.model.OSMWay;
import com.convelming.roadflow.service.MatsimLinkService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MatsimLinkServiceImpl implements MatsimLinkService {

    @Resource
    private MatsimLinkMapper matsimLinkMapper;
    @Resource
    private OSMWayMapper osmWayMapper;
    @Resource
    private MatsimNodeMapper matsimNodeMapper;

    public List<List<MatsimLink>> queryByOrigid(Long origid) {
        List<MatsimLink> org = matsimLinkMapper.queryByOrigid(origid); // 需要分成 to , from 两组 , 按顺序连接

        OSMWay osmWay = osmWayMapper.selectById(origid);
        Long startNodeId = JSONArray.parseArray(osmWay.getNodes()).getLong(0);
        if (osmWay.getOneway()) { // 单行道
            List<MatsimLink> links = buildOneWay(org);
            buildPath(links);
            return List.of(links);
        } else {

            // to 方向 路径
            List<MatsimLink> to = new ArrayList<>();
            List<MatsimLink> from = new ArrayList<>();

            MatsimLink start = getStartLink(org, startNodeId);
            if (Objects.equals(start.getFromNode(), startNodeId)) {
                to.add(new MatsimLink(start));
                org.remove(start);
                for (int i = 0; i < org.size(); i++) {
                    MatsimLink link = org.get(i);
                    if (Objects.equals(start.getToNode(), link.getFromNode()) &&
                            !Objects.equals(start.getFromNode(), link.getToNode())) {
                        to.add(new MatsimLink(link));
                        start = link;
                        org.remove(link);
                        i = -1;
                    }
                }
                from = buildOneWay(org);
            } else { // 终点作为起点了
                to.add(new MatsimLink(start));
                org.remove(start);
                for (int i = 0; i < org.size(); i++) {
                    MatsimLink link = org.get(i);
                    if (Objects.equals(start.getToNode(), link.getFromNode()) &&
                            !Objects.equals(start.getFromNode(), link.getToNode())) {
                        to.add(new MatsimLink(link));
                        start = link;
                        org.remove(link);
                        i = -1;
                    }
                }
                to = buildOneWay(org);
            }

            buildPath(from);
            buildPath(to);

            return List.of(to, from);
        }
    }

    @Override
    public MatsimLink queryById(Long id) {
        return matsimLinkMapper.selectById(id);
    }

    @Override
    public MatsimLink queryReverseLink(Long id) {
        MatsimLink link = matsimLinkMapper.queryReverseLink(id);
        MatsimNode to = matsimNodeMapper.selectById(link.getToNode());
        MatsimNode from = matsimNodeMapper.selectById(link.getFromNode());
        link.setToxy(new Double[]{to.getX(), to.getY()});
        link.setFromxy(new Double[]{from.getX(), from.getY()});
        return link;
    }

    @Override
    public int update(MatsimLink link) {
        return matsimLinkMapper.update(link);
    }

    @Override
    public int updateInWay(MatsimLink link) {
        return matsimLinkMapper.updateInWay(link);
    }


    /**
     * 构建单行道
     * @param links 路段
     * @return
     */
    private List<MatsimLink> buildOneWay(List<MatsimLink> links) {
        List<MatsimLink> way = new ArrayList<>();
        if (links.size() <= 1) {
            return links;
        }
        MatsimLink link = links.get(0);
        way.add(new MatsimLink(link));
        links.remove(link);
        // to
        for (int i = 0; i < links.size(); i++) {
            MatsimLink temp = links.get(i);
            if (Objects.equals(link.getToNode(), temp.getFromNode())) {
                way.add(new MatsimLink(temp));
                BeanUtils.copyProperties(temp, link);
                links.remove(temp);
                i = -1;
            }
        }
        BeanUtils.copyProperties(way.get(0), link);
        // from
        for (int i = 0; i < links.size(); i++) {
            MatsimLink temp = links.get(i);
            if (Objects.equals(link.getFromNode(), temp.getToNode())) {
                way.add(0, new MatsimLink(temp));
                BeanUtils.copyProperties(temp, link);
                links.remove(temp);
                i = -1;
            }
        }
        return way;
    }

    private void buildPath(List<MatsimLink> links) {

        Set<Long> nodesId = new HashSet<>();
        for (MatsimLink link : links) {
            nodesId.add(link.getFromNode());
            nodesId.add(link.getToNode());
        }

        List<MatsimNode> nodes = matsimNodeMapper.selectByIds(nodesId);
        Map<Long, MatsimNode> nodeMap = nodes.stream().collect(Collectors.toMap(MatsimNode::getId, (x -> x)));

        for (MatsimLink link : links) {
            MatsimNode to = nodeMap.get(link.getToNode());
            MatsimNode from = nodeMap.get(link.getFromNode());
            link.setToxy(new Double[]{to.getX(), to.getY()});
            link.setFromxy(new Double[]{from.getX(), from.getY()});
        }
    }

    private MatsimLink getStartLink(List<MatsimLink> links, Long startNodeId) {
        for(MatsimLink link : links){
            if(link.getFromNode().equals(startNodeId)){
                return link;
            }
        }
        throw new RuntimeException("OSM点与Matsim点不匹配");
    }


}
