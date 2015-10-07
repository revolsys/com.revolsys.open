package com.revolsys.swing.map.layer.record;

import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class FileRecordLayer extends ListRecordLayer {

  static {
    final Class<AbstractRecordLayer> clazz = AbstractRecordLayer.class;
    final MenuFactory menu = MenuFactory.getMenu(clazz);
    Menus.<FileRecordLayer> addMenuItem(menu, "refresh", "Reload from File",
      Icons.getIconWithBadge("page", "refresh"), FileRecordLayer::revert);
  }

  public static FileRecordLayer create(final Map<String, Object> properties) {
    return new FileRecordLayer(properties);
  }

  private Resource resource;

  private String url;

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
    GroupLayouts.makeColumns(panel, 2, true);
    return panel;
  }

  @Override
  protected boolean doInitialize() {
    this.url = getProperty("url");
    if (Property.hasValue(this.url)) {
      this.resource = Resource.getResource(this.url);
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
        try (
          final RecordReader reader = RecordReader.newRecordReader(this.resource)) {
          if (reader == null) {
            LoggerFactory.getLogger(getClass()).error("Cannot find reader for: " + this.resource);
            return false;
          } else {
            final Map<String, Object> properties = getProperties();
            reader.setProperties(properties);
            final RecordDefinition recordDefinition = reader.getRecordDefinition();
            setRecordDefinition(recordDefinition);
            GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
            clearRecords();
            for (final Record record : reader) {
              final Geometry geometry = record.getGeometry();
              if (geometry != null) {
                if (geometryFactory == null || !geometryFactory.isHasCoordinateSystem()) {
                  final GeometryFactory geometryFactory2 = geometry.getGeometryFactory();
                  if (geometryFactory2.isHasCoordinateSystem()) {
                    setGeometryFactory(geometryFactory2);
                    geometryFactory = geometryFactory2;
                    recordDefinition.setGeometryFactory(geometryFactory2);
                  }
                }
              }

              createRecordInternal(record);
            }
            refreshBoundingBox();
            return true;
          }
        } catch (final Throwable e) {
          ExceptionUtil.log(getClass(), "Error reading: " + this.resource, e);
        } finally {
          fireRecordsChanged();
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
