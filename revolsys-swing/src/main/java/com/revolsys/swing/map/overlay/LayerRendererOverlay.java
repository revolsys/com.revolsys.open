package com.revolsys.swing.map.overlay;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.ComponentViewport2D;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.BaseMapLayerGroup;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.util.Cancellable;
import com.revolsys.util.Property;

/**
 * <p>A lightweight component that users the {@link Layer}'s {@link LayerRenderer} to render the layer.</p>
 */
public class LayerRendererOverlay extends JComponent
  implements LayerMapOverlay, PropertyChangeListener {
  private static final Collection<String> IGNORE_PROPERTY_NAMES = new HashSet<>(Arrays
    .asList("selectionCount", "hasHighlightedRecords", "highlightedCount", "scale", "loaded"));

  private static final long serialVersionUID = 1L;

  private static final GeometryStyle STYLE_AREA = GeometryStyle.polygon(WebColors.FireBrick, 1,
    WebColors.newAlpha(WebColors.FireBrick, 16));

  private static final GeoreferencedImage EMPTY_IMAGE = new BufferedGeoreferencedImage(
    BoundingBox.empty(), 0, 0);

  private Layer layer;

  private ComponentViewport2D viewport;

  private boolean showAreaBoundingBox = false;

  private BackgroundRefreshResource<GeoreferencedImage> cachedImage = new BackgroundRefreshResource<>(
    "Render Layers", this::refreshImage);

  public LayerRendererOverlay(final MapPanel mapPanel) {
    this(mapPanel, null);
  }

  public LayerRendererOverlay(final MapPanel mapPanel, final Layer layer) {
    this.viewport = mapPanel.getViewport();

    setLayer(layer);
    Property.addListener(this.viewport, this);
    Property.addListener(this, mapPanel);
    this.cachedImage.addPropertyChangeListener(this);
  }

  @Override
  public void destroy() {
    if (this.layer != null) {
      Property.removeListener(this.layer, this);
      this.layer = null;
    }
    Property.removeAllListeners(this);
    this.cachedImage = null;
    this.viewport = null;
  }

  @Override
  public Layer getLayer() {
    return this.layer;
  }

  @Override
  public void paintComponent(final Graphics g) {
    if (!(this.layer instanceof NullLayer)) {
      final GeoreferencedImage image = this.cachedImage.getResource();
      if (image == null) {
        if (this.cachedImage.isNew()) {
          redraw();
        }
      } else {
        final Graphics2D graphics = (Graphics2D)g;
        final Graphics2DViewRenderer view = this.viewport.newViewRenderer(graphics);
        view.drawImage(image, false);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent e) {
    final Object source = e.getSource();
    if (source == this.cachedImage) {
      repaint();
    } else if (!(source instanceof MapPanel)) {
      if (source == this.layer) {
        if (this.layer.isDeleted()) {
          setLayer(NullLayer.INSTANCE);
        }
      }
      final String propertyName = e.getPropertyName();
      if (!IGNORE_PROPERTY_NAMES.contains(propertyName)) {
        if (this.layer instanceof Project) {
          final Project project = (Project)this.layer;
          if (AbstractTiledLayerRenderer.TILES_LOADED.equals(propertyName)) {
            if (source instanceof Layer) {
              final Layer eventLayer = (Layer)source;
              if (project.isBaseMapLayer(eventLayer)) {
                return;
              }
            }
          }
        }
        redraw();
      }
    }
  }

  @Override
  public void redraw() {
    final Container parent = getParent();
    if (getWidth() > 0 && getHeight() > 0) {
      if (parent != null && parent.isVisible()) {
        this.cachedImage.refresh();
      }
    }
  }

  @Override
  public void refresh() {
    if (this.layer != null) {
      this.layer.refresh();
    }
  }

  private GeoreferencedImage refreshImage(final Cancellable cancellable) {
    final Viewport2D viewport = this.viewport;
    if (viewport != null) {
      final double scaleForVisible = viewport.getScale();
      if (this.layer != null && this.layer.isExists() && viewport.isViewValid()
        && this.layer.isVisible(scaleForVisible)) {
        try (
          final ImageViewport imageViewport = new ImageViewport(viewport)) {
          final ViewRenderer view = imageViewport.newViewRenderer();
          view.setCancellable(cancellable);
          view.renderLayer(this.layer);
          if (this.showAreaBoundingBox) {
            final BoundingBox areaBoundingBox = view.getAreaBoundingBox();
            if (!areaBoundingBox.isEmpty() && !areaBoundingBox.bboxCovers(view)) {
              final Polygon viewportPolygon = view.bboxEdit(editor -> editor.expandPercent(0.1))
                .toPolygon(0);
              final Polygon areaPolygon = areaBoundingBox
                .bboxEdit(editor -> editor.expandDelta(imageViewport.getUnitsPerPixel()))
                .toPolygon(0);
              final Geometry drawPolygon = viewportPolygon.difference(areaPolygon);
              view.drawGeometry(drawPolygon, STYLE_AREA);
            }
          }
          if (!cancellable.isCancelled()) {
            return imageViewport.getGeoreferencedImage();
          }
        } catch (final Throwable t) {
          if (!cancellable.isCancelled()) {
            Logs.error(this, "Unable to paint", t);
          }
        }
      }
    }
    return EMPTY_IMAGE;
  }

  @Override
  public void setLayer(final Layer layer) {
    final Layer old = this.layer;
    if (old != layer) {
      if (old != null) {
        if (old.getParent() instanceof BaseMapLayerGroup) {
          old.setVisible(false);
        }
        Property.removeListener(old, this);

      }
      this.layer = layer;
      if (layer != null) {
        Property.addListener(layer, this);
        if (layer.getParent() instanceof BaseMapLayerGroup) {
          layer.setVisible(true);
        }
      }
      redraw();
      firePropertyChange("layer", old, layer);
    }
  }

  @Override
  public void setShowAreaBoundingBox(final boolean showAreaBoundingBox) {
    this.showAreaBoundingBox = showAreaBoundingBox;
    redraw();
  }
}
