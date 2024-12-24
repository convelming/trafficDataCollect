create table if not exists public.matsim_node
(
    id   bigint           not null primary key,
    x    double precision not null,
    y    double precision not null,
    srid integer          not null default 3857,
    geom geometry(point)  not null
);

comment on table public.matsim_node is '点';
comment on column public.matsim_node.id is '主键id';
comment on column public.matsim_node.x is 'x轴坐标';
comment on column public.matsim_node.y is 'y轴坐标';
comment on column public.matsim_node.srid is '空间坐标编码';
comment on column public.matsim_node.geom is '空间坐标信息';

alter table public.matsim_node
    owner to postgres;

drop table public.matsim_link;
create table if not exists public.matsim_link
(
    id        bigint               not null primary key,
    srid      integer              not null default 3857,
    from_node bigint               not null,
    to_node   bigint               not null,
    length    double precision,
    freespeed double precision,
    capacity  double precision,
    origid    bigint,
    type      varchar,
    geom      geometry(linestring) not null,
    center    geometry(point)      not null,
    name      varchar(200),
    lane      integer
);
comment on table public.matsim_link is '路';
comment on column public.matsim_link.id is '主键id';
comment on column public.matsim_link.srid is '空间坐标编码';
comment on column public.matsim_link.from_node is '起始点';
comment on column public.matsim_link.to_node is '结束点';
comment on column public.matsim_link.length is '长度';
comment on column public.matsim_link.freespeed is '自由速度';
comment on column public.matsim_link.capacity is '容量';
comment on column public.matsim_link.origid is 'osm way id';
comment on column public.matsim_link.type is 'osm way highway';
comment on column public.matsim_link.geom is '空间坐标信息';
comment on column public.matsim_link.center is '中心点空间坐标信息';
comment on column public.matsim_link.name is '路名';
comment on column public.matsim_link.lane is '车道数';

alter table public.matsim_link
    owner to postgres;

create table if not exists public.osm_node
(
    id        bigint           not null primary key,
    version   int              not null default 1,
    timestamp date             not null default now(),
    uid       integer,
    "user"    varchar,
    changeset bigint,
    lat       double precision not null,
    lon       double precision not null,
    x         double precision,
    y         double precision,
    geom4326  geometry(point)  not null,
    geom3857  geometry(point),
    other     text
);

comment on table public.osm_node is 'osm点';
comment on column public.osm_node.id is '主键id';
comment on column public.osm_node.version is '';
comment on column public.osm_node.timestamp is '';
comment on column public.osm_node.uid is '';
comment on column public.osm_node.user is '';
comment on column public.osm_node.changeset is '';
comment on column public.osm_node.lat is '纬度';
comment on column public.osm_node.lon is '经度';
comment on column public.osm_node.x is '墨卡托x轴';
comment on column public.osm_node.y is '墨卡托y轴';
comment on column public.osm_node.geom4326 is '经纬度空间坐标信息';
comment on column public.osm_node.geom3857 is '墨卡托空间坐标信息';
comment on column public.osm_node.other is '其它信息';

alter table public.osm_node
    owner to postgres;

drop table public.osm_way;
create table public.osm_way
(
    id        bigint primary key,
    version   int                  not null default 1,
    timestamp date                 not null default now(),
    uid       integer,
    "user"    varchar,
    changeset bigint,
    oneway    bool                 not null default false,
    highway   varchar,
    nodes     json                 not null,
    geom4326  geometry(linestring) not null,
    geom3857  geometry(linestring),
    other     text,
    name      varchar(200)
);

comment on table public.osm_way is 'osm路';
comment on column public.osm_way.id is '主键id';
comment on column public.osm_way.version is '';
comment on column public.osm_way.timestamp is '';
comment on column public.osm_way.uid is '';
comment on column public.osm_way.user is '';
comment on column public.osm_way.changeset is '';
comment on column public.osm_way.oneway is '是否单行道';
comment on column public.osm_way.geom4326 is '经纬度空间坐标信息';
comment on column public.osm_way.geom3857 is '墨卡托空间坐标信息';
comment on column public.osm_way.nodes is 'node列表';
comment on column public.osm_way.other is '其他信息';
comment on column public.osm_way.name is '路名';

