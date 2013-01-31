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
  private static final NumberFormat FORMAT = new DecimalFormat("############.############");
   private final Viewport2D viewport;

  private final GeometryFactory geometryFactory;

  private String title;

  public MapPointerLocation(final Viewport2D viewport, String title,
    final GeometryFactory geometryFactory) {
    this.viewport = viewport;
    this.title = title;
    this.geometryFactory = geometryFactory;

    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    setPreferredSize(new Dimension(200, 20));
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int screenX = e.getX();
    final int screenY = e.getY();
   
    final Point mapPoint =  viewport.toModelPoint(screenX, screenY);
    if (!mapPoint.isEmpty()) {
      final Point projectedPoint = geometryFactory.copy(mapPoint);
      if (!projectedPoint.isEmpty()) {
        double projectedX = projectedPoint.getX();
        double projectedY = projectedPoint.getY();
        if (geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
          setText(title + ": " + FORMAT.format(projectedY) + ", " + FORMAT.format(projectedX));
        } else {
          setText(title + ": " + FORMAT.format(projectedX) + ", " + FORMAT.format(projectedY));
        }
      }
    }
  }
}
