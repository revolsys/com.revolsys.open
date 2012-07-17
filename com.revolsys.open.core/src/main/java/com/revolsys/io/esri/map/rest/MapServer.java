package com.revolsys.io.esri.map.rest;

import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.io.esri.map.rest.map.LayerDescription;
import com.revolsys.io.esri.map.rest.map.TableDescription;
import com.revolsys.io.esri.map.rest.map.TileInfo;
import com.revolsys.io.esri.map.rest.map.TimeInfo;

public class MapServer extends Service {

  protected MapServer() {
    super("MapServer");
  }

  public String getCapabilities() {
    return getValue("capabilities");
  }

  public String getCopyrightText() {
    return getValue("copyrightText");
  }

  public String getDescription() {
    return getValue("description");
  }

  public Map<String, String> getDocumentInfo() {
    return getValue("documentInfo");
  }

  public BoundingBox getFullExtent() {
    return getBoundingBox("fullExtent");
  }

  public BoundingBox getInitialExtent() {
    return getBoundingBox("initialExtent");
  }

  public List<LayerDescription> getLayers() {
    return getList(LayerDescription.class, "layers");
  }

  public String getMapName() {
    return getValue("mapName");
  }

  public Boolean getSingleFusedMapCache() {
    return getValue("singleFusedMapCache");
  }

  public String getSupportedImageFormatTypes() {
    return getValue("supportedImageFormatTypes");
  }

  public List<TableDescription> getTables() {
    return getList(TableDescription.class, "tables");
  }

  public TileInfo getTileInfo() {
    return getObject(TileInfo.class, "tileInfo");
  }

  public TimeInfo getTimeInfo() {
    return getObject(TimeInfo.class, "timeInfo");
  }

  public String getUnits() {
    return getValue("units");
  }
}
