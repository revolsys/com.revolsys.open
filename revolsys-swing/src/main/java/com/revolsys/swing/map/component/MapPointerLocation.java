package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

public class MapPointerLocation extends JLabel implements MouseMotionListener {
  private static final long serialVersionUID = 1L;

  private final boolean geographics;

  private GeometryFactory geometryFactory;

  private String title;

  private Viewport2D viewport;

  private Point mapLocation;

  private PropertyChangeListener geometryFactoryListener;

  public MapPointerLocation(final MapPanel map, final boolean geographics) {
    this.viewport = map.getViewport();
    this.geographics = geographics;
    setGeometryFactory(this.viewport.getGeometryFactory());

    Property.addListenerNewValueSource(this.viewport, "geometryFactory", this::setGeometryFactory);
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
    final Point mapLocation = this.viewport.toModelPointRounded(this.geometryFactory, x, y);
    setMapLocation(mapLocation);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    Property.removeListener(this.viewport, "geometryFactory", this.geometryFactoryListener);
    this.viewport = null;
    this.geometryFactoryListener = null;
  }

  public void setGeometryFactory(GeometryFactory geometryFactory) {
    if (this.geographics && geometryFactory.isGeographic()) {
      setVisible(false);
    } else {
      setVisible(true);
    }
    geometryFactory = geometryFactory.convertAxisCount(2);
    if (geometryFactory.isGeographic()) {
      geometryFactory = geometryFactory.convertScales(10000000.0, 10000000.0);
    } else if (this.geographics) {
      geometryFactory = geometryFactory.getGeographicGeometryFactory();
      geometryFactory = geometryFactory.convertScales(10000000.0, 10000000.0);
    } else {
      geometryFactory = geometryFactory.convertScales(1000.0, 1000.0);
    }
    if (geometryFactory != this.geometryFactory) {
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();
      this.geometryFactory = geometryFactory;
      this.setToolTipText(geometryFactory.getHorizontalCoordinateSystemName());
      this.title = String.valueOf(srid);
      final Point mapLocation = geometryFactory.point(this.mapLocation);
      setMapLocation(mapLocation);
    }
  }

  protected void setMapLocation(final Point mapLocation) {
    Invoke.later(() -> {
      this.mapLocation = mapLocation;
      String text;
      if (Property.isEmpty(mapLocation)) {
        text = this.title;
      } else {
        final double projectedX = mapLocation.getX();
        final String textX = Doubles.toString(projectedX);
        final double projectedY = mapLocation.getY();
        final String textY = Doubles.toString(projectedY);
        if (this.geometryFactory.isGeographic()) {
          text = this.title + ": " + textY + ", " + textX;
        } else {
          text = this.title + ": " + textX + ", " + textY;
        }
      }
      setText(text);
    });
  }

  @Override
  public String toString() {
    return this.title;
  }
}
