package com.revolsys.swing.undo;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.property.DirectionalAttributes;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.jts.geom.Geometry;

public class ReverseDataObjectAttributesUndo extends AbstractUndoableEdit {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private final DataObject object;

  private final Geometry oldValue;

  public ReverseDataObjectAttributesUndo(final DataObject object) {
    this.object = object;
    this.oldValue = object.getGeometryValue();
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Geometry value = this.object.getGeometryValue();
      if (EqualsRegistry.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Geometry value = this.object.getGeometryValue();
      if (EqualsRegistry.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(this.object);
    property.reverseAttributes(this.object);
  }

  @Override
  protected void doUndo() {
    final DirectionalAttributes property = DirectionalAttributes.getProperty(this.object);
    property.reverseAttributes(this.object);
  }

  @Override
  public String toString() {
    return "Reverse attributes & geometry";
  }
}
