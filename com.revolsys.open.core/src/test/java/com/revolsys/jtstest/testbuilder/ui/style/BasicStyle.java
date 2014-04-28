package com.revolsys.jtstest.testbuilder.ui.style;

import java.awt.Color;
import java.awt.Graphics2D;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.Viewport;
import com.revolsys.jtstest.testbuilder.ui.render.GeometryPainter;

public class BasicStyle implements Style
{
  private Color lineColor;
  private Color fillColor;

  public BasicStyle(Color lineColor, Color fillColor) {
    this.lineColor = lineColor;
    this.fillColor = fillColor;
  }

  public BasicStyle() {
  }

  public void paint(Geometry geom, Viewport viewport, Graphics2D g)
  {
  	GeometryPainter.paint(geom, viewport, g, lineColor, fillColor);
  }
  
  public Color getLineColor() {
    return lineColor;
  }

  public Color getFillColor() {
    return fillColor;
  }


}
