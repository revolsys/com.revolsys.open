package com.revolsys.swing.map.layer.grid;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.swing.map.layer.AbstractLayer;

public class GridLayer extends AbstractLayer {

  private static final GridLayerRenderer RENDERER = new GridLayerRenderer();

  private final RectangularMapGrid grid;

  public GridLayer(final RectangularMapGrid grid) {
    this(grid.getName(), grid);
  }

  public GridLayer(final String name, final RectangularMapGrid grid) {
    super(name);
    this.grid = grid;
    setReadOnly(true);
    setSelectSupported(false);
    setRenderer(RENDERER);
  }

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
