package com.revolsys.swing.map.layer.record.renderer;

import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.view.ViewRenderer;

/**
 * Use the first renderer which is visible at the current scale, ignore all
 * others. Changes when the scale changes.
 */
public class ScaleMultipleRenderer extends AbstractMultipleRecordLayerRenderer {
  private static final Icon ICON = Icons.getIcon("style_scale");

  private transient long lastScale = 0;

  private transient AbstractRecordLayerRenderer renderer;

  private ScaleMultipleRenderer() {
    super("scaleStyle", "Scale Styles", ICON);
  }

  public ScaleMultipleRenderer(final LayerRenderer<?> parent) {
    this();
    setParent(parent);
  }

  public ScaleMultipleRenderer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  @Override
  public ScaleMultipleRenderer clone() {
    final ScaleMultipleRenderer clone = (ScaleMultipleRenderer)super.clone();
    clone.lastScale = 0;
    clone.renderer = null;
    return clone;
  }

  private AbstractRecordLayerRenderer getRenderer(final ViewRenderer view) {
    final long scaleForVisible = (long)view.getScaleForVisible();
    if (scaleForVisible == this.lastScale && this.renderer != null) {
      if (this.renderer.isVisible(scaleForVisible)) {
        return this.renderer;
      } else {
        return null;
      }
    } else {
      this.lastScale = scaleForVisible;
      for (final AbstractRecordLayerRenderer renderer : this.renderers) {
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
  protected void renderMultipleRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    final AbstractRecordLayerRenderer renderer = getRenderer(view);
    if (renderer != null) {
      renderer.renderRecords(view, layer, records);
    }
  }

  @Override
  protected void renderMultipleSelectedRecords(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final AbstractRecordLayerRenderer renderer = getRenderer(view);
    if (renderer != null) {
      renderer.renderSelectedRecords(view, layer, records);
    }
  }
}
