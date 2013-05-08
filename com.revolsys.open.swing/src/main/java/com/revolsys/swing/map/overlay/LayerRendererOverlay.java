package com.revolsys.swing.map.overlay;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.SwingWorkerManager;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;

/**
 * <p>A lightweight component that users the {@link Layer}'s {@link LayerRenderer} to render the layer.</p>
 */
@SuppressWarnings("serial")
public class LayerRendererOverlay extends JComponent implements
  PropertyChangeListener {

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
    viewport.addPropertyChangeListener(this);
    addPropertyChangeListener(mapPanel);
  }

  public void dispose() {
    if (layer != null) {
      layer.removePropertyChangeListener(this);
      layer = null;
    }
    viewport = null;
  }

  public Layer getLayer() {
    return layer;
  }

  public Project getProject() {
    return layer.getProject();
  }

  public Viewport2D getViewport() {
    return viewport;
  }

  @Override
  public void paintComponent(final Graphics g) {
    GeoReferencedImage image;
    synchronized (loadSync) {
      image = this.image;

      if (image == null) {
        final BoundingBox boundingBox = viewport.getBoundingBox();
        final int viewWidthPixels = viewport.getViewWidthPixels();
        final int viewHeightPixels = viewport.getViewHeightPixels();
        final GeoReferencedImage loadImage = new MapTile(boundingBox,
          viewWidthPixels, viewHeightPixels);
        imageWorker = new LayerRendererOverlaySwingWorker(this, loadImage);
        SwingWorkerManager.execute(imageWorker);
      }
    }
    GeoReferencedImageLayerRenderer.render(viewport, (Graphics2D)g, image);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (!(e.getSource() instanceof MapPanel)) {
      redraw();
    }
  }

  public void redraw() {
    synchronized (loadSync) {
      image = null;
      if (imageWorker != null) {
        imageWorker.cancel(true);
        imageWorker = null;
      }
      firePropertyChange("imageLoaded", true, false);
    }
  }

  public void setImage(final LayerRendererOverlaySwingWorker imageWorker) {
    synchronized (loadSync) {
      if (this.imageWorker == imageWorker) {
        this.image = imageWorker.getReferencedImage();
        if (image != null) {
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
        layer.removePropertyChangeListener(this);
      }
      this.layer = layer;
      if (layer != null) {
        layer.addPropertyChangeListener(this);
      }
      redraw();
      firePropertyChange("layer", old, layer);
    }
  }
}
