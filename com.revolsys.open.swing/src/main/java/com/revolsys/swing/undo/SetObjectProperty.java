package com.revolsys.swing.undo;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.Property;

public class SetObjectProperty extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final Object object;

  private final String propertyName;

  private final Object oldValue;

  private final Object newValue;

  public SetObjectProperty(final Object object, final String propertyName,
    final Object oldValue, final Object newValue) {
    this.object = object;
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Object value = Property.get(this.object, this.propertyName);
      if (EqualsRegistry.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value = Property.get(this.object, this.propertyName);
      if (EqualsRegistry.equal(value, this.newValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    Property.set(this.object, this.propertyName, this.newValue);
  }

  @Override
  protected void doUndo() {
    Property.set(this.object, this.propertyName, this.oldValue);
  }

  @Override
  public String toString() {
    return this.propertyName + " old=" + this.oldValue + ", new="
      + this.newValue;
  }
}