alter table public.osm_way
    owner to postgres;

create table public.link_stats
(
    id          bigint primary key,
    link_id     bigint           not null,
    way_id      bigint           not null,
    begin_time  date             not null,
    end_time    date             not null,
    "type"      varchar default 'to',
    pcu_h       int     default 0,
    x           double precision not null,
    y           double precision not null,
    remark      text,
    ip_addr     varchar          not null,
    version     int     default 0,
    deleted     bigint  default 0,
    create_time date    default now(),
    update_time date    default now(),
    video       text,
    scar        double precision,
    struck      double precision,
    mcar        double precision,
    mtruck      double precision,
    lcar        double precision,
    ltruck      double precision,
    is_two_way  boolean
);

comment on table public.link_stats is '路段流量';
comment on column public.link_stats.id is '主键id';
comment on column public.link_stats.link_id is 'natsim link id';
comment on column public.link_stats.way_id is 'osm way id';
comment on column public.link_stats.begin_time is '开始时间';
comment on column public.link_stats.end_time is '结束时间';
comment on column public.link_stats.type is '调查方式 ';
comment on column public.link_stats.pcu_h is 'pcu/h';
comment on column public.link_stats.x is '中心的 x 坐标';
comment on column public.link_stats.y is '中心的 y 坐标';
comment on column public.link_stats.remark is '备注';
comment on column public.link_stats.ip_addr is '登记ip';
comment on column public.link_stats.version is '版本号';
comment on column public.link_stats.deleted is '逻辑删除 0 未删除';
comment on column public.link_stats.create_time is '创建时间';
comment on column public.link_stats.update_time is '修改时间';
comment on column public.link_stats.video is '修改时间';
comment on column public.link_stats.scar is '小客车';
comment on column public.link_stats.struck is '小货车';
comment on column public.link_stats.mcar is '中客车';
comment on column public.link_stats.mtruck is '中货车';
comment on column public.link_stats.lcar is '大客车';
comment on column public.link_stats.ltruck is '大货车';
comment on column public.link_stats.is_two_way is '是否双向路';
alter table public.link_stats
    owner to postgres;

-- 十字路
drop table if exists crossroads;
create table crossroads
(
    id              bigint primary key,
    intersection_id bigint  not null,
    vertex          json, -- 顶点[x1,y1, x2,y2, x3,y3, x4,y4 ...]
    in_link_id      json, --
    video           varchar(255),
    "type"          varchar(1),
    annex           text,
    map_info        text,
    status          int8,
    lines           text,
    video_type      integer default 1,
    begin_time      timestamp,
    end_time        timestamp,
    remark          text,
    ip_addr         varchar not null,
    version         int     default 0,
    deleted         bigint  default 0,
    create_time     date    default now(),
    update_time     date    default now()
);
comment on table public.crossroads is '十字路';
comment on column public.crossroads.id is '';
comment on column public.crossroads.intersection_id is '';
comment on column public.crossroads.vertex is '顶点[x1,y1, x2,y2, x3,y3, x4,y4 ...]';
comment on column public.crossroads.in_link_id is '范围内link_id';
comment on column public.crossroads.video is '视频地址';
comment on column public.crossroads.status is '视频状态';
comment on column public.crossroads.lines is '绘制线数据';
comment on column public.crossroads.video_type is '拍摄类型（1俯视航拍，2侧面路拍，3正斜角拍摄';
comment on column public.crossroads.begin_time is '开始时间';
comment on column public.crossroads.end_time is '结束时间';
comment on column public.crossroads."type" is '录入类型';
comment on column public.crossroads.annex is '附件';
comment on column public.crossroads.map_info is '地图旋转缩放信息';
comment on column public.crossroads.remark is '备注';
comment on column public.crossroads.ip_addr is '登记ip';
comment on column public.crossroads.version is '版本号';
comment on column public.crossroads.deleted is '逻辑删除 0 未删除';
comment on column public.crossroads.create_time is '创建时间';
comment on column public.crossroads.update_time is '修改时间';
alter table public.crossroads
    owner to postgres;

