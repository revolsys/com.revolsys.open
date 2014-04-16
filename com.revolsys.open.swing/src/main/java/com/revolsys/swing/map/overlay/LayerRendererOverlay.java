package com.revolsys.swing.map.overlay;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

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

  private boolean loadImage = true;

  public LayerRendererOverlay(final MapPanel mapPanel) {
    this(mapPanel, null);
  }

  public LayerRendererOverlay(final MapPanel mapPanel, final Layer layer) {
    this.viewport = mapPanel.getViewport();
    setLayer(layer);
    Property.addListener(this.viewport, this);
    Property.addListener(this, mapPanel);
  }

  public void dispose() {
    if (this.layer != null) {
      Property.removeListener(this.layer, this);
      this.layer = null;
    }
    Property.removeAllListeners(this);
    this.image = null;
    this.imageWorker = null;
    this.viewport = null;
  }

  public Layer getLayer() {
    return this.layer;
  }

  public Project getProject() {
    return this.layer.getProject();
  }

  public Viewport2D getViewport() {
    return this.viewport;
  }

  @Override
  public void paintComponent(final Graphics g) {
    if (!(layer instanceof NullLayer)) {
      GeoReferencedImage image;
      synchronized (this.loadSync) {
        image = this.image;

        if ((image == null || this.loadImage) && imageWorker == null) {
          final BoundingBox boundingBox = this.viewport.getBoundingBox();
          final int viewWidthPixels = this.viewport.getViewWidthPixels();
          final int viewHeightPixels = this.viewport.getViewHeightPixels();
          final GeoReferencedImage loadImage = new GeoReferencedImage(
            boundingBox, viewWidthPixels, viewHeightPixels);
          this.imageWorker = new LayerRendererOverlaySwingWorker(this,
            loadImage);
          Invoke.worker(this.imageWorker);
        }
      }
      GeoReferencedImageLayerRenderer.render(this.viewport, (Graphics2D)g,
        image);
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (!(e.getSource() instanceof MapPanel)) {
      final String propertyName = e.getPropertyName();
      if (!propertyName.equals("selectionCount")
        && !propertyName.equals("hasHighlightedRecords")
        && !propertyName.equals("highlightedCount")) {
        redraw();
      }
    }
  }

  public void redraw() {
    if (isValid() && layer.isExists() && layer.isVisible()) {
      synchronized (this.loadSync) {
        this.loadImage = true;
        if (this.imageWorker != null) {
          this.imageWorker.cancel(true);
          this.imageWorker = null;
        }
        firePropertyChange("imageLoaded", true, false);
      }
    }
  }

  public void refresh() {
    if (layer != null) {
      layer.refresh();
    }
  }

  public void setImage(final LayerRendererOverlaySwingWorker imageWorker) {
    synchronized (this.loadSync) {
      if (this.imageWorker == imageWorker) {
        this.image = imageWorker.getReferencedImage();
        if (this.image != null) {
          this.loadImage = false;
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
        Property.addListener(old, this);
      }
      this.layer = layer;
      if (layer != null) {
        Property.addListener(layer, this);
        layer.refresh();
        layer.setVisible(true);
      }
      redraw();
      firePropertyChange("layer", old, layer);
    }
  }
}
