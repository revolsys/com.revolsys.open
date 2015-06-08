package com.revolsys.swing.map.overlay;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.raster.BufferedGeoReferencedImage;
import com.revolsys.raster.GeoReferencedImage;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayerRenderer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;

/**
 * <p>A lightweight component that users the {@link Layer}'s {@link LayerRenderer} to render the layer.</p>
 */
public class LayerRendererOverlay extends JComponent implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private static final Collection<String> IGNORE_PROPERTY_NAMES = new HashSet<>(Arrays.asList(
    "selectionCount", "hasHighlightedRecords", "highlightedCount", "scale"));

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
    if (!(this.layer instanceof NullLayer)) {
      GeoReferencedImage image;
      synchronized (this.loadSync) {
        image = this.image;

        if ((image == null || this.loadImage) && this.imageWorker == null) {
          final BoundingBox boundingBox = this.viewport.getBoundingBox();
          final int viewWidthPixels = this.viewport.getViewWidthPixels();
          final int viewHeightPixels = this.viewport.getViewHeightPixels();
          final GeoReferencedImage loadImage = new BufferedGeoReferencedImage(boundingBox,
            viewWidthPixels, viewHeightPixels);
          this.imageWorker = new LayerRendererOverlaySwingWorker(this, loadImage);
          Invoke.worker(this.imageWorker);
        }
      }
      if (image != null) {
        render((Graphics2D)g);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    if (!(e.getSource() instanceof MapPanel)) {
      final String propertyName = e.getPropertyName();
      if (!IGNORE_PROPERTY_NAMES.contains(propertyName)) {
        redraw();
      }
    }
  }

  public void redraw() {
    if (this.layer != null && getWidth() > 0 && getHeight() > 0 && this.layer.isExists()
      && this.layer.isVisible()) {
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
    if (this.layer != null) {
      this.layer.refresh();
    }
  }

  private void render(final Graphics2D graphics) {
    GeoReferencedImageLayerRenderer.render(this.viewport, graphics, this.image, false);
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
        Property.removeListener(old, this);
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
