package com.revolsys.swing.undo;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.vividsolutions.jts.geom.Geometry;

public class ReverseDataObjectGeometryUndo extends AbstractUndoableEdit {

  private final DataObject object;

  private final Geometry oldValue;

  public ReverseDataObjectGeometryUndo(final DataObject object) {
    this.object = object;
    this.oldValue = object.getGeometryValue();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Geometry value = object.getGeometryValue();
      if (EqualsRegistry.equal(value, oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Geometry value = object.getGeometryValue();
      if (EqualsRegistry.equal(value.reverse(), oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(object);
    property.reverseGeometry(object);
  }

  @Override
  protected void doUndo() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(object);
    property.reverseGeometry(object);
  }

  @Override
  public String toString() {
    return "Reverse geometry";
  }
}
