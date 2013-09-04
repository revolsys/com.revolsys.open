package com.revolsys.swing.map.overlay;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.parallel.Invoke;

/**
 * <p>A lightweight component that users the {@link Layer}'s {@link LayerRenderer} to render the layer.</p>
 */
public class LayerRendererOverlay extends JComponent implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private Layer layer;

  private Viewport2D viewport;

  private GeoReferencedImage image;

  private final Object loadSync = new Object();

  private LayerRendererOverlaySwingWorker imageWorker;

  public LayerRendererOverlay(final MapPanel mapPanel) {
    this(mapPanel, null);
  }

  public LayerRendererOverlay(final MapPanel mapPanel, final Layer layer) {
    this.viewport = mapPanel.getViewport();
    setLayer(layer);
    this.viewport.addPropertyChangeListener(this);
    addPropertyChangeListener(mapPanel);
  }

  public void dispose() {
    if (this.layer != null) {
      this.layer.removePropertyChangeListener(this);
      this.layer = null;
    }
    this.viewport = null;
  }

  public Layer getLayer() {
    return this.layer;
  }

  public LayerGroup getProject() {
    return this.layer.getProject();
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  @Override
  public void paintComponent(final Graphics g) {
    GeoReferencedImage image;
    synchronized (this.loadSync) {
      image = this.image;

      if (image == null && imageWorker == null) {
        final BoundingBox boundingBox = this.viewport.getBoundingBox();
        final int viewWidthPixels = this.viewport.getViewWidthPixels();
        final int viewHeightPixels = this.viewport.getViewHeightPixels();
        final GeoReferencedImage loadImage = new GeoReferencedImage(
          boundingBox, viewWidthPixels, viewHeightPixels);
        this.imageWorker = new LayerRendererOverlaySwingWorker(this, loadImage);
        Invoke.worker(this.imageWorker);
      }
    }
    GeoReferencedImageLayerRenderer.render(this.viewport, (Graphics2D)g, image);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (!(e.getSource() instanceof MapPanel)) {
      final String propertyName = e.getPropertyName();
      if (!propertyName.equals("hasSelectedRecords")
        && !propertyName.equals("selectionCount")) {
        redraw();
      }
    }
  }

  public void redraw() {
    synchronized (this.loadSync) {
      this.image = null;
      if (this.imageWorker != null) {
        this.imageWorker.cancel(true);
        this.imageWorker = null;
      }
      firePropertyChange("imageLoaded", true, false);
    }
  }

  public void setImage(final LayerRendererOverlaySwingWorker imageWorker) {
    synchronized (this.loadSync) {
      if (this.imageWorker == imageWorker) {
        this.image = imageWorker.getReferencedImage();
        if (this.image != null) {
          this.imageWorker = null;
        }
        firePropertyChange("imageLoaded", false, true);
      }
    }
  }

  public void setLayer(final Layer layer) {
    final Layer old = this.layer;
    if (old != layer) {
      if (old != null) {
        old.setVisible(false);
        old.removePropertyChangeListener(this);
      }
      this.layer = layer;
      if (layer != null) {
        layer.addPropertyChangeListener(this);
        layer.setVisible(true);
      }
      redraw();
      firePropertyChange("layer", old, layer);
    }
  }
}
