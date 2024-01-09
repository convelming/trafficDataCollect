package com.convelming.roadflow.model.vo;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsAvg {

    private Integer hour;

    private Double scar;

    private Double mcar;

    private Double lcar;

    private Double struck;

    private Double mtruck;

    private Double ltruck;

    private Double pcu_h;

//    select
//    to_char(begin_time, 'HH24') as "hour",
//    avg(pcu_h) as "pcu",
//    avg(scar / (extract(epoch from end_time - begin_time) / 3600)) as "scar",
//    avg(mcar / (extract(epoch from end_time - begin_time) / 3600)) as "mcar",
//    avg(lcar / (extract(epoch from end_time - begin_time) / 3600)) as "lcar",
//    avg(struck / (extract(epoch from end_time - begin_time) / 3600)) as "struck",
//    avg(mtruck / (extract(epoch from end_time - begin_time) / 3600)) as "mtruck",
//    avg(ltruck / (extract(epoch from end_time - begin_time) / 3600)) as "ltruck"
//    from link_stats
//    where link_id = '434456'
//    id in ()
//    group by to_char(begin_time, 'HH24');

}
