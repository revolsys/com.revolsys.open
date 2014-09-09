package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.ExceptionUtil;

/**
 * Use all the specified renderers to render the layer. All features are
 * rendered using the first renderer, then the second etc.
 */
public class MultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = Icons.getIcon("style_multiple");

  public MultipleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public MultipleRenderer(final AbstractRecordLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> multipleStyle) {
    super("multipleStyle", layer, parent, multipleStyle);
    setIcon(ICON);
  }

  public void addStyle(final GeometryStyle style) {
    final GeometryStyleRenderer renderer = new GeometryStyleRenderer(
      getLayer(), this, style);
    addRenderer(renderer);
  }

  // Needed for filter styles
  @Override
  public void renderRecord(final Viewport2D viewport,
    final BoundingBox visibleArea, final AbstractLayer layer,
    final LayerRecord record) {
    if (isVisible(record)) {
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        final long scale = (long)viewport.getScale();
        if (renderer.isVisible(scale)) {
          try {
            renderer.renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            ExceptionUtil.log(
              getClass(),
              "Unabled to render " + layer.getName() + " #"
                  + record.getIdentifier(), e);
          }
        }
      }
    }
  }

  @Override
  protected void renderRecords(final Viewport2D viewport,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final List<LayerRecord> visibleRecords = new ArrayList<>();
    for (final LayerRecord record : records) {
      if (isVisible(record) && !layer.isHidden(record)) {
        visibleRecords.add(record);
      }
    }

    for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
      final long scale = (long)viewport.getScale();
      if (renderer.isVisible(scale)) {
        try {
          renderer.renderRecords(viewport, layer, visibleRecords);
        } catch (final TopologyException e) {
        }
      }
    }
  }

  @Override
  public void renderSelectedRecord(final Viewport2D viewport,
    final AbstractLayer layer, final LayerRecord object) {
    if (isVisible(object)) {
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        final long scale = (long)viewport.getScale();
        if (renderer.isVisible(scale)) {
          try {
            renderer.renderSelectedRecord(viewport, layer, object);
          } catch (final Throwable e) {
            ExceptionUtil.log(
              getClass(),
              "Unabled to render " + layer.getName() + " #"
                  + object.getIdentifier(), e);
          }
        }
      }
    }
  }
}
