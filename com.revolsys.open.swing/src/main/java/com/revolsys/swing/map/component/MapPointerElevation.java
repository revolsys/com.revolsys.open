package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.elevation.gridded.GriddedElevationModelLayer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.number.Doubles;

public class MapPointerElevation extends JLabel implements MouseMotionListener {
  private static final long serialVersionUID = 1L;

  private Viewport2D viewport;

  private final MapPanel map;

  public MapPointerElevation(final MapPanel map) {
    this.map = map;
    this.viewport = map.getViewport();
    map.getMouseOverlay().addMouseMotionListener(this);
    setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(2, 3, 2, 3)));
    setText(" ");

    final int height = getPreferredSize().height;
    setPreferredSize(new Dimension(100, height));
  }

  @Override
  public void mouseDragged(final MouseEvent e) {
    mouseMoved(e);
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    final int x = e.getX();
    final int y = e.getY();
    final Point mapLocation = this.viewport.toModelPoint(x, y);
    for (final GriddedElevationModelLayer layer : this.map.getProject()
      .getVisibleDescendants(GriddedElevationModelLayer.class, this.viewport.getScale())) {
      final double layerElevation = layer.getElevation(mapLocation);
      if (!Double.isNaN(layerElevation)) {
        final double elevation = layerElevation;
        setMapElevation(layer.getGeometryFactory(), elevation);
        return;
      }
    }

    setMapElevation(null, Double.NaN);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    this.viewport = null;
  }

  protected void setMapElevation(final GeometryFactory geometryFactory, final double elevation) {
    Invoke.later(() -> {
      if (Double.isNaN(elevation)) {
        setVisible(false);
      } else {
        setVisible(true);
        final String text = Doubles.toString(Doubles.makePrecise(1000, elevation));
        setText(text);
      }
    });
  }

  @Override
  public String toString() {
    return getText();
  }
}
