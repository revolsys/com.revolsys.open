package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;

import com.revolsys.collection.map.Maps;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.logging.Logs;
import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.view.ViewRenderer;

/**
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = Icons.getIcon("style_filter");

  public FilterMultipleRenderer(final AbstractRecordLayer layer) {
    this(layer, null);
  }

  public FilterMultipleRenderer(final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super("filterStyle", layer, parent);
    setIcon(ICON);
  }

  public FilterMultipleRenderer(final Map<String, ? extends Object> properties) {
    super("filterStyle", "Filter Styles");
    setIcon(ICON);
    setProperties(properties);
  }

  protected AbstractRecordLayerRenderer getRenderer(final AbstractRecordLayer layer,
    final List<AbstractRecordLayerRenderer> renderers, final LayerRecord record,
    final double scaleForVisible) {

    for (final AbstractRecordLayerRenderer renderer : renderers) {
      if (renderer.isFilterAccept(record)) {
        if (renderer.isVisible(record) && !layer.isHidden(record)) {
          if (renderer.isVisible(scaleForVisible)) {
            return renderer;
          }
        }
        return null;
      }
    }
    return null;
  }

  @Override
  public void renderRecord(final ViewRenderer viewport, final BoundingBox visibleArea,
    final AbstractRecordLayer layer, final LayerRecord record) {
    final Map<AbstractRecordLayerRenderer, List<LayerRecord>> rendererToRecordMap = new LinkedHashMap<>();
    final double scaleForVisible = viewport.getScaleForVisible();
    if (isVisible(scaleForVisible)) {
      final List<AbstractRecordLayerRenderer> renderers = new ArrayList<>(getRenderers());
      for (final AbstractRecordLayerRenderer renderer : renderers) {
        rendererToRecordMap.put(renderer, new ArrayList<>());
      }
      if (isFilterAccept(record) && !layer.isHidden(record)) {
        final AbstractRecordLayerRenderer renderer = getRenderer(layer, renderers, record,
          scaleForVisible);
        if (renderer != null) {
          try {
            renderer.renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            if (!viewport.isCancelled()) {
              Logs.error(this,
                "Unabled to render " + layer.getName() + " #" + record.getIdentifier(), e);
            }
          }
        }
      }

    }
  }

  @Override
  protected void renderRecords(final ViewRenderer viewport,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    final Map<AbstractRecordLayerRenderer, List<LayerRecord>> rendererToRecordMap = new LinkedHashMap<>();
    final BoundingBox visibleArea = viewport.getBoundingBox();
    final double scaleForVisible = viewport.getScaleForVisible();
    if (isVisible(scaleForVisible)) {
      final List<AbstractRecordLayerRenderer> renderers = new ArrayList<>(getRenderers());
      for (final AbstractRecordLayerRenderer renderer : renderers) {
        rendererToRecordMap.put(renderer, new ArrayList<>());
      }
      for (final LayerRecord record : viewport.cancellable(records)) {
        if (isFilterAccept(record) && !layer.isHidden(record)) {
          final AbstractRecordLayerRenderer renderer = getRenderer(layer, renderers, record,
            scaleForVisible);
          if (renderer != null) {
            Maps.addToList(rendererToRecordMap, renderer, record);
          }
        }
      }
      for (final Entry<AbstractRecordLayerRenderer, List<LayerRecord>> entry : viewport
        .cancellable(rendererToRecordMap.entrySet())) {
        final AbstractRecordLayerRenderer renderer = entry.getKey();
        final List<LayerRecord> rendererRecords = entry.getValue();
        for (final LayerRecord record : viewport.cancellable(rendererRecords)) {
          try {
            renderer.renderRecord(viewport, visibleArea, layer, record);
          } catch (final TopologyException e) {
          } catch (final Throwable e) {
            if (!viewport.isCancelled()) {
              Logs.error(this,
                "Unabled to render " + layer.getName() + " #" + record.getIdentifier(), e);
            }
          }
        }

      }
    }
  }

  @Override
  public void renderSelectedRecord(final ViewRenderer viewport,
    final AbstractRecordLayer layer, final LayerRecord record) {
    if (isVisible(record)) {
      final double scaleForVisible = viewport.getScaleForVisible();
      for (final AbstractRecordLayerRenderer renderer : getRenderers()) {
        if (renderer.isFilterAccept(record)) {
          if (renderer.isVisible(record)) {
            if (renderer.isVisible(scaleForVisible)) {
              try {
                renderer.renderSelectedRecord(viewport, layer, record);
              } catch (final Throwable e) {
                Logs.error(this,
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
