package com.revolsys.swing.map.layer.geonames;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import com.revolsys.data.record.Record;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.record.BoundingBoxRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.marker.ImageMarker;
import com.revolsys.swing.parallel.AbstractSwingWorker;

public class GeoNamesBoundingBoxLayerWorker extends
AbstractSwingWorker<List<LayerRecord>, Void> {

  public static BoundingBoxRecordLayer create(
    final Map<String, Object> properties) {
    final GeometryFactory wgs84 = GeometryFactory.floating3(4326);
    final BoundingBoxRecordLayer layer = new BoundingBoxRecordLayer("geoname",
      "Geo Names", GeoNamesBoundingBoxLayerWorker.class, wgs84);

    final BufferedImage image = Icons.getImage("world");
    final ImageMarker marker = new ImageMarker(image);
    final MarkerStyle style = new MarkerStyle();
    style.setMarker(marker);
    layer.setRenderer(new MarkerStyleRenderer(layer, style));
    layer.setProperties(properties);
    return layer;
  }

  private final BoundingBoxRecordLayer layer;

  private final BoundingBox boundingBox;

  private final GeoNamesService geoNamesService = new GeoNamesService();

  private final GeometryFactory geometryFactory;

  public GeoNamesBoundingBoxLayerWorker(final BoundingBoxRecordLayer layer,
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
      final GeographicCoordinateSystem geoCs = projCs.getGeographicCoordinateSystem();
      geometryFactory = geoCs.getGeometryFactory();
      boundingBox = boundingBox.convert(geometryFactory);
    }
    final List<LayerRecord> results = (List)this.geoNamesService.getNames(boundingBox);
    for (final Record record : results) {
      final String name = record.getValue("name");
      final Point point = record.getGeometryValue();
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
  public String toString() {
    return "Load Geo Names";
  }

  @Override
  protected void uiTask() {
    try {
      final List<LayerRecord> index = get();
      this.layer.setIndexRecords(this.boundingBox, index);
    } catch (final Throwable e) {
      this.layer.setIndexRecords(this.boundingBox, null);
    }
  }
}
