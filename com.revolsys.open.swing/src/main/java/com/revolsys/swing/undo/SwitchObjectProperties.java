package com.revolsys.swing.undo;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.util.Property;

public class SwitchObjectProperties extends AbstractUndoableEdit {

  private static final long serialVersionUID = 1L;

  private final Object object;

  private final String propertyName1;

  private final String propertyName2;

  private final Object value1;

  private final Object value2;

  public SwitchObjectProperties(final Object object,
    final String propertyName1, final Object value1,
    final String propertyName2, final Object value2) {
    this.object = object;
    this.propertyName1 = propertyName1;
    this.value1 = value1;
    this.propertyName2 = propertyName2;
    this.value2 = value2;
  }

  public SwitchObjectProperties(final Object object,
    final String propertyName1, final String propertyName2) {
    this.object = object;
    this.propertyName1 = propertyName1;
    this.propertyName2 = propertyName2;
    this.value1 = Property.get(object, propertyName1);
    this.value2 = Property.get(object, propertyName2);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Object value1 = Property.get(this.object, this.propertyName1);
      final Object value2 = Property.get(this.object, this.propertyName2);
      if (!EqualsRegistry.equal(value1, value2)) {
        if (EqualsRegistry.equal(this.value1, value1)) {
          if (EqualsRegistry.equal(this.value2, value2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value1 = Property.get(this.object, this.propertyName1);
      final Object value2 = Property.get(this.object, this.propertyName2);
      if (EqualsRegistry.equal(this.value1, value2)) {
        if (EqualsRegistry.equal(this.value2, value1)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    Property.set(this.object, this.propertyName1, this.value2);
    Property.set(this.object, this.propertyName2, this.value1);
  }

  @Override
  protected void doUndo() {
    Property.set(this.object, this.propertyName1, this.value1);
    Property.set(this.object, this.propertyName2, this.value2);
  }

  @Override
  public String toString() {
    return "switch " + this.propertyName1 + "=" + this.value1 + " and "
        + this.propertyName2 + "=" + this.value2;
  }
}
