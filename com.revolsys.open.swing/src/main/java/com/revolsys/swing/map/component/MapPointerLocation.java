package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class MapPointerLocation extends JLabel
  implements MouseMotionListener, PropertyChangeListener {

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
    setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(2, 3, 2, 3)));
    setText(" ");

    final int height2 = getPreferredSize().height;
    setPreferredSize(new Dimension(250, height2));
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int x = e.getX();
    final int y = e.getY();
    final Point mapPoint = this.viewport.toModelPointRounded(this.geometryFactory, x, y);
    if (!mapPoint.isEmpty()) {
      final double projectedX = mapPoint.getX();
      final String textX = MathUtil.toString(projectedX);
      final double projectedY = mapPoint.getY();
      final String textY = MathUtil.toString(projectedY);
      if (this.geometryFactory.isGeographics()) {
        setText(this.title + ": " + textY + ", " + textX);
      } else {
        setText(this.title + ": " + textX + ", " + textY);
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
    if (this.geographics && geometryFactory.isGeographics()) {
      setVisible(false);
    } else {
      setVisible(true);
    }
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
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    this.setToolTipText(coordinateSystem.getName());
    this.title = String.valueOf(srid);

  }
}
