package com.revolsys.swing.map.layer.grid;

import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGridFactory;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.Dialogs;
import com.revolsys.swing.component.TabbedValuePanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.style.panel.LayerStylePanel;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;

public class GridLayer extends AbstractLayer {
  static {
    MenuFactory.addMenuInitializer(GridLayer.class, menu -> {
      menu.deleteMenuItem("zoom", "Zoom to Layer");
      menu.deleteMenuItem("refresh", "Refresh");
      menu.<GridLayer> addMenuItem("zoom", "Zoom to Mapsheet", "magnifier_zoom_grid",
        GridLayer::zoomToSheet, false);
    });
  }

  public static GridLayer newLayer(final Map<String, ? extends Object> config) {
    return new GridLayer(config);
  }

  private String gridName;

  private RectangularMapGrid grid;

  public GridLayer(final Map<String, ? extends Object> config) {
    super("gridLayer");
    setProperties(config);
    setReadOnly(true);
    setSelectSupported(false);
    if (getRenderer() == null) {
      setRenderer(new GridLayerRenderer(this));
    }
    setIcon("grid");
  }

  public RectangularMapGrid getGrid() {
    return this.grid;
  }

  public String getGridName() {
    return this.gridName;
  }

  @Override
  protected boolean initializeDo() {
    final String gridName = getGridName();
    if (Property.hasValue(gridName)) {
      this.grid = RectangularMapGridFactory.getGrid(gridName);
      if (this.grid == null) {
        Logs.error(this, "Cannot find gridName=" + gridName);
      }
    }
    if (this.grid == null) {
      Logs.error(this, "Layer definition does not contain a 'grid' or 'gridName' property");
      return false;
    } else {
      final GeometryFactory geometryFactory = this.grid.getGeometryFactory();
      setGeometryFactory(geometryFactory);
      return true;
    }
  }

  @Override
  public TabbedValuePanel newPropertiesPanel() {
    final TabbedValuePanel propertiesPanel = super.newPropertiesPanel();
    newPropertiesPanelStyle(propertiesPanel);
    return propertiesPanel;
  }

  protected void newPropertiesPanelStyle(final TabbedValuePanel propertiesPanel) {
    if (getRenderer() != null) {
      final LayerStylePanel stylePanel = new LayerStylePanel(this);
      propertiesPanel.addTab("Style", "palette", stylePanel);
    }
  }

  public void setGrid(final RectangularMapGrid grid) {
    this.grid = grid;
  }

  public void setGridName(final String gridName) {
    this.gridName = gridName;
  }

  @SuppressWarnings("unchecked")
  public void setStyle(Object style) {
    if (style instanceof Map) {
      final Map<String, Object> map = (Map<String, Object>)style;
      style = MapObjectFactory.toObject(map);
    }
    if (style instanceof GridLayerRenderer) {
      final GridLayerRenderer renderer = (GridLayerRenderer)style;
      setRenderer(renderer);
    } else {
      Logs.error(this, "Cannot create renderer for: " + style);
    }
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    final String gridName = getGridName();
    if (Property.hasValue(gridName)) {
      addToMap(map, "gridName", gridName);
    } else {
      addToMap(map, "grid", this.grid);
    }
    map.remove("readOnly");
    map.remove("selectSupported");
    return map;
  }

  public void zoomToSheet() {
    final LayerGroup project = getProject();
    if (project != null) {
      final RectangularMapGrid grid = getGrid();
      final String gridName = grid.getName();
      final String preferenceName = CaseConverter.toCapitalizedWords(gridName) + "Mapsheet";
      String mapsheet = PreferencesUtil.getString(getClass(), preferenceName);

      final String title = "Zoom to Mapsheet: " + getName();
      mapsheet = (String)Dialogs.showInputDialog(
        new JLabel("<html><p>" + gridName + "</p><p>Enter mapsheet to zoom to.</p></html>"), title,
        JOptionPane.QUESTION_MESSAGE, null, null, mapsheet);
      zoomToSheet(mapsheet);
    }
  }

  public void zoomToSheet(final String mapsheet) {
    final Project project = getProject();
    if (project != null) {
      if (Property.hasValue(mapsheet)) {
        final RectangularMapGrid grid = getGrid();
        final String gridName = grid.getName();
        try {
          final RectangularMapTile mapTile = grid.getTileByName(mapsheet);
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          project.setViewBoundingBox(boundingBox);
        } catch (final Throwable e) {
          final String message = "Invalid mapsheet " + mapsheet + " for " + gridName;
          Dialogs.showMessageDialog(message);
        } finally {
          final String preferenceName = CaseConverter.toCapitalizedWords(gridName) + "Mapsheet";
          PreferencesUtil.setString(getClass(), preferenceName, mapsheet);
        }
      }
    }
  }
}
