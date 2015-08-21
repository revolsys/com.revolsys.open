package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import javax.measure.Measure;
import javax.measure.quantity.Length;

import org.slf4j.LoggerFactory;

import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGException;
import com.kitfox.svg.SVGUniverse;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

public class SvgMarker extends AbstractMarker {

  private String name;

  private SVGDiagram shape;

  public SvgMarker(final Resource resource) {
    if (resource != null) {
      try {
        this.shape = new SVGUniverse().getDiagram(resource.getURI());
      } catch (final IOException e) {
        throw new IllegalArgumentException("Cannot get URI for " + resource, e);
      }
    }
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics, final MarkerStyle style,
    final double modelX, final double modelY, double orientation) {

    final AffineTransform savedTransform = graphics.getTransform();
    final Measure<Length> markerWidth = style.getMarkerWidth();
    final double mapWidth = Viewport2D.toDisplayValue(viewport, markerWidth);
    final Measure<Length> markerHeight = style.getMarkerHeight();
    final double mapHeight = Viewport2D.toDisplayValue(viewport, markerHeight);
    final String orientationType = style.getMarkerOrientationType();
    if ("none".equals(orientationType)) {
      orientation = 0;
    }

    graphics.translate(modelX, modelY);
    final double markerOrientation = style.getMarkerOrientation();
    orientation = orientation + markerOrientation;
    if (orientation != 0) {
      graphics.rotate(Math.toRadians(orientation));
    }

    final Measure<Length> deltaX = style.getMarkerDx();
    final Measure<Length> deltaY = style.getMarkerDy();
    double dx = Viewport2D.toDisplayValue(viewport, deltaX);
    double dy = Viewport2D.toDisplayValue(viewport, deltaY);

    final String verticalAlignment = style.getMarkerVerticalAlignment();
    if ("bottom".equals(verticalAlignment)) {
      dy -= mapHeight;
    } else if ("middle".equals(verticalAlignment)) {
      dy -= mapHeight / 2;
    }
    final String horizontalAlignment = style.getMarkerHorizontalAlignment();
    if ("right".equals(horizontalAlignment)) {
      dx -= mapWidth;
    } else if ("center".equals(horizontalAlignment)) {
      dx -= mapWidth / 2;
    }

    graphics.translate(dx, dy);

    // TODO scale image

    try {
      this.shape.render(graphics);
    } catch (final SVGException e) {
      LoggerFactory.getLogger(getClass()).error("Unable to render", e);
    }
    graphics.setTransform(savedTransform);
  }

  @Override
  public String toString() {
    if (this.name == null) {
      return "unknown";
    } else {
      return this.name;
    }
  }
}
