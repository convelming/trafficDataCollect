package com.convelming.roadflow.controller;

import com.convelming.roadflow.common.Result;
import com.convelming.roadflow.enums.HighwayType;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.service.MatsimLinkService;
import com.convelming.roadflow.util.CacheUtil;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/matsim/link")
public class MatsimLinkController {

    @Resource
    private MatsimLinkService matsimLinkService;
    @Resource
    private CacheUtil cacheUtil;

    /**
     * 根据origidid获取个路段，正反两个方向
     * @return
     */
    @GetMapping("/getMatsimLink/{origid}")
    public Result getMatsimLinkByWayId(@PathVariable String origid) {
        return Result.ok(matsimLinkService.queryByOrigid(origid));
    }

    /**
     * 根据id获取反向link
     * @return
     */
    @GetMapping("/getReverseLink/{id}")
    public Result getReverseLink(@PathVariable String id) {
        return Result.ok(matsimLinkService.queryReverseLink(id));
    }

    /**
     * 根据id获取link
     * @return
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable String id) {
        return Result.ok(matsimLinkService.queryById(id));
    }

    /**
     * 根据id查询link
     * @return
     */
    @GetMapping("/getLinkId")
    public Result getLinkId(String id) {
        return Result.ok(matsimLinkService.queryLikeId(id));
    }

    /**
     * 修改link信息
     * @return
     */
    @PostMapping("/update")
    public Result update(@RequestBody MatsimLink link) {
        return Result.ok("影响行数：" + matsimLinkService.update(link));
    }

    /**
     * 修改way中所有link信息
     * @return
     */
    @PostMapping("/updateInWay")
    public Result updateInWay(@RequestBody MatsimLink link){
        return Result.ok("影响行数：" + matsimLinkService.updateInWay(link));
    }

    /**
     * 获取全部道路类型
     * @return
     */
    @GetMapping("/getAllHighwayType")
    public Result getAllHighwayType(){
        Object arr = cacheUtil.get(CacheUtil.ALL_HIGHWAY_TYPE);
        if(arr == null){
            arr = HighwayType.values();
            cacheUtil.put(CacheUtil.ALL_HIGHWAY_TYPE, arr, Integer.MAX_VALUE);
        }
        return Result.ok(arr);
    }


}
