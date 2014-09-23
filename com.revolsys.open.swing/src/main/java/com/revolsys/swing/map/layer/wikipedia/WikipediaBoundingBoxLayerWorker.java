package com.revolsys.swing.map.layer.wikipedia;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.geonames.GeoNamesService;
import com.revolsys.swing.map.layer.record.BoundingBoxRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class WikipediaBoundingBoxLayerWorker extends
AbstractSwingWorker<List<LayerRecord>, Void> {

  public static BoundingBoxRecordLayer create(
    final Map<String, Object> properties) {
    final GeometryFactory wgs84 = GeometryFactory.floating3(4326);
    final BoundingBoxRecordLayer layer1 = new BoundingBoxRecordLayer(
      "wikipedia", "Wikipedia Articles", WikipediaBoundingBoxLayerWorker.class,
      wgs84);

    final BufferedImage image = Icons.getImage("wikipedia");
    final ImageMarker marker = new ImageMarker(image);
    final MarkerStyle style = new MarkerStyle();
    style.setMarker(marker);
    layer1.setRenderer(new MarkerStyleRenderer(layer1, style));
    final BoundingBoxRecordLayer layer = layer1;
    layer.setProperties(properties);
    return layer;
  }

  private final BoundingBoxRecordLayer layer;

  private final BoundingBox boundingBox;

  private final GeoNamesService geoNamesService = new GeoNamesService();

  private final GeometryFactory geometryFactory;

  public WikipediaBoundingBoxLayerWorker(final BoundingBoxRecordLayer layer,
    final BoundingBox boundingBox) {
    this.layer = layer;
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
  }

  @Override
  protected List<LayerRecord> doInBackground() throws Exception {
    BoundingBox boundingBox = this.boundingBox;
    GeometryFactory geometryFactory = this.geometryFactory;
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      geometryFactory = projCs.getGeographicCoordinateSystem()
          .getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
    }
    final List<LayerRecord> results = (List)this.geoNamesService.getWikipediaArticles(boundingBox);
    for (final Record record : results) {
      final String title = record.getValue("title");
      final String wikipediaUrl = record.getValue("wikipediaUrl");
      final String thumbnailImage = record.getValue("thumbnailImg");
      final Point point = record.getGeometryValue();
      String text;
      if (thumbnailImage != null) {
        text = "<html><b>" + title + "</b><br /><img src=\"" + thumbnailImage
            + "\" /><br /></html>";
      } else {
        text = "<html><b>" + title + "</b><br /></html>";
      }

      // if (viewport instanceof ComponentViewport2D) {
      // final ComponentViewport2D componentViewport =
      // (ComponentViewport2D)viewport;
      // componentViewport.addHotSpot(geometryFactory, point, text, "http://"
      // + wikipediaUrl);
      // }
    }
    return results;
  }

  @Override
  public String toString() {
    return "Load Wikipedia Articles";
  }

  @Override
  protected void uiTask() {
    try {
      final List<LayerRecord> records = get();
      this.layer.setIndexRecords(this.boundingBox, records);
    } catch (final Throwable e) {
      this.layer.setIndexRecords(this.boundingBox, null);
    }
  }
}
