/*
 * The Unified Mapping Platform (JUMP) is an extensible, interactive GUI
 * for visualizing and manipulating spatial features with geometry and attributes.
 *
 * Copyright (C) 2003 Vivid Solutions
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * Vivid Solutions
 * Suite #1A
 * 2328 Government Street
 * Victoria BC  V8T 5G5
 * Canada
 *
 * (250)385-6040
 * www.vividsolutions.com
 */
package com.revolsys.jump.saif.style;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.quadtree.Quadtree;
import com.vividsolutions.jts.util.Assert;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.geom.Angle;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.ui.Viewport;
import com.vividsolutions.jump.workbench.ui.renderer.style.Style;

public class AnnotatedPointStyle implements Style {
  public static final int FONT_BASE_SIZE = 12;

  public static final String ABOVE_LINE = "ABOVE_LINE";

  public static final String ON_LINE = "ON_LINE";

  public static final String BELOW_LINE = "BELOW_LINE";

  private Color originalColor;

  private AffineTransform originalTransform;

  private Layer layer;

  private Quadtree labelsDrawn = null;

  private boolean enabled = true;

  private Color color = Color.black;

  private Font font = new Font("Dialog", Font.PLAIN, FONT_BASE_SIZE);

  private boolean scaling = true;

  private double height = 12;

  private boolean hidingOverlappingLabels = true;

  private String verticalAlignment = ON_LINE;

  public AnnotatedPointStyle() {
  }

  public void initialize(final Layer layer) {
    labelsDrawn = new Quadtree();
    this.layer = layer;
  }

  public void paint(final Feature feature, final Graphics2D graphics,
    final Viewport viewport) throws NoninvertibleTransformException {
    Geometry geometry = feature.getGeometry();
    if (geometry instanceof Point) {
      Point point = (Point)geometry;
      paint(feature, point, graphics, viewport);
    } else if (geometry instanceof MultiPoint) {
      MultiPoint multiPoint = (MultiPoint)geometry;
      paint(feature, multiPoint, graphics, viewport);
    } else {
      System.out.println("Unknown geometry: " + geometry);
    }
  }

  private void paint(final Feature feature, final MultiPoint multiPoint,
    final Graphics2D graphics, final Viewport viewport)
    throws NoninvertibleTransformException {

    for (int i = 0; i < multiPoint.getNumPoints(); i++) {
      Point point = (Point)multiPoint.getGeometryN(i);
      paint(feature, point, graphics, viewport);
    }

  }

  @SuppressWarnings("unchecked")
  private void paint(final Feature feature, final Point point,
    final Graphics2D graphics, final Viewport viewport)
    throws NoninvertibleTransformException {
    Map<String, Object> values = (Map<String, Object>)point.getUserData();
    if (values != null) {
      Object label = values.get("text");
      if (label != null && label.toString().trim().length() > 0) {
        Coordinate coordinate = point.getCoordinate();
        Point2D labelLocation = viewport.toViewPoint(new Point2D.Double(
          coordinate.x, coordinate.y));
        paint(graphics, label.toString(), viewport.getScale(), labelLocation,
          angle(values, 0), height(values, getHeight()), false);
      }
    }
  }

  public static double angle(final Map<String, Object> values,
    final double defaultAngle) {
    Object orientation = values.get("orientation");
    if (orientation instanceof Number) {
      Number angle = (Number)orientation;
      return Angle.toRadians((450 - angle.doubleValue()) % 360);
    } else {
      return defaultAngle;
    }
  }

  public static double height(final Map<String, Object> values,
    final double defaultHeight) {
    Object characterHeight = values.get("characterHeight");
    if (characterHeight instanceof Number) {
      Number height = (Number)characterHeight;
      return height.doubleValue();
    } else {
      return defaultHeight;
    }
  }

