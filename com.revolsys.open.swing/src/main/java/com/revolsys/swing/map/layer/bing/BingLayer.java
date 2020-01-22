package com.revolsys.swing.map.layer.bing;

import java.util.Map;

import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.parallel.ExecutorServiceFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.layer.raster.ViewFunctionImageLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.CaseConverter;

public class BingLayer extends AbstractLayer implements BaseMapLayer {
  public static final GeometryFactory GEOMETRY_FACTORY = GeometryFactory.floating3d(EpsgId.WGS84);

  private static final BoundingBox MAX_BOUNDING_BOX = GEOMETRY_FACTORY.newBoundingBox(-180, -85,
    180, 85);

  private BingClient client;

  private ImagerySet imagerySet = ImagerySet.Road;

  private MapLayer mapLayer;

  BingLayer() {
    super("bing");
    setIcon("bing");
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setGeometryFactory(GeometryFactory.worldMercator());
    setRenderer(new ViewFunctionImageLayerRenderer<>(this, this::newImage));
  }

  public BingLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
    setGeometryFactory(GeometryFactory.worldMercator());
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BingLayer) {
      final BingLayer layer = (BingLayer)other;
      if (layer.imagerySet == this.imagerySet) {
        if (layer.mapLayer == this.mapLayer) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return MAX_BOUNDING_BOX;
  }

  public BingClient getClient() {
    return this.client;
  }

  public ImagerySet getImagerySet() {
    return this.imagerySet;
  }

  public MapLayer getMapLayer() {
    return this.mapLayer;
  }

  @Override
  protected boolean initializeDo() {
    final String bingMapsKey = getProperty("bingMapsKey");
    this.client = new BingClient(bingMapsKey);
    return true;
  }

  private GeoreferencedImage newImage(final ViewRenderer view) {
    final BingClient client = this.client;
    final double metresPerPixel = view.getMetresPerPixel();
    final int zoomLevel = client.getZoomLevel(this.imagerySet, metresPerPixel);
    final BoundingBox boundingBox = view.getBoundingBox();
    return client.getMapImage(this.imagerySet, this.mapLayer, boundingBox, zoomLevel);
  }

  public void setClient(final BingClient client) {
    this.client = client;
    ExecutorServiceFactory.getExecutorService().execute(this::initialize);
  }

  public void setImagerySet(final ImagerySet imagerySet) {
    this.imagerySet = imagerySet;
    if (getName() == null) {
      setName("Bing " + CaseConverter.toCapitalizedWords(imagerySet.toString()));
    }
  }

  public void setMapLayer(final MapLayer mapLayer) {
    this.mapLayer = mapLayer;
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "imagerySet", this.imagerySet);
    addToMap(map, "mapLayer", this.mapLayer);
    return map;
  }
}
