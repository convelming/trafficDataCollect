EARTH_RADIUS = 20037508.3427892;

// 地图默认样式序号，如果地图样式列表中没有这个样式，则默认序号为0的样式
DEFAULT_MAP_LAYER_STYLE_INDEX = 0;

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
    style_name: "OSM OpenMapTiles",
    getUrl: function () {
      return `http://192.168.60.234:8081/styles/OSM OpenMapTiles/512/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "DK",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/DK/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "CSJT",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/CSJT/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "DRAKBlue",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/DRAKBlue/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "LightBlue",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/LightBlue/${this.zoom}/${this.row}/${this.col}.png`;
    },
  },
  {
    style_name: "Bing地图",
    background: `#0a4173`,
    x_offset: -590,
    y_offset: 335,
    getUrl: function () {
      const { zoom, col, row } = this;
      let quadKey = "";
      for (let i = zoom; i > 0; i--) {
        let digit = "0";
        const mask = 1 << (i - 1);
        if ((row & mask) !== 0) {
          digit = String.fromCharCode(digit.charCodeAt(0) + 1);
        }
        if ((col & mask) !== 0) {
          digit = String.fromCharCode(digit.charCodeAt(0) + 2);
        }
        quadKey += digit;
      }
      // return `https://t0.dynamic.tiles.ditu.live.com/comp/ch/${quadKey}?mkt=zh-CN,en-US&ur=cn&it=G,L&jp=0&og=1&sv=9.27&n=t&o=webp,95&cstl=VBD&st=bld|v:0`;
      return `https://t0.dynamic.tiles.ditu.live.com/comp/ch/${quadKey}?mkt=zh-CN&ur=cn&it=G,RL&n=z&og=942&cstl=vbd`;
    },
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
