package com.revolsys.swing.map.layer.record;

import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
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
import com.revolsys.util.Property;

public class FileRecordLayer extends ListRecordLayer {

  static {
    MenuFactory.addMenuInitializer(FileRecordLayer.class,
      menu -> Menus.<FileRecordLayer> addMenuItem(menu, "refresh", "Reload from File",
        Icons.getIconWithBadge("page", "refresh"), FileRecordLayer::revertDo, true));
  }

  public static FileRecordLayer newLayer(final Map<String, ? extends Object> config) {
    return new FileRecordLayer(config);
  }

  private Resource resource;

  private String url;

  public FileRecordLayer(final Map<String, ? extends Object> properties) {
    super("recordFileLayer");
    setProperties(properties);
  }

  public String getUrl() {
    return this.url;
  }

  @Override
  protected boolean initializeDo() {
    this.url = getProperty("url");
    if (Property.hasValue(this.url)) {
      this.resource = Resource.getResource(this.url);
      return revertDo();
    } else {
      Logs.error(this, "Layer definition does not contain a 'url' property: " + getName());
      return false;
    }

  }

  @Override
  protected ValueField newPropertiesTabGeneralPanelSource(final BasePanel parent) {
    final ValueField panel = super.newPropertiesTabGeneralPanelSource(parent);

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

  protected boolean revertDo() {
    if (this.resource == null) {
      return false;
    } else {
      if (this.resource.exists()) {
        try (
          final RecordReader reader = RecordReader.newRecordReader(this.resource)) {
          if (reader == null) {
            Logs.error(this, "Cannot find reader for: " + this.resource);
            return false;
          } else {
            final Map<String, Object> properties = getProperties();
            reader.setProperties(properties);
            final RecordDefinition recordDefinition = reader.getRecordDefinition();
            setRecordDefinition(recordDefinition);
            if (recordDefinition == null) {
              Logs.error(this, "No record definition found for: " + this.url);
              return false;
            } else {
              GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
              clearRecords();
              try (
                BaseCloseable eventsDisabled = eventsDisabled()) {
                for (final Record record : reader) {
                  final Geometry geometry = record.getGeometry();
                  if (geometry != null) {
                    if (geometryFactory == null
                      || !geometryFactory.isHasHorizontalCoordinateSystem()) {
                      final GeometryFactory geometryFactory2 = geometry.getGeometryFactory();
                      if (geometryFactory2.isHasHorizontalCoordinateSystem()) {
                        setGeometryFactory(geometryFactory2);
                        geometryFactory = geometryFactory2;
                        recordDefinition.setGeometryFactory(geometryFactory2);
                      }
                    }
                  }
                  if (isDeleted()) {
                    return false;
                  } else {
                    addNewRecordPersisted(record);
                  }
                }
              }
            }
            refreshBoundingBox();
            initializeMenus();
            setExists(true);
            return true;
          }
        } catch (final RuntimeException e) {
          Logs.error(this, "Error reading: " + this.resource, e);
        } finally {
          refresh();
        }
      } else {
        Logs.error(this, "Cannot find: " + this.url);
      }
    }
    return false;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "url", this.url);
    return map;
  }
}
