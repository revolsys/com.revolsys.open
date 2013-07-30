package com.revolsys.swing.map.layer.wikipedia;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.algorithm.index.DataObjectQuadTree;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.swing.map.layer.InvokeMethodMapObjectFactory;
import com.revolsys.swing.map.layer.dataobject.DataObjectBoundingBoxLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.swing.map.layer.dataobject.style.marker.ImageMarker;
import com.revolsys.swing.map.layer.geonames.GeoNamesService;
import com.vividsolutions.jts.geom.Point;

public class WikipediaBoundingBoxLayerWorker extends
  SwingWorker<DataObjectQuadTree, Void> {

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "wikipedia", "Wikipedia Articles", WikipediaBoundingBoxLayerWorker.class,
    "create");

  public static DataObjectBoundingBoxLayer create(
    final Map<String, Object> properties) {
    final GeometryFactory wgs84 = GeometryFactory.getFactory(4326);
    final DataObjectBoundingBoxLayer layer1 = new DataObjectBoundingBoxLayer(
      "wikipedia", "Wikipedia Articles", WikipediaBoundingBoxLayerWorker.class,
      wgs84);

    final BufferedImage image = SilkIconLoader.getImage("wikipedia");
    final ImageMarker marker = new ImageMarker(image);
    final MarkerStyle style = new MarkerStyle();
    style.setMarker(marker);
    layer1.setRenderer(new MarkerStyleRenderer(layer1, style));
    final DataObjectBoundingBoxLayer layer = layer1;
    layer.setProperties(properties);
    return layer;
  }

  private final DataObjectBoundingBoxLayer layer;

  private final BoundingBox boundingBox;

  private final GeoNamesService geoNamesService = new GeoNamesService();

  private final GeometryFactory geometryFactory;

  public WikipediaBoundingBoxLayerWorker(
    final DataObjectBoundingBoxLayer layer, final BoundingBox boundingBox) {
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
      geometryFactory = GeometryFactory.getFactory(projCs.getGeographicCoordinateSystem());
      boundingBox = new BoundingBox(geometryFactory, boundingBox);
    }
    final List<DataObject> results = geoNamesService.getWikipediaArticles(boundingBox);
    for (final DataObject dataObject : results) {
      final String title = dataObject.getValue("title");
      final String wikipediaUrl = dataObject.getValue("wikipediaUrl");
      final String thumbnailImage = dataObject.getValue("thumbnailImg");
      final Point point = dataObject.getGeometryValue();
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
    return "Load Wikipedia Articles";
  }
}
