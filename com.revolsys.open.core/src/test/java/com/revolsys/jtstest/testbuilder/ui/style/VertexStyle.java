package com.revolsys.jtstest.testbuilder.ui.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jtstest.testbuilder.AppConstants;
import com.revolsys.jtstest.testbuilder.Viewport;

public class VertexStyle implements Style {
  private final double sizeOver2 = AppConstants.VERTEX_SIZE / 2d;

  protected Rectangle shape;

  private final Color color;

  // reuse point objects to avoid creation overhead
  private final Point2D pM = new Point2D.Double();

  private final Point2D pV = new Point2D.Double();

  public VertexStyle(final Color color) {
    this.color = color;
    // create basic rectangle shape
    shape = new Rectangle(0, 0, AppConstants.VERTEX_SIZE,
      AppConstants.VERTEX_SIZE);
  }

  @Override
  public void paint(final Geometry geom, final Viewport viewport,
    final Graphics2D g) {
    g.setPaint(color);
    for (final Vertex vertex : geom.vertices()) {
      if (!viewport.containsInModel(vertex)) {
        // Otherwise get "sun.dc.pr.PRException: endPath: bad path" exception
        continue;
      }
      pM.setLocation(vertex.getX(), vertex.getY());
      viewport.toView(pM, pV);
      shape.setLocation((int)(pV.getX() - sizeOver2),
        (int)(pV.getY() - sizeOver2));
      g.fill(shape);
    }
  }

}
