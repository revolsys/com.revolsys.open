package com.revolsys.swing;

import com.revolsys.elevation.tin.TriangulatedIrregularNetworkReaderFactory;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.arcgisrest.ArcGisRestServer;
import com.revolsys.swing.map.layer.bing.Bing;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelLayer;
import com.revolsys.swing.map.layer.elevation.tin.TriangulatedIrregularNetworkLayer;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.map.layer.grid.GridLayerRenderer;
import com.revolsys.swing.map.layer.mapguide.MapGuideWebServer;
import com.revolsys.swing.map.layer.ogc.wms.OgcWms;
import com.revolsys.swing.map.layer.openstreetmap.OpenStreetMapLayer;
import com.revolsys.swing.map.layer.pointcloud.PointCloudLayer;
import com.revolsys.swing.map.layer.raster.GeoreferencedImageLayer;
import com.revolsys.swing.map.layer.record.FileRecordLayer;
import com.revolsys.swing.map.layer.record.RecordStoreLayer;
import com.revolsys.swing.map.layer.record.ScratchRecordLayer;
import com.revolsys.swing.map.layer.record.renderer.FilterMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRecordLayerRenderer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.renderer.MultipleRecordRenderer;
import com.revolsys.swing.map.layer.record.renderer.ScaleMultipleRenderer;
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.marker.MarkerLibrary;
import com.revolsys.swing.map.layer.record.style.marker.TextMarker;
import com.revolsys.swing.map.layer.webmercatortilecache.WebMercatorTileCache;
import com.revolsys.swing.tree.TreeNodes;
import com.revolsys.swing.tree.node.file.PathTreeNode;
import com.revolsys.util.ServiceInitializer;

public class RsSwingServiceInitializer implements ServiceInitializer {

  public static EnableCheck enableCheck(final Class<? extends IoFactory> factoryClass) {
    return TreeNodes.enableCheck((final PathTreeNode node) -> node.isReadable(factoryClass));
  }

  private static void markers() {
    MapObjectFactoryRegistry.newFactory("markerText", "Marker Font and Text", TextMarker::new);
  }

  @Override
  public void initializeService() {
    MarkerLibrary.factoryInit();
    markers();
    layerRenderers();
    layers();
    GriddedElevationModelLayer.factoryInit();
    PointCloudLayer.factoryInit();
  }

  private void layerRenderers() {
    MapObjectFactoryRegistry.newFactory("geometryStyle", GeometryStyleRecordLayerRenderer::new);
    MapObjectFactoryRegistry.newFactory("textStyle", TextStyleRenderer::new);
    MapObjectFactoryRegistry.newFactory("markerStyle", MarkerStyleRenderer::new);
    MapObjectFactoryRegistry.newFactory("multipleStyle", MultipleRecordRenderer::new);
    MapObjectFactoryRegistry.newFactory("scaleStyle", ScaleMultipleRenderer::new);
    MapObjectFactoryRegistry.newFactory("filterStyle", FilterMultipleRenderer::new);
    MapObjectFactoryRegistry.newFactory("gridLayerRenderer", GridLayerRenderer::new);
  }

  private void layers() {
    MapObjectFactoryRegistry.newFactory("layerGroup", "Layer Group", LayerGroup::newLayer);

    MapObjectFactoryRegistry.newFactory("baseMapLayerGroup", "Base Map Layer Group",
      BaseMapLayerGroup::newLayer);

    AbstractLayer.menuItemPathAddLayer("record", "Add Record Layer", "map",
      RecordReaderFactory.class);

    MapObjectFactoryRegistry.newFactory("scratchRecordLayer", "File", ScratchRecordLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("recordFileLayer", "File", FileRecordLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("recordStoreLayer", "Record Store Layer",
      RecordStoreLayer::new);

    // MapObjectFactoryRegistry.newFactory("openStreetMapVectorApi", "Open
    // Street Map (Vector API)",
    // OpenStreetMapApiLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("gridLayer", "Grid Layer", GridLayer::newLayer);

    MapObjectFactoryRegistry.newFactory("geoReferencedImageLayer", "Geo-referenced Image Layer",
      GeoreferencedImageLayer::newLayer);

    AbstractLayer.menuItemPathAddLayer("image", "Add Image Layer", "picture",
      GeoreferencedImageReadFactory.class);

    MapObjectFactoryRegistry.newFactory("triangulatedIrregularNetworkLayer",
      "Triangulated Irregular Network Layer", TriangulatedIrregularNetworkLayer::new);

    AbstractLayer.menuItemPathAddLayer("tin", "Add TIN Layer", "tin",
      TriangulatedIrregularNetworkReaderFactory.class);

    ArcGisRestServer.factoryInit();

    Bing.factoryInit();

    MapGuideWebServer.factoryInit();

    OgcWms.factoryInit();

    MapObjectFactoryRegistry.newFactory("openStreetMap", "Open Street Map Tiles",
      OpenStreetMapLayer::new);

    WebMercatorTileCache.factoryInit();
  }

  @Override
  public int priority() {
    return 100;
  }

}
