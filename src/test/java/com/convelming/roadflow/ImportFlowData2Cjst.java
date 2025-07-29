package com.convelming.roadflow;

import jakarta.annotation.Resource;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ImportFlowData2Cjst {

    @Resource
    JdbcTemplate jdbcTemplate;

    public static void importFlowData(){
        String insertSql = "";
    }

}
