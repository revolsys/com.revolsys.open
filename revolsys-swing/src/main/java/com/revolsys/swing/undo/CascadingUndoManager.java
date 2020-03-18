package com.revolsys.swing.undo;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class CascadingUndoManager extends UndoManager {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private UndoManager parent;

  public CascadingUndoManager() {
  }

  public CascadingUndoManager(final UndoManager parent) {
    this.parent = parent;
  }

  @Override
  public synchronized boolean canRedo() {
    if (super.canRedo()) {
      return true;
    } else if (this.parent == null) {
      return false;
    } else {
      return this.parent.canRedo();
    }
  }

  @Override
  public synchronized boolean canUndo() {
    if (super.canUndo()) {
      return true;
    } else if (this.parent == null) {
      return false;
    } else {
      return this.parent.canUndo();
    }
  }

  @Override
  public synchronized boolean canUndoOrRedo() {
    if (super.canUndoOrRedo()) {
      return true;
    } else if (this.parent == null) {
      return false;
    } else {
      return this.parent.canUndoOrRedo();
    }
  }

  public UndoManager getParent() {
    return this.parent;
  }

  @Override
  public boolean isEventsEnabled() {
    return super.isEventsEnabled() && (this.parent == null || this.parent.isEventsEnabled());
  }

  @Override
  public synchronized void redo() throws CannotRedoException {
    if (super.canRedo()) {
      super.redo();
    } else if (this.parent != null && this.parent.canRedo()) {
      this.parent.redo();
    }
  }

  public void setParent(final UndoManager parent) {
    this.parent = parent;
  }

  @Override
  public synchronized void undo() throws CannotUndoException {
    if (super.canUndo()) {
      super.undo();
    } else if (this.parent != null && this.parent.canUndo()) {
      this.parent.undo();
    }
  }
}
