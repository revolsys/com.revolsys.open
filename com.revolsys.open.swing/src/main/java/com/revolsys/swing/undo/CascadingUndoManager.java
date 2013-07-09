package com.revolsys.swing.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

@SuppressWarnings("serial")
public class CascadingUndoManager extends UndoManager {

  private UndoManager parent;

  public CascadingUndoManager() {
  }

  public CascadingUndoManager(final UndoManager parent) {
    this.parent = parent;
  }

  @Override
  public synchronized boolean addEdit(final UndoableEdit edit) {
    final boolean addEdit = super.addEdit(edit);
    return addEdit;
  }

  @Override
  public synchronized boolean canRedo() {
    if (super.canRedo()) {
      return true;
    } else if (parent == null) {
      return false;
    } else {
      return parent.canRedo();
    }
  }

  @Override
  public synchronized boolean canUndo() {
    if (super.canUndo()) {
      return true;
    } else if (parent == null) {
      return false;
    } else {
      return parent.canUndo();
    }
  }

  @Override
  public synchronized boolean canUndoOrRedo() {
    if (super.canUndoOrRedo()) {
      return true;
    } else if (parent == null) {
      return false;
    } else {
      return parent.canUndoOrRedo();
    }
  }

  public UndoManager getParent() {
    return parent;
  }

  @Override
  public synchronized void redo() throws CannotRedoException {
    if (super.canRedo()) {
      super.redo();
    } else if (parent != null && parent.canRedo()) {
      parent.redo();
    }
  }

  public void setParent(final UndoManager parent) {
    this.parent = parent;
  }

  @Override
  public synchronized void undo() throws CannotUndoException {
    if (super.canUndo()) {
      super.undo();
    } else if (parent != null && parent.canUndo()) {
      parent.undo();
    }
  }
}
