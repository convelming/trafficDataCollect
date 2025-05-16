EARTH_RADIUS = 20037508.3427892;

// 地图默认样式序号，如果地图样式列表中没有这个样式，则默认序号为0的样式
DEFAULT_MAP_LAYER_STYLE_INDEX = 5;

// 地图样式列表
// {
//   style_name: "",
//   background: 0xd9ecff,
//   max_zoom: 18,
//   min_zoom: 0,
//   x_offset: 0,
//   y_offset: 0,
//   get_url: function () {
//     return `http://192.168.60.231:23334/osm/MapTilerBasic/${this.zoom}/${this.row}/${this.col}.png`;
//   }
// }
MAP_LAYER_STYLE = [
  {
    style_name: "POSITRON",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/Positron/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "OSM_LIDERTY",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/OSMLiberty/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "MAP_TILER_BASIC",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/MapTilerBasic/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "DARK_MATTER",
    background: `#0a4173`,
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/DarkMatter/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "OSM_BROGHT",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/OSMBroght/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "LIBERTY",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/liberty/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "MAPBOX",
    getUrl: function () {
      return `https://api.mapbox.com/styles/v1/convel/ck8frzi262yko1invkvbif5aw/tiles/512/${this.zoom}/${this.row}/${this.col}@2x?access_token=pk.eyJ1IjoiY29udmVsIiwiYSI6ImNtOW50Z2c0NTAyNGMybHB5Y2txcXY0NmgifQ.zM_QAebuyQtVh-A93w5wyA`
    }
  },
  // {
  //   style_name: "卫星图",
  //   getUrl: function () {
  //     return `http://192.168.60.231:23334/baidu/satellite/${this.zoom}/${this.row}/${this.col}.jpg`;
  //   },
  // },
  {
    style_name: "Arcgis",
    background: `#0a4173`,
    getUrl: function () {
      const { x, y, zoom } = this;
      const width = EARTH_RADIUS / Math.pow(2, zoom);
      const bbox = `${x - width},${y - width},${x + width},${y + width}`;
      const bboxSR = "3857";
      const imageSR = "3857";

      return `http://192.168.60.232:9195/mserver/arcgis/rest/services/csjt/%E5%B9%BF%E4%B8%9C%E7%9C%81wgs/MapServer/export?dpi=96&transparent=true&format=png8&layers=show:0,1,2,3&bbox=${bbox}&f=image&bboxSR=${bboxSR}&imageSR=${imageSR}`;
    },
  },
  {
    style_name: "极夜蓝",
    background: `#0a4173`,
    getUrl: function () {
      return `https://api.mapbox.com/styles/v1/dasin/cltigm5bp010s01ptciblgffl/tiles/512/${this.zoom}/${this.row}/${this.col}@2x?access_token=pk.eyJ1IjoiY29udmVsIiwiYSI6ImNtOW50Z2c0NTAyNGMybHB5Y2txcXY0NmgifQ.zM_QAebuyQtVh-A93w5wyA`
    }
  },
];

// geojson地图坐标系转换配置
try {
  proj4.defs("EPSG:4526", "+proj=tmerc +lat_0=0 +lon_0=114 +k=1 +x_0=38500000 +y_0=0 +ellps=GRS80 +units=m +no_defs");
  proj4.defs("EPSG:4547", "+proj=tmerc +lat_0=0 +lon_0=114 +k=1 +x_0=500000 +y_0=0 +ellps=GRS80 +units=m +no_defs +type=crs");
  proj4.defs("urn:ogc:def:crs:OGC:1.3:CRS84", proj4.defs("EPSG:4326"));
} catch (error) {
  console.error(error);
}
