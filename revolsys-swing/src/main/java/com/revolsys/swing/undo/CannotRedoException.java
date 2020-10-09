package com.revolsys.swing.undo;

public class CannotRedoException extends javax.swing.undo.CannotRedoException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private final String message;

  public CannotRedoException(final String message) {
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
