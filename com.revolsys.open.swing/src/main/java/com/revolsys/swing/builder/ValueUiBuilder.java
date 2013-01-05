package com.revolsys.swing.builder;

import java.awt.Component;

import javax.swing.CellEditor;

public interface ValueUiBuilder {

  Object getCellEditorValue();

  /**
   * Get the component to edit the specified value
   * 
   * @param value
   * @return
   * @see CellEditor
   */
  Component getEditorComponent(Object value);

  Component getRendererComponent(Object value);
}
