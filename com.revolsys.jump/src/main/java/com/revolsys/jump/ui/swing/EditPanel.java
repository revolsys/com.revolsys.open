package com.revolsys.jump.ui.swing;

import java.awt.LayoutManager;

import javax.swing.JPanel;

public class EditPanel<T> extends JPanel {
  /**
   * 
   */
  private static final long serialVersionUID = 5962576341440705313L;

  private T value;

  public EditPanel() {
  }

  public EditPanel(final boolean isDoubleBuffered) {
    super(isDoubleBuffered);
  }

  public EditPanel(final LayoutManager layout, final boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
  }

  public EditPanel(final LayoutManager layout) {
    super(layout);
  }

  public T getValue() {
    return value;
  }

  public void setValue(final T value) {
    this.value = value;
  }

  public void save() {
  }

  public String getTitle() {
    return "";
  }

  public void reset() {
  }
}
