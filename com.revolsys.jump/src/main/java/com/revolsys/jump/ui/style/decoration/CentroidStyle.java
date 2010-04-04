package com.revolsys.jump.ui.style.decoration;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import javax.swing.Icon;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.ChoosableStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class CentroidStyle implements Style, ChoosableStyle {
  private Shape shape;

  private int size = 4;

  private Color fillColor;

  private boolean enabled = true;

  private Color strokeColor;

  public CentroidStyle() {
    this(new Rectangle2D.Double());
  }

  public CentroidStyle(final Shape shape) {
    this.shape = shape;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setSize(final int size) {
    this.size = size;
  }

  public int getSize() {
    return size;
  }

  public void initialize(final Layer layer) {
    // Set the vertices' fill color to the layer's line color
    fillColor = GUIUtil.alphaColor(layer.getBasicStyle().getFillColor(),
      layer.getBasicStyle().getAlpha());
    strokeColor = GUIUtil.alphaColor(layer.getBasicStyle().getLineColor(),
      layer.getBasicStyle().getAlpha());

  }

  public void paint(final Feature f, final Graphics2D g, final Viewport viewport)
    throws Exception {
    Coordinate coordinate = f.getGeometry().getCentroid().getCoordinate();
    g.setColor(fillColor);

    if (viewport.getEnvelopeInModelCoordinates().contains(coordinate)) {
      paint(g, viewport.toViewPoint(new Point2D.Double(coordinate.x,
        coordinate.y)));
    }
  }

  public void paint(final Graphics2D g, final Point2D p) {
    setFrame(p);
    render(g);
  }

  private void setFrame(final Point2D p) {
    // UT
    /*
     * shape.setFrame(p.getX() - (getSize() / 2d), p.getY() - (getSize() / 2d),
     * getSize(), getSize());
     */
    ((RectangularShape)shape).setFrame(p.getX() - (getSize() / 2d), p.getY()
      - (getSize() / 2d), getSize(), getSize());
  }

  protected void render(final Graphics2D g) {
    // UT was
    // g.fill(shape);

    // deeJUMP
    g.setColor(strokeColor);
    g.draw(shape);
    g.setColor(fillColor);
    g.fill(shape);
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      Assert.shouldNeverReachHere();

      return null;
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getName() {
    return "Centroid Style";
  }
}
