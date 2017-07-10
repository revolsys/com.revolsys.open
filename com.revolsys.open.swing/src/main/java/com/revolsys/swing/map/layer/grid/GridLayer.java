package com.revolsys.swing.map.layer.grid;

import java.util.Map;

import javax.swing.JOptionPane;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGridFactory;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.logging.Logs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.Menus;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;

public class GridLayer extends AbstractLayer {
  static {
    MenuFactory.addMenuInitializer(GridLayer.class, (menu) -> {
      menu.deleteMenuItem("zoom", "Zoom to Layer");
      menu.deleteMenuItem("refresh", "Refresh");
      Menus.<GridLayer> addMenuItem(menu, "zoom", "Zoom to Mapsheet", "magnifier_zoom_grid",
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
    setRenderer(new GridLayerRenderer(this));
    setIcon(Icons.getIcon("grid"));
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

  public void setGrid(final RectangularMapGrid grid) {
    this.grid = grid;
  }

  public void setGridName(final String gridName) {
    this.gridName = gridName;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
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
      final MapPanel map = getMapPanel();
      final RectangularMapGrid grid = getGrid();
      final String gridName = grid.getName();
      final String preferenceName = CaseConverter.toCapitalizedWords(gridName) + "Mapsheet";
      String mapsheet = PreferencesUtil.getString(getClass(), preferenceName);
      mapsheet = JOptionPane.showInputDialog(map,
        "Enter name of the" + gridName + " mapsheet to zoom to", mapsheet);
      zoomToSheet(mapsheet);
    }
  }

  public void zoomToSheet(final String mapsheet) {
    final Project project = getProject();
    if (project != null) {
      if (Property.hasValue(mapsheet)) {
        final MapPanel map = getMapPanel();
        final RectangularMapGrid grid = getGrid();
        final String gridName = grid.getName();
        try {
          final RectangularMapTile mapTile = grid.getTileByName(mapsheet);
          final BoundingBox boundingBox = mapTile.getBoundingBox();
          project.setViewBoundingBox(boundingBox);
        } catch (final Throwable e) {
          final String message = "Invalid mapsheet " + mapsheet + " for " + gridName;
          JOptionPane.showMessageDialog(map, message);
        } finally {
          final String preferenceName = CaseConverter.toCapitalizedWords(gridName) + "Mapsheet";
          PreferencesUtil.setString(getClass(), preferenceName, mapsheet);
        }
      }
    }
  }
}
