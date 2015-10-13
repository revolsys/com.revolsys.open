package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import com.revolsys.collection.map.Maps;
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
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = Icons.getIcon("style_filter");

  public FilterMultipleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public FilterMultipleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    super("filterStyle", layer, parent, style);
    setIcon(ICON);
  }

  protected AbstractRecordLayerRenderer getRenderer(final AbstractRecordLayer layer,
    final List<AbstractRecordLayerRenderer> renderers, final LayerRecord record,
    final double scale) {
    for (final AbstractRecordLayerRenderer renderer : renderers) {
      if (renderer.isFilterAccept(record)) {
        if (renderer.isVisible(record) && !layer.isHidden(record)) {
          if (renderer.isVisible(scale)) {
            return renderer;
          }
        }
        return null;
      }
    }
    return null;
  }

  @Override
  public void renderRecord(final Viewport2D viewport, final BoundingBox visibleArea,
    final AbstractLayer layer, final LayerRecord record) {
  }

  @Override
  protected void renderRecords(final Viewport2D viewport, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    final Map<AbstractRecordLayerRenderer, List<LayerRecord>> rendererToRecordMap = new LinkedHashMap<>();
    final BoundingBox visibleArea = viewport.getBoundingBox();
    final double scale = viewport.getScale();
    if (isVisible(scale)) {
      final List<AbstractRecordLayerRenderer> renderers = new ArrayList<>(getRenderers());
      for (final AbstractRecordLayerRenderer renderer : renderers) {
        rendererToRecordMap.put(renderer, new ArrayList<LayerRecord>());
      }
      for (final LayerRecord record : records) {
        if (isFilterAccept(record) && !layer.isHidden(record)) {
          final AbstractRecordLayerRenderer renderer = getRenderer(layer, renderers, record, scale);
          if (renderer != null) {
            Maps.addToList(rendererToRecordMap, renderer, record);
          }
        }
      }
      for (final Entry<AbstractRecordLayerRenderer, List<LayerRecord>> entry : rendererToRecordMap
        .entrySet()) {
        final AbstractRecordLayerRenderer renderer = entry.getKey();
        final List<LayerRecord> rendererRecords = entry.getValue();
        for (final LayerRecord record : rendererRecords) {
          try {
            renderer.renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            Exceptions.log(getClass(),
              "Unabled to render " + layer.getName() + " #" + record.getIdentifier(), e);
          }
        }

      }
    }
  }

  @Override
  public void renderSelectedRecord(final Viewport2D viewport, final AbstractLayer layer,
    final LayerRecord record) {
    if (isVisible(record)) {
      final double scale = viewport.getScale();
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        if (renderer.isFilterAccept(record)) {
          if (renderer.isVisible(record)) {
            if (renderer.isVisible(scale)) {
              try {
                renderer.renderSelectedRecord(viewport, layer, record);
              } catch (final Throwable e) {
                Exceptions.log(getClass(),
                  "Unabled to render " + layer.getName() + " #" + record.getIdentifier(), e);
              }
            }
          }
          // Only render using the first match
          return;
        }
      }
    }
  }
}
