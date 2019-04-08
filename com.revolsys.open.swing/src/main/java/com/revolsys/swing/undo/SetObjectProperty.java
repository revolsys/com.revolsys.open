package com.revolsys.swing.undo;

import org.jeometry.common.data.type.DataType;

import com.revolsys.util.Property;

public class SetObjectProperty extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  private final Object newValue;

  private final Object object;

  private final Object oldValue;

  private final String propertyName;

  public SetObjectProperty(final Object object, final String propertyName, final Object oldValue,
    final Object newValue) {
    this.object = object;
    this.propertyName = propertyName;
    this.oldValue = oldValue;
    this.newValue = newValue;
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Object value = Property.get(this.object, this.propertyName);
      if (DataType.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value = Property.get(this.object, this.propertyName);
      if (DataType.equal(value, this.newValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void redoDo() {
    Property.setSimple(this.object, this.propertyName, this.newValue);
  }

  @Override
  public String toString() {
    return this.propertyName + " old=" + this.oldValue + ", new=" + this.newValue;
  }

  @Override
  protected void undoDo() {
    Property.setSimple(this.object, this.propertyName, this.oldValue);
  }
}
