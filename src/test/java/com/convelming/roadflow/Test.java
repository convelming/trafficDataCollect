package com.convelming.roadflow;

import com.convelming.roadflow.model.LinkStats;
import com.convelming.roadflow.model.MatsimLink;
import com.convelming.roadflow.model.proxy.MatsimLinkProxy;
import com.easy.query.api.proxy.base.MapProxy;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import jakarta.annotation.Resource;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {

    // AVRO
    private final BigDecimal HOURS = new BigDecimal("3600000");

    @org.junit.Test
    public void pcu() {
        BigDecimal pcu = BigDecimal.ZERO;
        pcu = pcu.add(new BigDecimal(399)).add(new BigDecimal(1));
        pcu = pcu.add(new BigDecimal(2).multiply(new BigDecimal("1.5"))).add(new BigDecimal(5).multiply(new BigDecimal("1.5")));
        pcu = pcu.add(new BigDecimal(13).multiply(new BigDecimal("2"))).add(new BigDecimal(7).multiply(new BigDecimal("2")));
        pcu = pcu.divide(new BigDecimal(15 * 60000), 64, RoundingMode.UP).multiply(HOURS);

        BigDecimal d = new BigDecimal("2.3656");
        System.out.println(d.setScale(2, RoundingMode.DOWN));

        System.out.println(pcu.doubleValue());
    }


    @Resource
    private EasyEntityQuery eeq;

    @org.junit.Test
    public void easyQuery() {
//        String sql = "select id, origid from " + TABLE_NAME + " where id||'' like ? order by id limit 1000";
//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(MatsimLink.class), "%" + id + "%");
        String keyword = "22087";
        List<MatsimLink> list = eeq.queryable(MatsimLink.class).where(t -> {
            t.id().like(keyword);
        }).toList();
        System.out.println(list);
    }

}
