package com.revolsys.swing.map.layer.record.renderer;

import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.TopologyException;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.view.ViewRenderer;

public abstract class AbstractGeometryRecordLayerRenderer extends AbstractRecordLayerRenderer {

  public AbstractGeometryRecordLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  public AbstractGeometryRecordLayerRenderer(final String type, final String name,
    final AbstractRecordLayer layer, final LayerRenderer<?> parent) {
    super(type, name, layer, parent);
  }

  protected abstract void renderRecord(final ViewRenderer view, final AbstractRecordLayer layer,
    final LayerRecord record, Geometry geometry);

  @Override
  protected final void renderRecords(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    if (isVisible(view)) {
      renderRecordsDo(view, layer, records);
    }
  }

  protected void renderRecordsDo(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    for (final LayerRecord record : view.cancellable(records)) {
      if (isFilterAccept(record)) {
        try {
          final Geometry geometry = record.getGeometry();
          renderRecord(view, layer, record, geometry);
        } catch (final TopologyException e) {
        } catch (final Throwable e) {
          if (!view.isCancelled()) {
            Logs.error(this, "Unabled to render " + layer.getName() + " #" + record.getIdentifier(),
              e);
          }
        }
      }
    }
  }

  @Override
  protected void renderSelectedRecordsDo(final ViewRenderer view, final AbstractRecordLayer layer,
    final List<LayerRecord> records) {
    renderRecordsDo(view, layer, records);
  }
}
