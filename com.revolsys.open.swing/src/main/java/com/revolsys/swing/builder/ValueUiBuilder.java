package com.revolsys.swing.builder;

import javax.swing.CellEditor;
import javax.swing.JComponent;

public interface ValueUiBuilder {

  Object getCellEditorValue();

  /**
   * Get the component to edit the specified value
   *
   * @param value
   * @return
   * @see CellEditor
   */
  JComponent getEditorComponent(Object value);

  JComponent getRendererComponent(Object value);
}
