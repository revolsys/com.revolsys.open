package com.revolsys.jtstest.testbuilder.ui.style;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import com.revolsys.jtstest.testbuilder.Viewport;

public class CircleEndpointStyle 
  extends LineStringEndpointStyle 
{

  private final static double DIAMETER = 10;
  private boolean filled = true;

  // default in case colour is not set
  private Color color = Color.RED;

  public CircleEndpointStyle(Color color, boolean start, boolean filled) {
      super(start);
      this.color = color;
      this.filled = filled;
  }

  protected void paint(Point2D terminal, Point2D next, Viewport viewport,
      Graphics2D g) 
  {

      g.setPaint(color);

      Shape circle = toCircle(terminal);

      if (filled) {
          g.fill(circle);
      }
      else {
        g.draw(circle);
      }
  }

  private Shape toCircle(Point2D viewPoint) {
    return new Ellipse2D.Double(viewPoint.getX() - (DIAMETER / 2d),
        viewPoint.getY() - (DIAMETER / 2d), DIAMETER, DIAMETER);
}

}
