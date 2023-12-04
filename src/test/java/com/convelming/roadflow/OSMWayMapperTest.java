package com.convelming.roadflow;

import com.alibaba.fastjson.JSONObject;
import com.convelming.roadflow.mapper.OSMWayMapper;
import com.convelming.roadflow.model.OSMWay;
import com.convelming.roadflow.util.XmlUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.postgis.jdbc.PGgeometry;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class OSMWayMapperTest {


    @Resource
    private OSMWayMapper osmWayMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;


    public void sqlTest() throws SQLException, JSONException {
        long id = -1;
        OSMWay way = new OSMWay(id, "", 1, new Date(), 1L, "1", 1L, "[1,2,3,4]", new PGgeometry("SRID=4326;POINT(1 1)"), new PGgeometry("SRID=3857;POINT(1 1)"), true, "", "");
        osmWayMapper.deleteById(id);
        osmWayMapper.insert(way);
        OSMWay result = osmWayMapper.selectById(id);
        log.info(String.valueOf(result));
    }

//    @Test
    public void updateName() throws SQLException {
        List<OSMWay> ways = jdbcTemplate.query("select * from osm_way", new BeanPropertyRowMapper<>(OSMWay.class));
        ways.forEach(way -> {
            String json = way.getOther();
            if (json != null && json.length() > 0) {
                JSONObject object = JSONObject.parseObject(json);
                String name = object.getString("name");
                if (name != null) {
                    jdbcTemplate.update("update osm_way set name = ? where id = ?", new Object[]{name, way.getId()});
                }
            }
        });
    }

    //    @Test
    public void initOSMWay() throws ParseException {
        List<OSMWay> osmways = XmlUtil.loadWay("C:\\Users\\zengren\\Documents\\WeChat Files\\wxid_xg6cuaubu03v22\\FileStorage\\File\\2023-11\\gzInpoly221123.osm", "way");
//        osmways.stream().filter( way -> {
//
//        });
        osmWayMapper.batchInsert(osmways);
    }

    /*
    docker run --name postgres --restart=always -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:alpine3.18
    docker run --name postgres --restart=always -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d 192.168.60.231:8843/library/postgres:alpine3.18
     */

}
