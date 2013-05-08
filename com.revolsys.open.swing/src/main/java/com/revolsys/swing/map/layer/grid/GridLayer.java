package com.revolsys.swing.map.layer.grid;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapGridFactory;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.InvokeMethodLayerFactory;
import com.revolsys.swing.map.layer.LayerFactory;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public class GridLayer extends AbstractLayer {

  static {
    MenuFactory menu = ObjectTreeModel.getMenu(GridLayer.class);
    menu.addMenuItem("zoom", new ZoomToMapSheet());
  }

  public static final LayerFactory<GridLayer> FACTORY = new InvokeMethodLayerFactory<GridLayer>(
    "grid", "Grid", GridLayer.class, "create");

  public static GridLayer create(final Map<String, Object> properties) {
    final String layerName = (String)properties.get("name");
    final String gridName = (String)properties.get("gridName");
    if (StringUtils.hasText(gridName)) {
      final RectangularMapGrid grid = RectangularMapGridFactory.getGrid(gridName);
      if (grid == null) {
        LoggerFactory.getLogger(GridLayer.class).error(
          properties + " cannot find gridName=" + gridName);
      } else {
        final GridLayer layer = new GridLayer(layerName, grid);
        layer.setProperties(properties);
        return layer;
      }
    } else {
      LoggerFactory.getLogger(GridLayer.class).error(
        properties + " does not contain a gridName property");
    }
    return null;
  }

  private final RectangularMapGrid grid;

  public GridLayer(final RectangularMapGrid grid) {
    this(grid.getName(), grid);
  }

  public GridLayer(final String name, final RectangularMapGrid grid) {
    super(name);
    if (!StringUtils.hasText(name)) {
      setName(grid.getName());
    }
    this.grid = grid;
    setReadOnly(true);
    setSelectSupported(false);
    setRenderer(new GridLayerRenderer(this));
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return grid.getGeometryFactory();
  }

  public RectangularMapGrid getGrid() {
    return grid;
  }

  public void zoomToSheet(final String sheet) {
    // final MapPanel layerViewPanel = context.getMapPanel();
    // final Viewport2D viewport = layerViewPanel.getViewport();
    // final GeometryFactory geometryFactory = viewport.getGeometryFactory();
    // final RectangularMapTile tile = grid.getTileByName(sheet);
    // final Polygon polygon = tile.getPolygon(geometryFactory, 50);
    //
    // viewport.setBoundingBox(BoundingBox.getBoundingBox(polygon));
    // layerViewPanel.fireSelectionChanged();
  }
}
