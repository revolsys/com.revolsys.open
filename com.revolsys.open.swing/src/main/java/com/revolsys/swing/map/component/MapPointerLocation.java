package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.swing.map.Viewport2D;
import com.vividsolutions.jts.geom.Point;

@SuppressWarnings("serial")
public class MapPointerLocation extends JLabel implements MouseMotionListener {
  private static final NumberFormat FORMAT = new DecimalFormat(
    "############.############");

  private final Viewport2D viewport;

  private final GeometryFactory geometryFactory;

  private final String title;

  public MapPointerLocation(final Viewport2D viewport, final String title,
    final GeometryFactory geometryFactory) {
    this.viewport = viewport;
    this.title = title;
    this.geometryFactory = geometryFactory;

    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    setPreferredSize(new Dimension(250, 20));
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {

    final java.awt.Point point = e.getPoint();
    final Point mapPoint = viewport.toModelPointRounded(geometryFactory, point);
    if (!mapPoint.isEmpty()) {
      final double projectedX = mapPoint.getX();
      final double projectedY = mapPoint.getY();
      if (geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
        setText(title + ": " + FORMAT.format(projectedY) + ", "
          + FORMAT.format(projectedX));
      } else {
        setText(title + ": " + FORMAT.format(projectedX) + ", "
          + FORMAT.format(projectedY));
      }
    }
  }
}
