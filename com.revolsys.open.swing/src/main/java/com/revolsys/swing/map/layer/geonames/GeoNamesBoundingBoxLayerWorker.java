package com.revolsys.swing.map.layer.geonames;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.InvokeMethodLayerFactory;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.map.layer.dataobject.DataObjectBoundingBoxLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.ImageMarker;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.vividsolutions.jts.geom.Point;

public class GeoNamesBoundingBoxLayerWorker extends
  SwingWorker<DataObjectQuadTree, Void> {

  public static final LayerFactory<DataObjectBoundingBoxLayer> FACTORY = new InvokeMethodLayerFactory<DataObjectBoundingBoxLayer>(
    "geoname", "Geoname.org", GeoNamesBoundingBoxLayerWorker.class, "create");

  public static DataObjectBoundingBoxLayer create(
    final Map<String, Object> properties) {
    final GeometryFactory wgs84 = GeometryFactory.getFactory(4326);
    final DataObjectBoundingBoxLayer layer = new DataObjectBoundingBoxLayer(
      "Geo Names", GeoNamesBoundingBoxLayerWorker.class, wgs84);

    final BufferedImage image = SilkIconLoader.getImage("world");
    ImageMarker marker = new ImageMarker(image);
    MarkerStyle style = new MarkerStyle();
    style.setMarker(marker);
    layer.setRenderer(new MarkerStyleRenderer(layer, style));
    layer.setProperties(properties);
    return layer;
  }

  private final DataObjectBoundingBoxLayer layer;

  private final BoundingBox boundingBox;

  private final GeoNamesService geoNamesService = new GeoNamesService();

  private final GeometryFactory geometryFactory;

  public GeoNamesBoundingBoxLayerWorker(final DataObjectBoundingBoxLayer layer,
    final BoundingBox boundingBox) {
    this.layer = layer;
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
  }

  @Override
  protected DataObjectQuadTree doInBackground() throws Exception {
    BoundingBox boundingBox = this.boundingBox;
    GeometryFactory geometryFactory = this.geometryFactory;
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projCs = (ProjectedCoordinateSystem)coordinateSystem;
      final GeographicCoordinateSystem geoCs = projCs.getGeographicCoordinateSystem();
      geometryFactory = GeometryFactory.getFactory(geoCs);
      boundingBox = new BoundingBox(geometryFactory, boundingBox);
    }
    final List<DataObject> results = geoNamesService.getNames(boundingBox);
    for (final DataObject dataObject : results) {
      final String name = dataObject.getValue("name");
      final Point point = dataObject.getGeometryValue();
      final String text = "<html><b>" + name + "</b><br /></html>";

      // if (viewport instanceof ComponentViewport2D) {
      // final ComponentViewport2D componentViewport =
      // (ComponentViewport2D)viewport;
      // componentViewport.addHotSpot(geometryFactory, point, text, null);
      // }
    }
    final DataObjectQuadTree index = new DataObjectQuadTree(results);
    return index;
  }

  @Override
  protected void done() {
    try {
      final DataObjectQuadTree index = get();
      layer.setIndex(boundingBox, index);
    } catch (final Throwable e) {
      layer.setIndex(boundingBox, null);
    }
  }

  @Override
  public String toString() {
    return "Load Geo Names";
  }
}
