package com.revolsys.swing.undo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.undo.UndoableEdit;

@SuppressWarnings("serial")
public class MultipleUndo extends AbstractUndoableEdit {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final List<UndoableEdit> edits;

  public MultipleUndo() {
    this(new ArrayList<UndoableEdit>());
  }

  public MultipleUndo(final List<UndoableEdit> edits) {
    this.edits = new ArrayList<UndoableEdit>(edits);
  }

  public MultipleUndo(final UndoableEdit... edits) {
    this(Arrays.asList(edits));
  }

  @Override
  public boolean addEdit(final UndoableEdit edit) {
    if (edit == null || isHasBeenDone()) {
      return false;
    } else {
      this.edits.add(edit);
      return true;
    }
  }

  @Override
  public boolean canRedo() {
    for (final UndoableEdit edit : this.edits) {
      if (!edit.canRedo()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean canUndo() {
    for (final UndoableEdit edit : this.edits) {
      if (!edit.canUndo()) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected void doRedo() {
    for (final UndoableEdit edit : this.edits) {
      edit.redo();
    }
  }

  @Override
  protected void doUndo() {
    for (final UndoableEdit edit : this.edits) {
      edit.undo();
    }
  }

  public boolean isEmpty() {
    return this.edits.isEmpty();
  }
}
