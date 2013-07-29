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
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.layer.InvokeMethodMapObjectFactory;
import com.revolsys.swing.map.layer.grid.GridLayer;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectFileLayer extends DataObjectListLayer {
  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataObjectFile", "File", DataObjectFileLayer.class, "create");

  public static DataObjectFileLayer create(final Map<String, Object> properties) {
    final String url = (String)properties.remove("url");
    if (StringUtils.hasText(url)) {
      final Resource resource = SpringUtil.getResource(url);
      final DataObjectFileLayer layer = new DataObjectFileLayer(resource);
      layer.setProperties(properties);
      return layer;
    } else {
      LoggerFactory.getLogger(GridLayer.class).error(
        "Layer definition does not contain a 'url' property");
    }
    return null;
  }

  private final String url;

  private final Resource resource;

  public DataObjectFileLayer(final Resource resource) {
    this.resource = resource;
    this.url = SpringUtil.getUrl(resource).toString();
    setType("dataObjectFile");
    setName(FileUtil.getBaseName(url));
    SwingWorkerManager.execute("Loading file: " + url, this, "revert");
  }

  public String getUrl() {
    return url;
  }

  public void revert() {
    final DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(resource);
    if (reader == null) {
      throw new IllegalArgumentException("Cannot find reader for: " + resource);
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
      } finally {
        reader.close();
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "url", url);
    return map;
  }
}
