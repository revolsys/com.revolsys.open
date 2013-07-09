package com.revolsys.swing.undo;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.util.JavaBeanUtil;

@SuppressWarnings("serial")
public class SetObjectProperty extends AbstractUndoableEdit {

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
      final Object value = JavaBeanUtil.getValue(object, propertyName);
      if (EqualsRegistry.equal(value, oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value = JavaBeanUtil.getValue(object, propertyName);
      if (EqualsRegistry.equal(value, newValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void doRedo() {
    JavaBeanUtil.setValue(object, propertyName, newValue);
  }

  @Override
  protected void doUndo() {
    JavaBeanUtil.setValue(object, propertyName, oldValue);
  }

  @Override
  public String toString() {
    return propertyName + " old=" + oldValue + ", new=" + newValue;
  }
}
