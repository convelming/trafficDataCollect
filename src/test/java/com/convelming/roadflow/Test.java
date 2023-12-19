package com.convelming.roadflow;

import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {

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

}
