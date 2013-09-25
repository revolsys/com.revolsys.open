package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectState;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.InvokeMethodMapObjectFactory;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.ExceptionUtil;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectFileLayer extends DataObjectListLayer {
  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataObjectFile", "File", DataObjectFileLayer.class, "create");

  public static DataObjectFileLayer create(final Map<String, Object> properties) {
    final String url = (String)properties.remove("url");
    Resource resource = null;
    if (StringUtils.hasText(url)) {
      resource = SpringUtil.getResource(url);
    }
    if (resource == null) {
      LoggerFactory.getLogger(GridLayer.class).error(
        "Layer definition does not contain a 'url' property");
    }
    final DataObjectFileLayer layer = new DataObjectFileLayer(resource);
    layer.setProperties(properties);
    return layer;
  }

  private final String url;

  private final Resource resource;

  public DataObjectFileLayer(final Resource resource) {
    this.resource = resource;
    setType("dataObjectFile");
    setExists(false);
    if (resource == null) {
      this.url = null;
      setName("Unknown");
    } else {
      this.url = SpringUtil.getUrl(resource).toString();
      setName(FileUtil.getBaseName(this.url));
      Invoke.background("Loading file: " + this.url, this, "revert");
    }
  }

  public String getUrl() {
    return this.url;
  }

  public void revert() {
    if (resource == null) {
      setExists(false);
    } else {
      if (resource.exists()) {
        final DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(this.resource);
        if (reader == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Cannot find reader for: " + this.resource);
          setExists(false);
        } else {
          try {
            final DataObjectMetaData metaData = reader.getMetaData();
            setMetaData(metaData);
            final GeometryFactory geometryFactory = metaData.getGeometryFactory();
            BoundingBox boundingBox = new BoundingBox(geometryFactory);
            final List<LayerDataObject> records = new ArrayList<LayerDataObject>();
            for (final DataObject object : reader) {
              final Geometry geometry = object.getGeometryValue();
              boundingBox = boundingBox.expandToInclude(geometry);
              final LayerDataObject record = createDataObject(metaData);
              record.setState(DataObjectState.Initalizing);
              record.setValues(object);
              record.setState(DataObjectState.Persisted);
              records.add(record);
            }
            setRecords(records);
            setBoundingBox(boundingBox);
            setExists(true);
          } catch (final Throwable e) {
            ExceptionUtil.log(getClass(), "Error reading: " + resource, e);
          } finally {
            reader.close();
          }
        }
      } else {
        LoggerFactory.getLogger(getClass()).error("Cannot find: " + this.url);
        setExists(false);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "url", this.url);
    return map;
  }
}
