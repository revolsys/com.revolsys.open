package com.revolsys.swing.undo;

import java.io.Serializable;

import javax.swing.UIManager;
import javax.swing.undo.UndoableEdit;

public abstract class AbstractUndoableEdit implements UndoableEdit, Serializable {
  private static final long serialVersionUID = 1L;

  private boolean alive = true;

  private boolean hasBeenDone = false;

  public AbstractUndoableEdit() {
  }

  @Override
  public boolean addEdit(final UndoableEdit anEdit) {
    return false;
  }

  @Override
  public boolean canRedo() {
    return this.alive && !this.hasBeenDone;
  }

  @Override
  public boolean canUndo() {
    return this.alive && this.hasBeenDone;
  }

  @Override
  public void die() {
    this.alive = false;
  }

  @Override
  public String getPresentationName() {
    return "";
  }

  @Override
  public String getRedoPresentationName() {
    String name = getPresentationName();
    if (!"".equals(name)) {
      name = UIManager.getString("AbstractUndoableEdit.redoText") + " " + name;
    } else {
      name = UIManager.getString("AbstractUndoableEdit.redoText");
    }

    return name;
  }

  @Override
  public String getUndoPresentationName() {
    String name = getPresentationName();
    if (!"".equals(name)) {
      name = UIManager.getString("AbstractUndoableEdit.undoText") + " " + name;
    } else {
      name = UIManager.getString("AbstractUndoableEdit.undoText");
    }

    return name;
  }

  public boolean isAlive() {
    return this.alive;
  }

  public boolean isHasBeenDone() {
    return this.hasBeenDone;
  }

  @Override
  public boolean isSignificant() {
    return true;
  }

  @Override
  public final void redo() throws CannotRedoException {
    if (!canRedo()) {
      final String message = toString();
      throw new CannotRedoException(message);
    }
    this.hasBeenDone = true;
    redoDo();
  }

  protected void redoDo() {
  }

  @Override
  public boolean replaceEdit(final UndoableEdit anEdit) {
    return false;
  }

  protected void setHasBeenDone(final boolean hasBeenDone) {
    this.hasBeenDone = hasBeenDone;
  }

  @Override
  public String toString() {
    return super.toString() + " hasBeenDone: " + this.hasBeenDone + " alive: " + this.alive;
  }

  @Override
  public final void undo() throws CannotUndoException {
    if (!canUndo()) {
      final String message = toString();
      throw new CannotUndoException(message);
    }
    this.hasBeenDone = false;
    undoDo();
  }

  protected void undoDo() {
  }
}
