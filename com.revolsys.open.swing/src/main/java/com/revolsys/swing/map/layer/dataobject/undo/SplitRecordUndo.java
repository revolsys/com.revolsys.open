package com.revolsys.swing.map.layer.dataobject.undo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.model.data.equals.DataObjectEquals;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.overlay.CloseLocation;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.vividsolutions.jts.geom.Geometry;

public class SplitRecordUndo extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  private final LayerDataObject record1;

  private final CloseLocation splitLocation;

  private Geometry geometry;

  private AbstractDataObjectLayer layer;

  private Map<String, Object> values;

  private final Map<String, Object> values1 = new HashMap<String, Object>();

  private final Map<String, Object> values2 = new HashMap<String, Object>();

  private LayerDataObject record2;

  public SplitRecordUndo(final LayerDataObject record,
    final CloseLocation splitLocation) {
    this.record1 = record;
    this.splitLocation = splitLocation;
    if (record1 != null) {
      this.layer = record1.getLayer();
      this.geometry = record1.getGeometryValue();
      this.values = new HashMap<String, Object>(record1);
    }
  }

  @Override
  public boolean canRedo() {
    if (layer != null) {
      if (record1 != null) {
        if (splitLocation != null) {
          if (geometry != null) {
            if (EqualsRegistry.equal(record1.getGeometryValue(), geometry)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (record1 != null) {
      if (record2 != null) {
        if (DataObjectEquals.equalAttributes(record1, values1)) {
          if (DataObjectEquals.equalAttributes(record2, values2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    final List<LayerDataObject> splitRecords = layer.splitRecord(record1,
      splitLocation);
    if (splitRecords.size() == 2) {
      values1.putAll(record1);
      record2 = splitRecords.get(1);
      values2.putAll(record2);
    }
  }

  @Override
  protected void doUndo() {
    record1.setValues(values);
    layer.deleteRecords(record2);
    record2 = null;
    values1.clear();
    values2.clear();
  }
}
