package com.revolsys.swing.undo;

import java.io.Serializable;

import javax.swing.UIManager;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class AbstractUndoableEdit implements UndoableEdit, Serializable {

  private boolean hasBeenDone;

  private boolean alive;

  public AbstractUndoableEdit() {
    super();

    hasBeenDone = true;
    alive = true;
  }

  @Override
  public boolean addEdit(final UndoableEdit anEdit) {
    return false;
  }

  @Override
  public boolean canRedo() {
    return alive && !hasBeenDone;
  }

  @Override
  public boolean canUndo() {
    return alive && hasBeenDone;
  }

  @Override
  public void die() {
    alive = false;
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

  @Override
  public boolean isSignificant() {
    return true;
  }

  @Override
  public void redo() throws CannotRedoException {
    if (!canRedo()) {
      throw new CannotRedoException();
    }
    hasBeenDone = true;
  }

  @Override
  public boolean replaceEdit(final UndoableEdit anEdit) {
    return false;
  }

  @Override
  public String toString() {
    return super.toString() + " hasBeenDone: " + hasBeenDone + " alive: "
      + alive;
  }

  @Override
  public void undo() throws CannotUndoException {
    if (!canUndo()) {
      throw new CannotUndoException();
    }
    hasBeenDone = false;
  }
}
