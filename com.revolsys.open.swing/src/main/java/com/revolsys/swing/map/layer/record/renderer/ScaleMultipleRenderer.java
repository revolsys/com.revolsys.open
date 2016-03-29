package com.revolsys.swing.map.layer.record.renderer;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Exceptions;

/**
 * Use the first renderer which is visible at the current scale, ignore all
 * others. Changes when the scale changes.
 */
public class ScaleMultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = Icons.getIcon("style_scale");

  private transient long lastScale = 0;

  private transient AbstractRecordLayerRenderer renderer;

  public ScaleMultipleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super("scaleStyle", layer, parent);
    setIcon(ICON);
  }

  public ScaleMultipleRenderer(final Map<String, ? extends Object> properties) {
    super("scaleStyle", "Scale Styles");
    setIcon(ICON);
    setProperties(properties);
  }

  @Override
  public ScaleMultipleRenderer clone() {
    final ScaleMultipleRenderer clone = (ScaleMultipleRenderer)super.clone();
    clone.lastScale = 0;
    clone.renderer = null;
    return clone;
  }

  private AbstractRecordLayerRenderer getRenderer(final Viewport2D viewport) {
    final long scaleForVisible = (long)viewport.getScaleForVisible();
    if (scaleForVisible == this.lastScale && this.renderer != null) {
      if (this.renderer.isVisible(scaleForVisible)) {
        return this.renderer;
      } else {
        return null;
      }
    } else {
      this.lastScale = scaleForVisible;
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(scaleForVisible)) {
          this.renderer = renderer;
          return renderer;
        }
      }
      return null;
    }
  }

  @Override
  public boolean isVisible(final LayerRecord object) {
    if (super.isVisible() && super.isVisible(object)) {
      if (this.renderer != null) {
        return this.renderer.isVisible(object);
      }
    }
    return false;
  }

  @Override
  public void render(final Viewport2D viewport, final AbstractRecordLayer layer) {
    if (layer.hasGeometryField()) {
      final AbstractRecordLayerRenderer renderer = getRenderer(viewport);
      if (renderer != null) {
        renderer.render(viewport, layer);
      }
    }
  }

  // NOTE: Needed for filter styles
  @Override
  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord object) {
    final AbstractRecordLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      if (isVisible(object)) {
        try {
          renderer.renderRecord(viewport, visibleArea, layer, object);
        } catch (final TopologyException e) {
        } catch (final Throwable e) {
          Exceptions.error(getClass(),
            "Unabled to render " + layer.getName() + " #" + object.getIdentifier(), e);
        }
      }
    }
  }

  @Override
  // NOTE: Needed for multiple styles
  protected void renderRecords(final Viewport2D viewport, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    final BoundingBox visibleArea = viewport.getBoundingBox();
    final AbstractRecordLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      for (final LayerRecord record : records) {
        if (isVisible(record)) {
          try {
            renderer.renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          }
        }
      }
    }
  }

  @Override
  public void renderSelectedRecord(final Viewport2D viewport, final AbstractLayer layer,
    final LayerRecord object) {
    final AbstractRecordLayerRenderer renderer = getRenderer(viewport);
    if (renderer != null) {
      if (isVisible(object)) {
        try {
          renderer.renderSelectedRecord(viewport, layer, object);
        } catch (final Throwable e) {
          Exceptions.error(getClass(),
            "Unabled to render " + layer.getName() + " #" + object.getIdentifier(), e);
        }
      }
    }
  }
}
