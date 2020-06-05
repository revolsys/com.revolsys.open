package com.revolsys.swing.undo;

public class CannotUndoException extends javax.swing.undo.CannotUndoException {

  private final String message;

  public CannotUndoException(final String message) {
    this.message = message;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public String toString() {
    return this.message;
  }

}
