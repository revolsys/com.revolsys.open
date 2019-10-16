package com.revolsys.swing.map.layer.record.renderer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.view.ViewRenderer;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRecordRenderer extends AbstractMultipleRecordLayerRenderer {
  private static final Icon ICON = Icons.getIcon("style_multiple");

  public MultipleRecordRenderer(final AbstractRecordLayer layer) {
    super("multipleStyle", "Multiple Styles");
    setIcon(ICON);
  }

  public MultipleRecordRenderer(final AbstractRecordLayer layer,
    final AbstractRecordLayerRenderer... renderers) {
    this(layer, Arrays.asList(renderers));
  }

  public MultipleRecordRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super("multipleStyle", layer, parent);
    setIcon(ICON);
  }

  public MultipleRecordRenderer(final AbstractRecordLayer layer,
    final List<? extends AbstractRecordLayerRenderer> renderers) {
    super("multipleStyle", "Multiple Styles");
    setIcon(ICON);
    setRenderers(renderers);
  }

  public MultipleRecordRenderer(final Map<String, ? extends Object> properties) {
    super("multipleStyle", "Multiple Styles");
    setIcon(ICON);
    setProperties(properties);
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRecordLayerRenderer renderer = new GeometryStyleRecordLayerRenderer(
      getLayer(), this, style);
    addRenderer(renderer);
  }

  @Override
  protected void renderMultipleRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    for (int i = 0; i < this.renderers.size() && !view.isCancelled(); i++) {
      AbstractRecordLayerRenderer renderer;
      try {
        renderer = this.renderers.get(i);
      } catch (final ArrayIndexOutOfBoundsException e) {
        return;
      }
      renderer.renderRecords(view, layer, records);
    }
  }

  @Override
  protected void renderMultipleSelectedRecords(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    for (int i = 0; i < this.renderers.size() && !view.isCancelled(); i++) {
      AbstractRecordLayerRenderer renderer;
      try {
        renderer = this.renderers.get(i);
      } catch (final ArrayIndexOutOfBoundsException e) {
        return;
      }
      renderer.renderSelectedRecords(view, layer, records);
    }
  }
}
