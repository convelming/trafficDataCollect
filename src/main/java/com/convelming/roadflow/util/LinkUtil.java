package com.convelming.roadflow.util;


import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LinkUtil {

    public static void main(String[] args) {
//        String path = "E:\\project\\convelming\\matsim-maas\\scenarios\\grid_network.xml";
//        String path = "E:\\project\\convelming\\matsim-maas\\scenarios\\mielec_2014_02\\network.xml";
        String path = "C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-10\\gz230427_fullPath_4526_h9.xml";
        Network network = NetworkUtils.readNetwork(path);
        List<LongLink> links = mergeLink(network);
//        System.out.println(links);
        int linknums = 0;
        for (LongLink l : links) {
            linknums += l.getLinks().size();
            // 让 link 按路线相接排序


        }
        System.out.println(linknums);
    }




    // 传入整个路网network.xml
    // 合并出入只有一个点的段落
    // # 构建映射关系保存到数据库
    public static List<LongLink> mergeLink(Network network) {
        List<LongLink> longLinks = new ArrayList<>();
        List<Link> links = new ArrayList<>(network.getLinks().values());

        Map<Id<Link>, Link> usersLink = new HashMap<>();
        int index = 0;
        while (usersLink.size() < links.size()) {
            Link link = links.get(index); // 随便拿一段路
            if (usersLink.get(link.getId()) != null) {
                index++;
                continue;
            }
            usersLink.put(link.getId(), link);

            Node to = link.getToNode();
            Node from = link.getFromNode();

            LongLink longLink = new LongLink();
            longLink.getLinks().add(link);
            longLink.getNodes().add(to);
            longLink.getNodes().add(from);

            // to 一直往前走到尽头
            // 尽头 outlink == 0 && fromnode == 当前node
            // 岔路口 outlink > 1 && fromnode == 当前node
            // 没有岔路口 outlink == 1 # 一条往前 一条往回  有没有单行道 out in 都是 1 ?
            while (to.getOutLinks().size() >= 1
//                    || to.getInLinks().size() == 1
            ) {
                Link toLink = to.getOutLinks().values().iterator().next();

                if (toLink.getToNode() == from && toLink.getFromNode() == to) { // 返回的路
                    break;
                }

                if (usersLink.get(toLink.getId()) != null) {
                    break;
                }

                longLink.getNodes().add(to);
                longLink.getLinks().add(toLink);

                to = toLink.getToNode();

                usersLink.put(toLink.getId(), toLink);
            }

            // 重置到选中路段
            to = link.getToNode();
            from = link.getFromNode();

            // from 一直往回走到尽头
            while (from.getInLinks().size() >= 1
//                    || to.getInLinks().size() == 1
            ) {
                Link fromLink = from.getInLinks().values().iterator().next();

                if (fromLink.getToNode() == from && fromLink.getFromNode() == to) {
                    break;
                }

                if (usersLink.get(fromLink.getId()) != null) {
                    break;
                }

                // 往回走
                longLink.getNodes().add(0, from);
                longLink.getLinks().add(0, fromLink);

                from = fromLink.getFromNode();

                usersLink.put(fromLink.getId(), fromLink);
            }

            if (!longLink.isNull()) {
                longLinks.add(longLink);
            }
            index++;
        }
        return longLinks;
    }


    private static class LongLink {

        public LongLink() {
            links = new ArrayList<>();
            nodes = new ArrayList<>();
        }

        String goejson; // 一段长路径的

        public boolean isNull() {
            return links.isEmpty() && nodes.isEmpty();
        }

        /**
         * 原路径
         */
        List<Link> links;

        /**
         * 包含的全部节点
         */
        List<Node> nodes;

        /**
         * 前端显示 前两个为初始点，后面每两个表示一组偏移量
         */
        List<Double> path;

        /**
         * 中心点
         */
        Coord center;

        public List<Link> getLinks() {
            return links;
        }

        public void setLinks(List<Link> links) {
            this.links = links;
        }

        public List<Node> getNodes() {
            return nodes;
        }

        public void setNodes(List<Node> nodes) {
            this.nodes = nodes;
        }

        public List<Double> getPath() {
            return path;
        }

        public void setPath(List<Double> path) {
            this.path = path;
        }

        public Coord getCenter() {
            return center;
        }

        public void setCenter(Coord center) {
            this.center = center;
        }

        @Override
        public String toString() {
            return "LongLink{" +
                    "links=" + links +
                    ", nodes=" + nodes +
                    ", path=" + path +
                    ", center=" + center +
                    '}';
        }
    }

}
