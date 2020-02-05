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

  public static MultipleRecordRenderer create(final AbstractRecordLayer layer,
    final AbstractRecordLayerRenderer... renderers) {
    return new MultipleRecordRenderer(layer, Arrays.asList(renderers));
  }

  public MultipleRecordRenderer() {
    super("multipleStyle", "Multiple Styles", ICON);
  }

  public MultipleRecordRenderer(final AbstractRecordLayer layer) {
    this();
    setLayer(layer);
  }

  public MultipleRecordRenderer(final AbstractRecordLayer layer,
    final List<? extends AbstractRecordLayerRenderer> renderers) {
    this();
    setLayer(layer);
    setRenderers(renderers);
  }

  public MultipleRecordRenderer(final LayerRenderer<?> parent) {
    this();
    setParent(parent);
  }

  public MultipleRecordRenderer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRecordLayerRenderer renderer = new GeometryStyleRecordLayerRenderer(this,
      style);
    addRenderer(renderer);
  }

  @Override
  protected void renderMultipleRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    for (final AbstractRecordLayerRenderer renderer : this.renderers) {
      if (view.isCancelled()) {
        return;
      }
      renderer.renderRecords(view, layer, records);
    }
  }

  @Override
  protected void renderMultipleSelectedRecords(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    for (final AbstractRecordLayerRenderer renderer : this.renderers) {
      if (view.isCancelled()) {
        return;
      }
      renderer.renderSelectedRecords(view, layer, records);
    }
  }
}
