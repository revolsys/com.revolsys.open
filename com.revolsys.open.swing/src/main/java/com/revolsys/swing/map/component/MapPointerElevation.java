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
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.store.Overwrite;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.elevation.gridded.IGriddedElevationModelLayer;
import com.revolsys.swing.parallel.AbstractSwingWorker;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.parallel.MaxThreadsSwingWorker;
import com.revolsys.util.number.Doubles;

public class MapPointerElevation extends JLabel implements MouseMotionListener {
  public class ElevationWorker extends AbstractSwingWorker<Double, Void>
    implements MaxThreadsSwingWorker {

    private final Point mapLocation;

    public ElevationWorker(final Point mapLocation) {
      this.mapLocation = mapLocation;
    }

    @Override
    public int getMaxThreads() {
      return 1;
    }

    @Override
    protected Double handleBackground() {
      if (isCancelled()) {
        return Double.NaN;
      } else {
        final int refreshIndex = MapPointerElevation.this.refreshIndex;

        final List<IGriddedElevationModelLayer> layers = getLayers();
        return IGriddedElevationModelLayer.getElevation(layers, this.mapLocation);
      }
    }

    @Override
    protected void handleDone(final Double elevation) {
      if (Double.isFinite(elevation) && !getLayers().isEmpty()) {
        setVisible(true);
        final String text = Doubles.toString(Doubles.makePrecise(1000, elevation));
        setText(text);
      } else {
        setVisible(false);
      }
    }

    @Override
    public String toString() {
      return "Get map elevation";
    }
  }

  private static final long serialVersionUID = 1L;

  private final Channel<Point> refreshChannel = new Channel<>(new Overwrite<>());

  private final int refreshIndex = 0;

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
    new Thread(() -> {
      while (true) {
        final Point point = this.refreshChannel.read();
        if (point == null) {
          return;
        } else {
          final List<IGriddedElevationModelLayer> layers = getLayers();
          if (!layers.isEmpty()) {

            final double elevation = IGriddedElevationModelLayer.getElevation(layers, point);
            Invoke.later(() -> {
              synchronized (this.layerSync) {
                if (Double.isFinite(elevation) && !getLayers().isEmpty()) {
                  setVisible(true);
                  final String text = Doubles.toString(Doubles.makePrecise(1000, elevation));
                  setText(text);
                } else {
                  setVisible(false);
                }
              }
            });
          }
        }
      }
    }, "map-get-elevation").start();
  }

  private List<IGriddedElevationModelLayer> getLayers() {
    if (this.layers == null) {
      synchronized (this.layerSync) {
        if (this.layers == null) {
          final double scale = this.viewport.getScale();
          this.layers = IGriddedElevationModelLayer.getVisibleLayers(this.project, scale);
          final boolean hasLayers = !this.layers.isEmpty();
          Invoke.later(() -> this.setVisible(hasLayers));
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
      this.refreshChannel.write(mapLocation);
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
