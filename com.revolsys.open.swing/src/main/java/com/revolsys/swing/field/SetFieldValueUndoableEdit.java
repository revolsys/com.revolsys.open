package com.revolsys.swing.field;

import javax.swing.JComponent;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.UndoManager;

@SuppressWarnings("serial")
public class SetFieldValueUndoableEdit extends AbstractUndoableEdit {

  public static SetFieldValueUndoableEdit create(final UndoManager undoManager,
    final Field field, final Object oldValue, final Object newValue) {
    if (undoManager == null) {
      return null;
    } else {
      final SetFieldValueUndoableEdit edit = new SetFieldValueUndoableEdit(
        field, oldValue, newValue);
      undoManager.addEdit(edit);
      return edit;
    }
  }

  private final Field field;

  private final Object oldValue;

  private final Object newValue;

  public SetFieldValueUndoableEdit(final Field field, final Object oldValue,
    final Object newValue) {
    this.field = field;
    this.oldValue = oldValue;
    this.newValue = newValue;
    setHasBeenDone(true);
  }

  @Override
  public boolean canRedo() {
    if (super.canRedo()) {
      final Object value = field.getFieldValue();
      if (EqualsRegistry.equal(value, oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value = field.getFieldValue();
      if (EqualsRegistry.equal(value, newValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void doRedo() {
    field.setFieldValue(newValue);
    ((JComponent)field).requestFocusInWindow();
  }

  @Override
  public void doUndo() {
    field.setFieldValue(oldValue);
    ((JComponent)field).requestFocusInWindow();
  }

  @Override
  public String toString() {
    return field.getFieldName() + " old=" + oldValue + ", new=" + newValue;
  }
}
