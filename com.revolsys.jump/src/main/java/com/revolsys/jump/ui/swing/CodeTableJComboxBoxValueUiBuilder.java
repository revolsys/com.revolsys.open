package com.revolsys.jump.ui.swing;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.revolsys.gis.data.model.codes.CodeTable;

public class CodeTableJComboxBoxValueUiBuilder extends AbstractValueUIBuilder {
  private JLabel rendererComponent = new JLabel();

  private JComboBox editorComponent;

  private CodeTable codeTable;

  public CodeTableJComboxBoxValueUiBuilder(final CodeTable codeTable) {
    rendererComponent.setOpaque(true);
    this.codeTable = codeTable;
    editorComponent = new JComboBox(new CodeTableComboBoxModel(codeTable));
    editorComponent.setRenderer(new CodeTableListCellRenderer());
  }

  public Component getEditorComponent(final Object key) {
    Object value = codeTable.getValue((Number)key);
    editorComponent.setSelectedItem(value);
    return editorComponent;
  }

  public Component getRendererComponent(final Object key) {
    if (key == null) {
      rendererComponent.setText("-");
    } else {
      List<Object> values = codeTable.getValues((Number)key);
      if (values != null) {
        rendererComponent.setText(toString(values));
      } else {
        rendererComponent.setText(key.toString());
      }
    }
    return rendererComponent;
  }

  private String toString(final List<Object> values) {
    StringBuffer sb = new StringBuffer();
    for (Iterator<Object> valueIter = values.iterator(); valueIter.hasNext();) {
      Object object = valueIter.next();
      sb.append(object);
      if (valueIter.hasNext()) {
        sb.append(",");
      }
    }
    return sb.toString();
  }

  @SuppressWarnings("unchecked")
  public Object getCellEditorValue() {
    Entry<Number, List<Object>> value = (Entry<Number, List<Object>>)editorComponent.getSelectedItem();
    if (value == null) {
      return null;
    } else {
      return value.getKey();
    }
  }

}
