package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.logging.Logs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.Cancellable;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRecordRenderer extends AbstractMultipleRenderer {
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
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(getLayer(), this, style);
    addRenderer(renderer);
  }

  // Needed for filter styles
  @Override
  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord record) {
    if (isVisible(record)) {
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        final long scaleForVisible = (long)viewport.getScaleForVisible();
        if (renderer.isVisible(scaleForVisible)) {
          try {
            renderer.renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            Logs.error(this, "Unabled to render " + layer.getName() + " #" + record.getIdentifier(),
              e);
          }
        }
      }
    }
  }

  @Override
  protected void renderRecords(final Viewport2D viewport, final Cancellable cancellable,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final List<LayerRecord> visibleRecords = new ArrayList<>();
    for (final LayerRecord record : cancellable.cancellable(records)) {
      if (isVisible(record) && !layer.isHidden(record)) {
        visibleRecords.add(record);
      }
    }

    for (final AbstractRecordLayerRenderer renderer : cancellable.cancellable(getRenderers())) {
      final long scaleForVisible = (long)viewport.getScaleForVisible();
      if (renderer.isVisible(scaleForVisible)) {
        try {
          renderer.renderRecords(viewport, cancellable, layer, visibleRecords);
        } catch (final TopologyException e) {
        }
      }
    }
  }

  @Override
  public void renderSelectedRecord(final Viewport2D viewport, final AbstractLayer layer,
    final LayerRecord record) {
    if (isVisible(record)) {
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        final long scaleForVisible = (long)viewport.getScaleForVisible();
        if (renderer.isVisible(scaleForVisible)) {
          try {
            renderer.renderSelectedRecord(viewport, layer, record);
          } catch (final Throwable e) {
            Logs.error(this, "Unabled to render " + layer.getName() + " #" + record.getIdentifier(),
              e);
          }
        }
      }
    }
  }
}
