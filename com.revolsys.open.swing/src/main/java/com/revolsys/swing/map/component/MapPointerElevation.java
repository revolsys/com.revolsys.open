package com.revolsys.swing.map.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;

import com.revolsys.geometry.model.Point;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.elevation.gridded.IGriddedElevationModelLayer;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.number.Doubles;

public class MapPointerElevation extends JLabel implements MouseMotionListener {
  public class ElevationWorker extends AbstractSwingWorker<Double, Void> {

    private final Point mapLocation;

    public ElevationWorker(final Point mapLocation) {
      this.mapLocation = mapLocation;
    }

    @Override
    protected Double handleBackground() {
      if (isCancelled()) {
        return Double.NaN;
      } else {
        return IGriddedElevationModelLayer.getElevation(getLayers(), this.mapLocation);
      }
    }

    @Override
    protected void handleDone(final Double elevation) {
      if (Double.isNaN(elevation) || getLayers().isEmpty()) {
        setVisible(false);
      } else {
        setVisible(true);
        final String text = Doubles.toString(Doubles.makePrecise(1000, elevation));
        setText(text);
      }
    }

    @Override
    public String toString() {
      return "Get map elevation";
    }
  }

  private static final long serialVersionUID = 1L;

  private Viewport2D viewport;

  private SwingWorker<Double, Void> worker;

  private final Project project;

  private List<IGriddedElevationModelLayer> layers = Collections.emptyList();

  private final Object layerSync = new Object();

  public MapPointerElevation(final MapPanel map) {
    this.project = map.getProject();
    this.viewport = map.getViewport();
    map.getMouseOverlay().addMouseMotionListener(this);
    setBorder(
      BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED),
        BorderFactory.createEmptyBorder(2, 3, 2, 3)));
    setText(" ");

    final int height = getPreferredSize().height;
    setPreferredSize(new Dimension(100, height));
    setVisible(false);
    final PropertyChangeListener listener = e -> {
      synchronized (this.layerSync) {
        this.layers = null;
      }
    };
    this.project.addPropertyChangeListener("layers", listener);
    this.project.addPropertyChangeListener("visible", listener);
  }

  private List<IGriddedElevationModelLayer> getLayers() {
    if (this.layers == null) {
      synchronized (this.layerSync) {
        if (this.layers == null) {
          final double scale = this.viewport.getScale();
          this.layers = IGriddedElevationModelLayer.getVisibleLayers(this.project, scale);
          if (this.layers.isEmpty()) {
            Invoke.later(() -> this.setVisible(false));
          }
        }
      }
    }
    return this.layers;
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

    if (!getLayers().isEmpty()) {
      final SwingWorker<Double, Void> oldWorker = this.worker;
      if (oldWorker != null) {
        oldWorker.isCancelled();
      }
      final SwingWorker<Double, Void> worker = new ElevationWorker(mapLocation);
      this.worker = worker;
      Invoke.worker(worker);
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    this.viewport = null;
  }

  @Override
  public String toString() {
    return getText();
  }
}
