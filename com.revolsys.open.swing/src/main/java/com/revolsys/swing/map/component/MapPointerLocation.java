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

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.ProjectedCoordinateSystem;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.Property;
import com.vividsolutions.jts.geom.Point;

public class MapPointerLocation extends JLabel implements MouseMotionListener,
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private static NumberFormat getFormat() {
    return new DecimalFormat("############.############");
  }

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
    final java.awt.Point point = e.getPoint();
    final Point mapPoint = this.viewport.toModelPointRounded(
      this.geometryFactory, point);
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

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    double scaleFactor;
    CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();

    if (this.geographics) {
      if (coordinateSystem instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
        coordinateSystem = projectedCoordinateSystem.getGeographicCoordinateSystem();
      }
      scaleFactor = 10000000;
    } else {
      scaleFactor = 1000;
    }
    final int srid = coordinateSystem.getId();
    this.geometryFactory = GeometryFactory.getFactory(srid, 2, scaleFactor,
      scaleFactor);
    this.setToolTipText(coordinateSystem.getName());
    this.title = String.valueOf(srid);

  }
}
