package com.revolsys.swing.map.layer.grid;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.grid.RectangularMapGrid;
import com.revolsys.gis.grid.RectangularMapTile;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;

public class GridLayerRenderer extends AbstractLayerRenderer<GridLayer> {

  public GridLayerRenderer(final GridLayer layer) {
    super("grid", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final GridLayer layer) {
    try {
      final double scale = viewport.getScale();
      if (layer.isVisible(scale)) {
        final BoundingBox boundingBox = viewport.getBoundingBox();
        final RectangularMapGrid grid = layer.getGrid();
        final List<RectangularMapTile> tiles = grid.getTiles(boundingBox);
        final Graphics2D graphics = viewport.getGraphics();
        final Font font = graphics.getFont();
        for (final RectangularMapTile tile : tiles) {
          final BoundingBox tileBoundingBox = tile.getBoundingBox();
          final BoundingBox intersectBoundingBox = boundingBox.intersection(tileBoundingBox);
          if (!intersectBoundingBox.isEmpty()) {
            final String tileName = tile.getName().toUpperCase();

            final Polygon polygon = tile.getPolygon(
              viewport.getGeometryFactory(), 50);
            GeometryStyleRenderer.renderOutline(viewport, graphics, polygon,
              GeometryStyle.line(Color.LIGHT_GRAY));

            final Point centroid = polygon.getCentroid();
            final double centreX = centroid.getX();
            final double centreY = centroid.getY();

            final Font newFont = new Font(font.getName(), font.getStyle(), 12);
            graphics.setFont(newFont);

            final FontMetrics metrics = graphics.getFontMetrics();
            final double[] coord = new double[2];
            viewport.getModelToScreenTransform().transform(new double[] {
              centreX, centreY
            }, 0, coord, 0, 1);
            int x = (int)(coord[0] - metrics.stringWidth(tileName) / 2);
            int y = (int)(coord[1] + metrics.getHeight() / 2);
            final Rectangle2D bounds = metrics.getStringBounds(tileName,
              graphics);
            final double width = bounds.getWidth();
            final double height = bounds.getHeight();

            if (x < 0) {
              x = 1;
            }
            final int viewWidth = viewport.getViewWidthPixels();
            if (x + width > viewWidth) {
              x = (int)(viewWidth - width - 1);
            }
            if (y < height) {
              y = (int)height + 1;
            }
            final int viewHeight = viewport.getViewHeightPixels();
            if (y > viewHeight) {
              y = viewHeight - 1;
            }

            graphics.setColor(WebColors.LightGray);
            graphics.fill(new Rectangle2D.Double(x - 2, y + bounds.getY() - 1,
              width + 4, height + 2));

            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
              RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            graphics.setColor(Color.BLACK);
            graphics.drawString(tileName, x, y);
          }
          graphics.setFont(font);
        }
      }
    } catch (final IllegalArgumentException e) {

    }
  }

  @Override
  public Map<String, Object> toMap() {
    return Collections.emptyMap();
  }
}
