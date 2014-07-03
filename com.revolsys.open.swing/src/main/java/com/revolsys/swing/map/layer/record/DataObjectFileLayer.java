package com.revolsys.swing.map.layer.record;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.io.DataObjectReaderFactory;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.map.InvokeMethodMapObjectFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.util.ExceptionUtil;

public class DataObjectFileLayer extends DataObjectListLayer {
  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "dataObjectFile", "File", DataObjectFileLayer.class, "create");

  public static DataObjectFileLayer create(final Map<String, Object> properties) {
    return new DataObjectFileLayer(properties);
  }

  private String url;

  private Resource resource;

  public DataObjectFileLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("dataObjectFile");
  }

  @Override
  protected ValueField addPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.addPropertiesTabGeneralPanelSource(parent);

    final String url = getUrl();
    if (url.startsWith("file:")) {
      final String fileName = url.replaceFirst("file:(//)?", "");
      SwingUtil.addReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addReadOnlyTextField(panel, "URL", url);
    }
    final String fileNameExtension = FileUtil.getFileNameExtension(url);
    if (StringUtils.hasText(fileNameExtension)) {
      SwingUtil.addReadOnlyTextField(panel, "File Extension", fileNameExtension);
      final DataObjectReaderFactory factory = IoFactoryRegistry.getInstance()
        .getFactoryByFileExtension(DataObjectReaderFactory.class,
          fileNameExtension);
      if (factory != null) {
        SwingUtil.addReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    GroupLayoutUtil.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  protected boolean doInitialize() {
    url = getProperty("url");
    if (StringUtils.hasText(url)) {
      resource = SpringUtil.getResource(url);
      return revert();
    } else {
      LoggerFactory.getLogger(getClass()).error(
        "Layer definition does not contain a 'url' property: " + getName());
      return false;
    }

  }

  public String getUrl() {
    return this.url;
  }

  public boolean revert() {
    if (resource == null) {
      return false;
    } else {
      if (resource.exists()) {
        final DataObjectReader reader = AbstractDataObjectReaderFactory.dataObjectReader(this.resource);
        if (reader == null) {
          LoggerFactory.getLogger(getClass()).error(
            "Cannot find reader for: " + this.resource);
          return false;
        } else {
          try {
            reader.setProperties(getProperties());
            final RecordDefinition metaData = reader.getMetaData();
            setMetaData(metaData);
            final GeometryFactory geometryFactory = metaData.getGeometryFactory();
            BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory);
            for (final Record record : reader) {
              final Geometry geometry = record.getGeometryValue();
              boundingBox = boundingBox.expandToInclude(geometry);

              createRecordInternal(record);
            }
            setBoundingBox(boundingBox);
            return true;
          } catch (final Throwable e) {
            ExceptionUtil.log(getClass(), "Error reading: " + resource, e);
          } finally {
            fireRecordsChanged();
            reader.close();
          }
        }
      } else {
        LoggerFactory.getLogger(getClass()).error("Cannot find: " + this.url);
      }
    }
    return false;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    MapSerializerUtil.add(map, "url", this.url);
    return map;
  }
}
