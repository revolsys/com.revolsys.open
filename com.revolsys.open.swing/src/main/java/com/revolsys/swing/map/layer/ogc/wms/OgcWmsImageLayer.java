package com.revolsys.swing.map.layer.ogc.wms;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.wms.WmsClient;
import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.menu.MenuFactory;

public class OgcWmsImageLayer extends AbstractLayer implements BaseMapLayer {
  public static void mapObjectFactoryInit() {
    MapObjectFactoryRegistry.newFactory("ogcWmsImageLayer", "OGC WMS Image Layer",
      OgcWmsImageLayer::new);

    final MenuFactory wmsLayerMenu = MenuFactory.getMenu(WmsLayerDefinition.class);

    final Function<WmsLayerDefinition, BaseMapLayer> baseMapLayerFactory = OgcWmsImageLayer::new;
    BaseMapLayer.addNewLayerMenu(wmsLayerMenu, baseMapLayerFactory);

  }

  private boolean hasError = false;

  private String serviceUrl;

  private String layerName;

  private WmsLayerDefinition wmsLayerDefinition;

  public OgcWmsImageLayer() {
    super("ogcWmsImageLayer");
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new OgcWmsImageLayerRenderer(this));
  }

  public OgcWmsImageLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  public OgcWmsImageLayer(final WmsLayerDefinition wmsLayerDefinition) {
    this();
    if (wmsLayerDefinition == null) {
      setExists(false);
    } else {
      setInitialized(true);
      setExists(true);
      setWmsLayerDefinition(wmsLayerDefinition);
    }
  }

  public WmsLayerDefinition getWmsLayerDefinition() {
    return this.wmsLayerDefinition;
  }

  @Override
  protected boolean initializeDo() {
    final boolean initialized = super.initializeDo();
    if (initialized) {
      final WmsClient wmsClient = new WmsClient(this.serviceUrl);
      final WmsLayerDefinition wmsLayerDefinition = wmsClient.getLayer(this.layerName);
      setWmsLayerDefinition(wmsLayerDefinition);
      return wmsLayerDefinition != null;
    }
    return initialized;
  }

  public boolean isHasError() {
    return this.hasError;
  }

  @Override
  protected void refreshDo() {
    this.hasError = false;
    super.refreshDo();
  }

  public void setError(final Throwable e) {
    if (!this.hasError) {
      this.hasError = true;
      Logs.error(this, "Unable to get map tiles", e);
    }
  }

  public void setLayerName(final String layerName) {
    this.layerName = layerName;
  }

  public void setServiceUrl(final String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  protected void setWmsLayerDefinition(final WmsLayerDefinition wmsLayerDefinition) {
    this.wmsLayerDefinition = wmsLayerDefinition;
    if (wmsLayerDefinition == null) {
      setExists(false);
    } else {
      setExists(true);
      final WmsClient wmsClient = wmsLayerDefinition.getWmsClient();
      this.serviceUrl = wmsClient.getServiceUrl().toString();
      final String layerTitle = wmsLayerDefinition.getTitle();
      setName(layerTitle);
      this.layerName = wmsLayerDefinition.getName();
      final long minimumScale = (long)wmsLayerDefinition.getMinimumScale();
      super.setMinimumScale(minimumScale);
      final long maximumScale = (long)wmsLayerDefinition.getMaximumScale();
      super.setMaximumScale(maximumScale);
      setBoundingBox(wmsLayerDefinition.getLatLonBoundingBox());
      final GeometryFactory geometryFactory = wmsLayerDefinition.getDefaultGeometryFactory();
      setGeometryFactory(geometryFactory);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.keySet().removeAll(Arrays.asList("readOnly", "querySupported", "selectSupported",
      "minimumScale", "maximumScale"));
    addToMap(map, "serviceUrl", this.serviceUrl);
    addToMap(map, "layerName", this.layerName);
    return map;
  }
}
