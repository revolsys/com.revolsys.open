package com.revolsys.swing;

import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisRestServer;
import com.revolsys.swing.map.layer.bing.Bing;
import com.revolsys.swing.map.layer.geonames.GeoNamesBoundingBoxLayerWorker;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.mapguide.MapGuideWebServer;
import com.revolsys.swing.map.layer.ogc.wms.OgcWms;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapApiLayer;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.record.FileRecordLayer;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.map.layer.record.renderer.FilterMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.ScaleMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.marker.SvgMarker;
import com.revolsys.swing.map.layer.record.style.marker.TextMarker;
import com.revolsys.swing.map.layer.webmercatortilecache.WebMercatorTileCache;
import com.revolsys.swing.map.layer.wikipedia.WikipediaBoundingBoxLayerWorker;
import com.revolsys.swing.map.symbol.SymbolLibrary;
import com.revolsys.util.ServiceInitializer;

public class RsSwingServiceInitializer implements ServiceInitializer {
  private static void markers() {
    MapObjectFactoryRegistry.newFactory("markerText", "Marker Font and Text", (config) -> {
      return new TextMarker(config);
    });
    MapObjectFactoryRegistry.newFactory("markerSvg", "Marker SVG", (config) -> {
      return new SvgMarker(config);
    });
  }

  @Override
  public void initializeService() {
    SymbolLibrary.factoryInit();
    markers();
    layerRenderers();
    layers();
  }

  private void layerRenderers() {
    MapObjectFactoryRegistry.newFactory("geometryStyle", (config) -> {
      return new GeometryStyleRenderer(config);
    });
    MapObjectFactoryRegistry.newFactory("textStyle", (config) -> {
      return new TextStyleRenderer(config);
    });
    MapObjectFactoryRegistry.newFactory("markerStyle", (config) -> {
      return new MarkerStyleRenderer(config);
    });
    MapObjectFactoryRegistry.newFactory("multipleStyle", (config) -> {
      return new MultipleRenderer(config);
    });
    MapObjectFactoryRegistry.newFactory("scaleStyle", (config) -> {
      return new ScaleMultipleRenderer(config);
    });
    MapObjectFactoryRegistry.newFactory("filterStyle", (config) -> {
      return new FilterMultipleRenderer(config);
    });
  }

  private void layers() {
    MapObjectFactoryRegistry.newFactory("layerGroup", "Layer Group", (config) -> {
      return LayerGroup.newLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("baseMapLayerGroup", "Base Map Layer Group", (config) -> {
      return BaseMapLayerGroup.newLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("recordFileLayer", "File", (config) -> {
      return FileRecordLayer.newLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("recordStoreLayer", "Record Store Layer", (config) -> {
      return new RecordStoreLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("openStreetMapVectorApi", "Open Street Map (Vector API)",
      (config) -> {
        return OpenStreetMapApiLayer.newLayer(config);
      });

    MapObjectFactoryRegistry.newFactory("gridLayer", "Grid Layer", (config) -> {
      return GridLayer.newLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("wikipedia", "Wikipedia Articles", (config) -> {
      return WikipediaBoundingBoxLayerWorker.newLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("geoname", "Geoname.org", (config) -> {
      return GeoNamesBoundingBoxLayerWorker.newLayer(config);
    });

    MapObjectFactoryRegistry.newFactory("geoReferencedImageLayer", "Geo-referenced Image Layer",
      (config) -> {
        return GeoreferencedImageLayer.newLayer(config);
      });

    ArcGisRestServer.factoryInit();

    Bing.factoryInit();

    MapGuideWebServer.factoryInit();

    OgcWms.factoryInit();

    MapObjectFactoryRegistry.newFactory("openStreetMap", "Open Street Map Tiles", (config) -> {
      return new OpenStreetMapLayer(config);
    });

    WebMercatorTileCache.factoryInit();
  }

}
