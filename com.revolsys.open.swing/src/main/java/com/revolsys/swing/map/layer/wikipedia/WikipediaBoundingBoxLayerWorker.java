package com.revolsys.swing.map.layer.wikipedia;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.geonames.GeoNamesService;
import com.revolsys.swing.map.layer.record.BoundingBoxRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class WikipediaBoundingBoxLayerWorker extends AbstractSwingWorker<List<LayerRecord>, Void> {

  public static BoundingBoxRecordLayer newLayer(final Map<String, ? extends Object> config) {
    final GeometryFactory wgs84 = GeometryFactory.floating3(4326);
    final BoundingBoxRecordLayer layer1 = new BoundingBoxRecordLayer("wikipedia",
      "Wikipedia Articles", WikipediaBoundingBoxLayerWorker.class, wgs84);

    final BufferedImage image = Icons.getImage("wikipedia");
    final ImageMarker marker = new ImageMarker(image);
    final MarkerStyle style = new MarkerStyle();
    style.setMarker(marker);
    layer1.setRenderer(new MarkerStyleRenderer(layer1, style));
    final BoundingBoxRecordLayer layer = layer1;
    layer.setProperties(config);
    return layer;
  }

  private final BoundingBox boundingBox;

  private final GeometryFactory geometryFactory;

  private final GeoNamesService geoNamesService = new GeoNamesService();

  private final BoundingBoxRecordLayer layer;

  public WikipediaBoundingBoxLayerWorker(final BoundingBoxRecordLayer layer,
    final BoundingBox boundingBox) {
    this.layer = layer;
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
  }

  @Override
  protected List<LayerRecord> handleBackground() {
    BoundingBox boundingBox = this.boundingBox;
    GeometryFactory geometryFactory = this.geometryFactory;
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      geometryFactory = projCs.getGeographicCoordinateSystem().getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
    }
    final List<LayerRecord> results = (List)this.geoNamesService.getWikipediaArticles(boundingBox);
    for (final Record record : results) {
      final String title = record.getValue("title");
      final String wikipediaUrl = record.getValue("wikipediaUrl");
      final String thumbnailImage = record.getValue("thumbnailImg");
      final Point point = record.getGeometry();
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
  protected void handleCancelled() {
    this.layer.setIndexRecords(this.boundingBox, null);
  }

  @Override
  protected void handleDone(final List<LayerRecord> records) {
    try {
      this.layer.setIndexRecords(this.boundingBox, records);
    } catch (final Throwable e) {
      this.layer.setIndexRecords(this.boundingBox, null);
    }
  }

  @Override
  protected void handleException(final Throwable exception) {
    super.handleException(exception);
    this.layer.setIndexRecords(this.boundingBox, null);
  }

  @Override
  public String toString() {
    return "Load Wikipedia Articles";
  }
}
