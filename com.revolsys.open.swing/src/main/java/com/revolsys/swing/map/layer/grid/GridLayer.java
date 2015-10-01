package com.revolsys.swing.map.layer.grid;

import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGridFactory;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.MenuSourceAction;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.PreferencesUtil;
import com.revolsys.util.Property;

public class GridLayer extends AbstractLayer {
  static {
    final MenuFactory menu = MenuFactory.getMenu(GridLayer.class);

    menu.deleteMenuItem("zoom", "Zoom to Layer");
    menu.deleteMenuItem("refresh", "Refresh");

    MenuSourceAction.<GridLayer> addMenuItem(menu, "zoom", "Zoom to Mapsheet",
      "magnifier_zoom_grid", GridLayer::zoomToSheet);
  }

  public static GridLayer create(final Map<String, Object> properties) {
    return new GridLayer(properties);
  }

  private RectangularMapGrid grid;

  public GridLayer(final Map<String, Object> properties) {
    super(properties);
    setType("gridLayer");
    setReadOnly(true);
    setSelectSupported(false);
    setRenderer(new GridLayerRenderer(this));
    setIcon(Icons.getIcon("grid"));
  }

  @Override
  protected boolean doInitialize() {
    final String gridName = getProperty("gridName");
    if (Property.hasValue(gridName)) {
      this.grid = RectangularMapGridFactory.getGrid(gridName);
      if (this.grid == null) {
        LoggerFactory.getLogger(getClass()).error("Cannot find gridName=" + gridName);
      } else {
        final GeometryFactory geometryFactory = this.grid.getGeometryFactory();
        setGeometryFactory(geometryFactory);
        return true;
      }
    } else {
      LoggerFactory.getLogger(getClass())
        .error("Layer definition does not contain a 'gridName' property");
    }
    return false;
  }

  public RectangularMapGrid getGrid() {
    return this.grid;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.remove("readOnly");
    map.remove("selectSupported");
    return map;
  }

  public void zoomToSheet() {
    final LayerGroup project = getProject();
    if (project != null) {
      final MapPanel map = MapPanel.get(this);
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
        final MapPanel map = MapPanel.get(this);
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
