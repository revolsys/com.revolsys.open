package com.revolsys.swing.map.layer.record;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.revolsys.data.record.Record;
import com.revolsys.data.record.io.RecordIo;
import com.revolsys.data.record.io.RecordReader;
import com.revolsys.data.record.io.RecordReaderFactory;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
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
import com.revolsys.util.Property;

public class FileRecordLayer extends ListRecordLayer {

  public static FileRecordLayer create(final Map<String, Object> properties) {
    return new FileRecordLayer(properties);
  }

  private String url;

  private Resource resource;

  public FileRecordLayer(final Map<String, ? extends Object> properties) {
    super(properties);
    setType("recordFileLayer");
  }

  @Override
  protected ValueField createPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.createPropertiesTabGeneralPanelSource(parent);

    final String url = getUrl();
    if (url.startsWith("file:")) {
      final String fileName = url.replaceFirst("file:(//)?", "");
      SwingUtil.addLabelledReadOnlyTextField(panel, "File", fileName);
    } else {
      SwingUtil.addLabelledReadOnlyTextField(panel, "URL", url);
    }
    final String fileNameExtension = FileUtil.getFileNameExtension(url);
    if (Property.hasValue(fileNameExtension)) {
      SwingUtil.addLabelledReadOnlyTextField(panel, "File Extension", fileNameExtension);
      final RecordReaderFactory factory = IoFactory.factory(RecordReaderFactory.class,
        fileNameExtension);
      if (factory != null) {
        SwingUtil.addLabelledReadOnlyTextField(panel, "File Type", factory.getName());
      }
    }
    GroupLayoutUtil.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  protected boolean doInitialize() {
    this.url = getProperty("url");
    if (Property.hasValue(this.url)) {
      this.resource = SpringUtil.getResource(this.url);
      return revert();
    } else {
      LoggerFactory.getLogger(getClass())
        .error("Layer definition does not contain a 'url' property: " + getName());
      return false;
    }

  }

  public String getUrl() {
    return this.url;
  }

  public boolean revert() {
    if (this.resource == null) {
      return false;
    } else {
      if (this.resource.exists()) {
        final RecordReader reader = RecordIo.recordReader(this.resource);
        if (reader == null) {
          LoggerFactory.getLogger(getClass()).error("Cannot find reader for: " + this.resource);
          return false;
        } else {
          try {
            final Map<String, Object> properties = getProperties();
            reader.setProperties(properties);
            final RecordDefinition recordDefinition = reader.getRecordDefinition();
            setRecordDefinition(recordDefinition);
            final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
            BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory);
            for (final Record record : reader) {
              final Geometry geometry = record.getGeometryValue();
              boundingBox = boundingBox.expandToInclude(geometry);

              createRecordInternal(record);
            }
            setBoundingBox(boundingBox);
            return true;
          } catch (final Throwable e) {
            ExceptionUtil.log(getClass(), "Error reading: " + this.resource, e);
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
