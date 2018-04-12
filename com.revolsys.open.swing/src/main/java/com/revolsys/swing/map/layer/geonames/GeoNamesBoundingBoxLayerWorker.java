package com.revolsys.swing.map.layer.geonames;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.record.Record;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.BoundingBoxRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class GeoNamesBoundingBoxLayerWorker extends AbstractSwingWorker<List<LayerRecord>, Void> {

  public static BoundingBoxRecordLayer newLayer(final Map<String, ? extends Object> config) {
    final GeometryFactory wgs84 = GeometryFactory.floating3d(EpsgCoordinateSystems.WGS84_ID);
    final BoundingBoxRecordLayer layer = new BoundingBoxRecordLayer("geoname", "Geo Names",
      GeoNamesBoundingBoxLayerWorker.class, wgs84);

    final BufferedImage image = Icons.getImage("world");
    final ImageMarker marker = new ImageMarker(image);
    final MarkerStyle style = new MarkerStyle();
    style.setMarker(marker);
    layer.setRenderer(new MarkerStyleRenderer(layer, style));
    layer.setProperties(config);
    return layer;
  }

  private final BoundingBox boundingBox;

  private final GeometryFactory geometryFactory;

  private final GeoNamesService geoNamesService = new GeoNamesService();

  private final BoundingBoxRecordLayer layer;

  public GeoNamesBoundingBoxLayerWorker(final BoundingBoxRecordLayer layer,
    final BoundingBox boundingBox) {
    this.layer = layer;
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
  }

  @Override
  protected List<LayerRecord> handleBackground() {
    BoundingBox boundingBox = this.boundingBox;
    GeometryFactory geometryFactory = this.geometryFactory;
    final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      final GeographicCoordinateSystem geoCs = projCs.getGeographicCoordinateSystem();
      geometryFactory = geoCs.getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
    }
    final List<LayerRecord> results = (List)this.geoNamesService.getNames(boundingBox);
    for (final Record record : results) {
      final String name = record.getValue("name");
      final Point point = record.getGeometry();
      final String text = "<html><b>" + name + "</b><br /></html>";

      // if (viewport instanceof ComponentViewport2D) {
      // final ComponentViewport2D componentViewport =
      // (ComponentViewport2D)viewport;
      // componentViewport.addHotSpot(geometryFactory, point, text, null);
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
    this.layer.setIndexRecords(this.boundingBox, records);
  }

  @Override
  protected void handleException(final Throwable exception) {
    super.handleException(exception);
    this.layer.setIndexRecords(this.boundingBox, null);
  }

  @Override
  public String toString() {
    return "Load Geo Names";
  }
}
