package com.revolsys.swing.map.layer.mapguide;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.jeometry.common.io.PathName;
import org.jeometry.common.logging.Logs;

import com.revolsys.beans.Classes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.mapguide.FeatureLayer;
import com.revolsys.record.query.Query;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.PasswordUtil;
import com.revolsys.util.Property;

public class MapGuideWebServerRecordLayer extends AbstractRecordLayer {
  private static final String J_TYPE = "mapGuideWebServerRecordLayer";

  private FeatureLayer webServiceLayer;

  private String url;

  private PathName layerPath;

  private String password;

  private String username;

  public MapGuideWebServerRecordLayer() {
    super(J_TYPE);
    setReadOnly(true);
  }

  public MapGuideWebServerRecordLayer(final FeatureLayer layerDescription) {
    this();
    setWebServiceLayer(layerDescription);
    setProperties(Collections.emptyMap());
  }

  public MapGuideWebServerRecordLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  protected void forEachRecordInternal(final Query query,
    final Consumer<? super LayerRecord> consumer) {
    try (
      RecordReader reader = this.webServiceLayer.newRecordReader(this::newLayerRecord, query)) {
      for (final Record record : reader) {
        consumer.accept((LayerRecord)record);
      }
    }
  }

  public PathName getLayerPath() {
    return this.layerPath;
  }

  @Override
  public int getRecordCount(final Query query) {
    if (this.webServiceLayer == null) {
      return 0;
    } else {
      return this.webServiceLayer.getRecordCount(query);
    }
  }

  @Override
  public List<LayerRecord> getRecords(BoundingBox boundingBox) {
    if (hasGeometryField()) {
      boundingBox = convertBoundingBox(boundingBox);
      if (Property.hasValue(boundingBox)) {
        final List<LayerRecord> records = this.webServiceLayer.getRecords(this::newLayerRecord,
          boundingBox);
        return records;
      }
    }
    return Collections.emptyList();
  }

  public String getUrl() {
    return this.url;
  }

  public FeatureLayer getWebServiceLayer() {
    return this.webServiceLayer;
  }

  @Override
  protected boolean initializeDo() {
    FeatureLayer webServiceLayer = getWebServiceLayer();
    if (webServiceLayer == null) {
      final String url = getUrl();
      final PathName layerPath = getLayerPath();

      if (url == null) {
        Logs.error(this, Classes.className(this) + " requires a url: " + getPath());
        return false;
      }
      if (layerPath == null) {
        Logs.error(this, Classes.className(this) + " requires a layerPath: " + getPath());
        return false;
      }
      ArcGisRestCatalog server;
      try {
        server = ArcGisRestCatalog.newArcGisRestCatalog(url);
      } catch (final Throwable e) {
        Logs.error(this, "Unable to connect to server: " + url + " for " + getPath(), e);
        return false;
      }
      try {
        webServiceLayer = server.getWebServiceResource(layerPath, FeatureLayer.class);
      } catch (final IllegalArgumentException e) {
        Logs.error(this, "Layer is not valid: " + getPath(), e);
        return false;
      }
      if (webServiceLayer == null) {
        Logs.error(this, "Layer does not exist: " + layerPath + " for " + getPath());
        return false;
      } else {
        setWebServiceLayer(webServiceLayer);
      }
    }

    if (webServiceLayer != null) {
      final RecordDefinition recordDefinition = webServiceLayer.getRecordDefinition();
      if (recordDefinition != null) {
        setRecordDefinition(recordDefinition);
        setBoundingBox(webServiceLayer.getBoundingBox());
        // initRenderer();
        return super.initializeDo();
      }
    }
    return false;
  }

  public void setLayerPath(final PathName layerPath) {
    this.layerPath = layerPath;
  }

  public void setPassword(final String password) {
    this.password = PasswordUtil.decrypt(password);
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public void setUrl(final UrlResource url) {
    if (url != null) {
      setUrl(url.getUriString());
      this.username = url.getUsername();
      this.password = url.getPassword();
    }
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setWebServiceLayer(final FeatureLayer layerDescription) {
    this.webServiceLayer = layerDescription;
    if (this.webServiceLayer != null) {
      final String name = this.webServiceLayer.getName();
      if (getName() == null) {
        setName(name);
      }

      final UrlResource serviceUrl = this.webServiceLayer.getWebService().getServiceUrl();
      setUrl(serviceUrl);

      final PathName pathName = this.webServiceLayer.getPathName();
      setLayerPath(pathName);

    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "url", this.url);
    addToMap(map, "username", this.username);
    addToMap(map, "password", PasswordUtil.encrypt(this.password));
    addToMap(map, "layerPath", this.layerPath);
    return map;
  }
}
