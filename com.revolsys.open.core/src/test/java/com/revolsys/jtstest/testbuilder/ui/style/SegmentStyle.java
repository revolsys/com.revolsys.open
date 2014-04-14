package com.revolsys.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jtstest.testbuilder.Viewport;

public abstract class SegmentStyle extends LineStringStyle {

  public SegmentStyle() {
    super();
    // TODO Auto-generated constructor stub
  }

  protected void paint(final int index, final Coordinates p0,
    final Coordinates p1, final int lineType, final Viewport viewport,
    final Graphics2D g) throws Exception {
    paint(index, viewport.toView(new Point2D.Double(p0.getX(), p0.getY())),
      viewport.toView(new Point2D.Double(p1.getX(), p1.getY())), lineType,
      viewport, g);
  }

  /**
   * 
   * @param p0 the origin of the line segment, in view space
   * @param p1 the termination of the line segment, in view space
   * @param viewport
   * @param graphics
   * @throws Exception
   */
  protected abstract void paint(int index, Point2D p0, Point2D p1,
    int lineType, Viewport viewport, Graphics2D graphics) throws Exception;

  @Override
  protected void paintLineString(final LineString lineString,
    final int lineType, final Viewport viewport, final Graphics2D graphics)
    throws Exception {
    for (int i = 0; i < lineString.getNumPoints() - 1; i++) {
      paint(i, lineString.getCoordinateN(i), lineString.getCoordinateN(i + 1),
        lineType, viewport, graphics);
    }
  }

}
