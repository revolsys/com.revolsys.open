package com.revolsys.swing.map.layer.record;

import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Property;

public class FileRecordLayer extends ListRecordLayer {

  static {
    MenuFactory.addMenuInitializer(FileRecordLayer.class,
      menu -> menu.<FileRecordLayer> addMenuItem("refresh", "Reload from File",
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
              final GeometryFactory geometryFactory = recordDefinition.getGeometryFactory();
              GeometryFactory setGeometryFactory;
              if (recordDefinition.hasGeometryField()) {
                setGeometryFactory = setGeometryFactoryPrompt(geometryFactory);
                if (setGeometryFactory != geometryFactory) {
                  recordDefinition.setGeometryFactory(setGeometryFactory);
                }
              } else {
                setGeometryFactory = GeometryFactory.DEFAULT_2D;
              }
              clearRecords();
              try (
                BaseCloseable eventsDisabled = eventsDisabled()) {
                for (final Record record : reader) {
                  final Geometry geometry = record.getGeometry();
                  if (geometry != null) {
                    if (!setGeometryFactory.isHasHorizontalCoordinateSystem()) {
                      if (geometry.isHasHorizontalCoordinateSystem()) {
                        final GeometryFactory geometryFactory2 = geometry.getGeometryFactory();
                        setGeometryFactory(geometryFactory2);
                        setGeometryFactory = geometryFactory2;
                        recordDefinition.setGeometryFactory(setGeometryFactory);
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
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    addToMap(map, "url", this.url);
    return map;
  }
}
