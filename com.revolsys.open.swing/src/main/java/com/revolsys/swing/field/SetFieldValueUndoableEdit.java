package com.revolsys.swing.field;

import javax.swing.JComponent;

import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.undo.AbstractUndoableEdit;
import com.revolsys.swing.undo.UndoManager;

public class SetFieldValueUndoableEdit extends AbstractUndoableEdit {
  private static final long serialVersionUID = 1L;

  public static SetFieldValueUndoableEdit newUndoableEdit(final UndoManager undoManager,
    final Field field, final Object oldValue, final Object newValue) {
    if (undoManager == null) {
      return null;
    } else {
      final SetFieldValueUndoableEdit edit = new SetFieldValueUndoableEdit(field, oldValue,
        newValue);
      undoManager.addEdit(edit);
      return edit;
    }
  }

  private final Field field;

  private final Object newValue;

  private final Object oldValue;

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
      final Object value = this.field.getFieldValue();
      if (DataType.equal(value, this.oldValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canUndo() {
    if (super.canUndo()) {
      final Object value = this.field.getFieldValue();
      if (DataType.equal(value, this.newValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void redoDo() {
    this.field.setFieldValue(this.newValue);
    ((JComponent)this.field).requestFocusInWindow();
  }

  @Override
  public String toString() {
    return this.field.getFieldName() + " old=" + this.oldValue + ", new=" + this.newValue;
  }

  @Override
  public void undoDo() {
    this.field.setFieldValue(this.oldValue);
    ((JComponent)this.field).requestFocusInWindow();
  }
}
