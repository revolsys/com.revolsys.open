package com.revolsys.jump.ui.swing;

import java.awt.Component;

import javax.swing.CellEditor;

public interface ValueUiBuilder {

  Component getRendererComponent(Object value);

  /**
   * Get the component to edit the specified value
   * 
   * @param value
   * @return
   * @see CellEditor
   */
  Component getEditorComponent(Object value);

  Object getCellEditorValue();
}