  public void paint(final Graphics2D g, final String text,
    final double viewportScale, final Point2D viewCentre, final double angle,
    final double height, final boolean linear) {
    setup(g);
    try {
      double scale = height / getFont().getSize2D();
      if (isScaling()) {
        scale *= viewportScale;
      }
      g.setColor(getColor());
      TextLayout layout = new TextLayout(text, getFont(),
        g.getFontRenderContext());
      AffineTransform transform = g.getTransform();
      configureTransform(transform, viewCentre, scale, layout, angle, linear);
      g.setTransform(transform);
      if (isHidingOverlappingLabels()) {
        Area transformedLabelBounds = new Area(layout.getBounds()).createTransformedArea(transform);
        Envelope transformedLabelBoundsEnvelope = envelope(transformedLabelBounds);
        if (collidesWithExistingLabel(transformedLabelBounds,
          transformedLabelBoundsEnvelope)) {
          return;
        }
        labelsDrawn.insert(transformedLabelBoundsEnvelope,
          transformedLabelBounds);
      }
      layout.draw(g, 0, 0);
    } finally {
      cleanup(g);
    }
  }

  private Envelope envelope(final Shape shape) {
    Rectangle2D bounds = shape.getBounds2D();
    return new Envelope(bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(),
      bounds.getMaxY());
  }

  @SuppressWarnings("unchecked")
  private boolean collidesWithExistingLabel(final Area transformedLabelBounds,
    final Envelope transformedLabelBoundsEnvelope) {
    List<Area> potentialCollisions = labelsDrawn.query(transformedLabelBoundsEnvelope);
    for (Area potentialCollision : potentialCollisions) {
      Area intersection = new Area(potentialCollision);
      intersection.intersect(transformedLabelBounds);
      if (!intersection.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  private void setup(final Graphics2D g) {
    originalTransform = g.getTransform();
    originalColor = g.getColor();
  }

  private void cleanup(final Graphics2D g) {
    g.setTransform(originalTransform);
    g.setColor(originalColor);
  }

  private void configureTransform(final AffineTransform transform,
    final Point2D viewCentre, final double scale, final TextLayout layout,
    final double angle, final boolean linear) {
    double xTranslation = viewCentre.getX();
    double yTranslation = viewCentre.getY();
    if (linear) {
      yTranslation -= verticalAlignmentOffset(scale
        * layout.getBounds().getHeight());
    }
    // Negate the angle because the positive y-axis points downwards.
    // See the #rotate JavaDoc. [Jon Aquino]
    transform.rotate(-angle, viewCentre.getX(), viewCentre.getY());
    transform.translate(xTranslation, yTranslation);
    transform.scale(scale, scale);
  }

  private double verticalAlignmentOffset(final double scaledLabelHeight) {
    if (getVerticalAlignment().equals(ON_LINE)) {
      return 0;
    }
    double buffer = 3;
    double offset = buffer + (layer.getBasicStyle().getLineWidth() / 2d)
      + (scaledLabelHeight / 2d);
    if (getVerticalAlignment().equals(ABOVE_LINE)) {
      return offset;
    }
    if (getVerticalAlignment().equals(BELOW_LINE)) {
      return -offset;
    }
    Assert.shouldNeverReachHere();
    return 0;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public Color getColor() {
    return color;
  }

  public Font getFont() {
    return font;
  }

  public boolean isScaling() {
    return scaling;
  }

  public double getHeight() {
    return height;
  }

  public boolean isHidingOverlappingLabels() {
    return hidingOverlappingLabels;
  }

  public String getVerticalAlignment() {
    return verticalAlignment;
  }

  public void setVerticalAlignment(final String verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  public void setColor(final Color color) {
    this.color = color;
  }

  public void setFont(final Font font) {
    this.font = font;
  }

  public void setScaling(final boolean scaling) {
    this.scaling = scaling;
  }

  public void setHeight(final double height) {
    this.height = height;
  }

  public void setHidingOverlappingLabels(final boolean hidingOverlappingLabels) {
    this.hidingOverlappingLabels = hidingOverlappingLabels;
  }

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      Assert.shouldNeverReachHere();
      return null;
    }
  }

}
