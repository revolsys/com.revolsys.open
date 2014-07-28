package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.Property;

public class MapPointerLocation extends JLabel implements MouseMotionListener,
PropertyChangeListener {
  private static NumberFormat getFormat() {
    return new DecimalFormat("############.############");
  }

  private static final long serialVersionUID = 1L;

  private final Viewport2D viewport;

  private GeometryFactory geometryFactory;

  private String title;

  private final boolean geographics;

  private final Reference<MapPanel> map;

  public MapPointerLocation(final MapPanel map, final boolean geographics) {
    this.map = new WeakReference<MapPanel>(map);
    this.viewport = map.getViewport();
    this.geographics = geographics;
    setGeometryFactory(map.getGeometryFactory());

    Property.addListener(map, "geometryFactory", this);
    map.getMouseOverlay().addMouseMotionListener(this);

    setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    setPreferredSize(new Dimension(250, 30));
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final Point mapPoint = this.viewport.toModelPointRounded(
      this.geometryFactory, e.getX(), e.getY());
    if (!mapPoint.isEmpty()) {
      final double projectedX = mapPoint.getX();
      final double projectedY = mapPoint.getY();
      if (this.geometryFactory.getCoordinateSystem() instanceof GeographicCoordinateSystem) {
        setText(this.title + ": " + getFormat().format(projectedY) + ", "
            + getFormat().format(projectedX));
      } else {
        setText(this.title + ": " + getFormat().format(projectedX) + ", "
            + getFormat().format(projectedY));
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final MapPanel map = this.map.get();
    if (map != null) {
      setGeometryFactory(map.getGeometryFactory());
    }
  }

  public void setGeometryFactory(GeometryFactory geometryFactory) {
    geometryFactory = geometryFactory.convertAxisCount(2);

    if (this.geographics || geometryFactory.isGeographics()) {
      if (geometryFactory.isProjected()) {
        geometryFactory = geometryFactory.getGeographicGeometryFactory();
      }
      geometryFactory = geometryFactory.convertScales(10000000);
    } else {
      geometryFactory = geometryFactory.convertScales(1000);
    }
    final int srid = geometryFactory.getSrid();
    this.geometryFactory = geometryFactory;
    this.setToolTipText(geometryFactory.getCoordinateSystem().getName());
    this.title = String.valueOf(srid);

  }
}
