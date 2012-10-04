package com.revolsys.swing.field;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

public class CodeTableListCellRenderer extends DefaultListCellRenderer {
  private CodeTable codeTable;

  public CodeTableListCellRenderer(CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
    if (value == null || value == CodeTableComboBoxModel.NULL) {
      value = "-";
    } else {
      List<Object> values = codeTable.getValues(value);
      if (values == null || values.isEmpty()) {
        value = "-";
      } else {
        value = CollectionUtil.toString(values);
      }
    }
    return super.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
  }
}
