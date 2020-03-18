package com.revolsys.swing.map.layer.ogc.wms;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;
import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.wms.WmsClient;
import com.revolsys.gis.wms.capabilities.WmsLayerDefinition;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.layer.raster.ViewFunctionImageLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Property;
import com.revolsys.webservice.WebService;
import com.revolsys.webservice.WebServiceConnectionManager;

public class OgcWmsImageLayer extends AbstractLayer implements BaseMapLayer {
  private String connectionName;

  private String serviceUrl;

  private boolean hasError = false;

  private String layerName;

  private WmsLayerDefinition wmsLayerDefinition;

  public OgcWmsImageLayer() {
    super("ogcWmsImageLayer");
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new ViewFunctionImageLayerRenderer<>(this, this::newImage));
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

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OgcWmsImageLayer) {
      final OgcWmsImageLayer layer = (OgcWmsImageLayer)other;
      if (DataType.equal(layer.getServiceUrl(), getServiceUrl())) {
        if (DataType.equal(layer.getLayerName(), getLayerName())) {
          if (DataType.equal(layer.getConnectionName(), getConnectionName())) {
            if (DataType.equal(layer.getName(), getName())) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public String getConnectionName() {
    return this.connectionName;
  }

  public String getLayerName() {
    return this.layerName;
  }

  public String getServiceUrl() {
    return this.serviceUrl;
  }

  public WmsLayerDefinition getWmsLayerDefinition() {
    return this.wmsLayerDefinition;
  }

  @Override
  protected boolean initializeDo() {
    final boolean initialized = super.initializeDo();
    if (initialized) {
      try {
        final WmsClient wmsClient;
        if (Property.hasValue(this.connectionName)) {
          final WebService<?> webService = WebServiceConnectionManager
            .getWebService(this.connectionName);
          if (webService == null) {
            Logs.error(this,
              getPath() + ": Web service " + this.connectionName + ": no connection configured");
            return false;
          } else if (webService instanceof WmsClient) {
            wmsClient = (WmsClient)webService;
          } else {
            Logs.error(this,
              getPath() + ": Web service " + this.connectionName + ": is not a OGS WMS service");
            return false;
          }
        } else if (Property.hasValue(this.serviceUrl)) {
          wmsClient = new WmsClient(this.serviceUrl);
        } else {
          Logs.error(this, getPath()
            + ": A record store layer requires a connection entry with a name or url, username, and password ");
          return false;
        }
        final WmsLayerDefinition wmsLayerDefinition = wmsClient.getLayer(this.layerName);
        setWmsLayerDefinition(wmsLayerDefinition);
        return wmsLayerDefinition != null;
      } catch (final WrappedException e) {
        final Throwable cause = Exceptions.unwrap(e);
        if (cause instanceof UnknownHostException) {
          return setNotExists("Unknown host: " + cause.getMessage());
        } else {
          throw e;
        }
      }
    }
    return initialized;
  }

  public boolean isHasError() {
    return this.hasError;
  }

  private GeoreferencedImage newImage(final ViewRenderer view) {
    try {
      final WmsLayerDefinition wmsLayerDefinition = this.wmsLayerDefinition;
      if (wmsLayerDefinition != null) {
        if (!view.isCancelled()) {
          final BoundingBox viewportBoundingBox = view.getBoundingBox();
          final BoundingBox queryBoundingBox = viewportBoundingBox
            .bboxIntersection(wmsLayerDefinition.getLatLonBoundingBox());
          final int viewWidthPixels = (int)Math.ceil(view.getViewWidthPixels());
          final int viewHeightPixels = (int)Math.ceil(view.getViewHeightPixels());
          return wmsLayerDefinition.getMapImage(queryBoundingBox, viewWidthPixels,
            viewHeightPixels);
        }
      }
    } catch (final Throwable t) {
      if (!view.isCancelled()) {
        Logs.error(this, "Unable to get image", t);
      }
    }
    return null;
  }

  @Override
  protected void refreshDo() {
    this.hasError = false;
    super.refreshDo();
  }

  public void setConnectionName(final String connectionName) {
    this.connectionName = connectionName;
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
      final String connectionName = wmsClient.getName();
      if (Property.hasValue(connectionName)) {
        this.connectionName = connectionName;
        this.serviceUrl = null;
      } else {
        this.serviceUrl = wmsClient.getServiceUrl().toString();
      }
      final String layerTitle = wmsLayerDefinition.getTitle();
      if (!Property.hasValue(getName())) {
        setName(layerTitle);
      }
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.keySet()
      .removeAll(Arrays.asList("readOnly", "querySupported", "selectSupported", "minimumScale",
        "maximumScale"));
    if (Property.hasValue(this.connectionName)) {
      addToMap(map, "connectionName", this.connectionName);
    } else {
      addToMap(map, "serviceUrl", this.serviceUrl);
    }
    addToMap(map, "layerName", this.layerName);
    return map;
  }
}