-- 十字路流量表
drop table if exists crossroads_stats;
create table crossroads_stats
(
    id            bigint primary key,
    crossroads_id bigint,
    in_link       varchar(100),
    out_link      varchar(100),
    pcu_h         double precision,
    pcu_detail    text,
    count         double precision,
    deleted       bigint  default 0,
    result_id     varchar(100),
    van           integer default 0,
    truck         integer default 0,
    bus           integer default 0,
    car           integer default 0,
    end_point     text,
    start_point   text
);
comment on table public.crossroads_stats is '十字路流量';
comment on column public.crossroads_stats.id is 'id';
comment on column public.crossroads_stats.crossroads_id is '十字路id';
comment on column public.crossroads_stats.in_link is '进link';
comment on column public.crossroads_stats.out_link is '出link';
comment on column public.crossroads_stats.pcu_h is 'PCU/H';
comment on column public.crossroads_stats.count is '总数';
comment on column public.crossroads_stats.deleted is '是否删除，0未删除';
comment on column public.crossroads_stats.result_id is '视频识别结果id';
comment on column public.crossroads_stats.van is '小货车';
comment on column public.crossroads_stats.truck is '大货车';
comment on column public.crossroads_stats.bus is '大客车';
comment on column public.crossroads_stats.car is '小客车';
comment on column public.crossroads_stats.end_point is '终点';
comment on column public.crossroads_stats.start_point is '起点';
alter table public.crossroads_stats
    owner to postgres;

create table intersection
(
    id          bigint primary key,
    x           double precision not null,
    y           double precision not null,
    geom        geometry(point)  not null,
    name        varchar(100),
    status      int    default 0,
    version     int    default 0,
    deleted     bigint default 0,
    create_time date   default now(),
    update_time date   default now()
);
comment on table public.intersection is '十字路中心';
comment on column public.intersection.id is '主键id';
comment on column public.intersection.x is 'x坐标';
comment on column public.intersection.y is 'y坐标';
comment on column public.intersection.geom is 'geometry';
comment on column public.intersection.name is '名称';
comment on column public.intersection.status is '是否已有录入数据，0未录入，1已录入';
comment on column public.intersection.version is '版本号';
comment on column public.intersection.deleted is '逻辑删除，0未删除';
comment on column public.intersection.create_time is '创建时间';
comment on column public.intersection.update_time is '更新时间';

create table map_picture
(
    id          bigint primary key,
    geom        geometry(point),
    x           double precision,
    y           double precision,
    lon         double precision,
    lat         double precision,
    "path"      varchar(100),
    "name"      varchar(100),
    remark      text,
    ip_addr     varchar not null,
    version     int    default 0,
    deleted     bigint default 0,
    create_time date   default now(),
    update_time date   default now()
);
comment on table public.map_picture is '地图图片';
comment on column public.map_picture.id is 'id';
comment on column public.map_picture.x is 'x坐标';
comment on column public.map_picture.y is 'y坐标';
comment on column public.map_picture.geom is '点位置';
comment on column public.map_picture.lon is '经度';
comment on column public.map_picture.lat is '纬度';
comment on column public.map_picture.path is '图片地址';
comment on column public.map_picture.name is '图片名';
comment on column public.map_picture.remark is '备注';
comment on column public.map_picture.ip_addr is 'ip';
comment on column public.map_picture.version is '版本号';
comment on column public.map_picture.deleted is '逻辑删除，0未删除';
comment on column public.map_picture.create_time is '创建时间';
comment on column public.map_picture.update_time is '更新时间';