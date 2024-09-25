// 地图样式配置
// 以列表的第一个为默认样式
// {
//   style_name: "",
//   background: 0xd9ecff,
//   max_zoom: 18,
//   min_zoom: 0,
//   x_offset: 0,
//   y_offset: 0,
//   getUrl: function () {
//     return `http://192.168.60.231:23334/osm/MapTilerBasic/${this.zoom}/${this.row}/${this.col}.png`;
//   }
// }
window.MAP_LAYER_STYLE = [
  // {
  //   style_name: "卫星图",
  //   getUrl: function () {
  //     // OMS瓦片原点 -20037508.3427892, 20037508.3427892
  //     // -20037508.3427892, 20037508.3427892 | -20037508.3427892, 0 | -20037508.3427892, -20037508.3427892
  //     // 0, 20037508.3427892 | 0, 0 | 0, -20037508.3427892
  //     // 20037508.3427892, 20037508.3427892 | 20037508.3427892, 0 | 20037508.3427892, -20037508.3427892

  //     // 百度瓦片原点 0, 0
  //     // 百度地图瓦片数量 2 * PI * R / Math.pow(2,26 - z)  R = 6378137

  //     let brow = Math.floor(this._x / Math.pow(2, 25 - this.zoom));
  //     let bcol = Math.floor(this._y / Math.pow(2, 25 - this.zoom));

  //     return `http://192.168.60.231:23334/baidu/satellite/${this.zoom + 1}/${brow}/${bcol}.jpg`
  //   }
  // },
  {
    style_name: "LIBERTY",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/liberty/${this.zoom}/${this.row}/${this.col}.png`;
    }
  },
  {
    style_name: "MAP_TILER_BASIC",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/MapTilerBasic/${this.zoom}/${this.row}/${this.col}.png`;
    }
  },
  {
    style_name: "DARK_MATTER",
    background: `#0a4173`,
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/DarkMatter/${this.zoom}/${this.row}/${this.col}.png`;
    }
  },

  {
    style_name: "OSM_BROGHT",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/OSMBroght/${this.zoom}/${this.row}/${this.col}.png`;
    }
  },
  {
    style_name: "OSM_LIDERTY",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/OSMLiberty/${this.zoom}/${this.row}/${this.col}.png`;
    }
  },
  {
    style_name: "POSITRON",
    getUrl: function () {
      return `http://192.168.60.231:23334/osm/Positron/${this.zoom}/${this.row}/${this.col}.png`;
    }
  },
  {
    style_name: "MAPBOX",
    getUrl: function () {
      return `https://api.mapbox.com/styles/v1/convel/ck8frzi262yko1invkvbif5aw/tiles/512/${this.zoom}/${this.row}/${this.col}@2x?access_token=pk.eyJ1IjoiY29udmVsIiwiYSI6ImNsaHB4cXA2MDBicGIzam1zb25zdGtiOHAifQ.UuaTujcOQlxywCJWWZ0SSg`
    }
  },
  {
    style_name: "极夜蓝",
    background: `#0a4173`,
    getUrl: function () {
      return `https://api.mapbox.com/styles/v1/dasin/cltigm5bp010s01ptciblgffl/tiles/512/${this.zoom}/${this.row}/${this.col}@2x?access_token=pk.eyJ1IjoiY29udmVsIiwiYSI6ImNsaHB4cXA2MDBicGIzam1zb25zdGtiOHAifQ.UuaTujcOQlxywCJWWZ0SSg`
    }
  },
]

