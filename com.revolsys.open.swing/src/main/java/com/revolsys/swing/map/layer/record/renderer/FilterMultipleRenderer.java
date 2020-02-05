package com.revolsys.swing.map.layer.record.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.swing.Icon;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.view.ViewRenderer;

/**
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRecordLayerRenderer {

  private static final Icon ICON = Icons.getIcon("style_filter");

  public FilterMultipleRenderer() {
    super("filterStyle", "Filter Styles", ICON);
  }

  public FilterMultipleRenderer(final AbstractRecordLayer layer) {
    this();
    setLayer(layer);
  }

  public FilterMultipleRenderer(final LayerRenderer<?> parent) {
    this();
    setParent(parent);
  }

  public FilterMultipleRenderer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }

  private void addRecord(final List<List<LayerRecord>> rendererRecordsLists,
    final LayerRecord record) {
    int i = 0;
    for (final AbstractRecordLayerRenderer renderer : this.renderers) {
      if (renderer.isFilterAccept(record)) {
        final List<LayerRecord> rendererRecords = rendererRecordsLists.get(i);
        if (rendererRecords != null) {
          rendererRecords.add(record);
        }
        return;
      }
      i++;
    }
  }

  private void filterRendererRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records,
    final BiConsumer<AbstractRecordLayerRenderer, List<LayerRecord>> action) {
    final double scaleForVisible = view.getScaleForVisible();
    final List<List<LayerRecord>> rendererRecordsLists = new ArrayList<>();
    for (final AbstractRecordLayerRenderer renderer : this.renderers) {
      if (renderer.isVisible(scaleForVisible)) {
        rendererRecordsLists.add(new ArrayList<>());
      } else {
        rendererRecordsLists.add(null);
      }
    }
    for (final LayerRecord record : view.cancellable(records)) {
      addRecord(rendererRecordsLists, record);
    }
    int i = 0;
    for (final AbstractRecordLayerRenderer renderer : this.renderers) {
      final List<LayerRecord> rendererRecords = rendererRecordsLists.get(i);
      if (rendererRecords != null && !rendererRecords.isEmpty()) {
        action.accept(renderer, rendererRecords);
      }
      i++;
    }
  }

  @Override
  protected void renderMultipleRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    filterRendererRecords(view, layer, records, (renderer, rendererRecords) -> {
      renderer.renderRecords(view, layer, rendererRecords);
    });
  }

  @Override
  protected void renderMultipleSelectedRecords(final ViewRenderer view,
    final AbstractRecordLayer layer, final List<LayerRecord> records) {
    filterRendererRecords(view, layer, records, (renderer, rendererRecords) -> {
      renderer.renderSelectedRecords(view, layer, rendererRecords);
    });
  }
}
