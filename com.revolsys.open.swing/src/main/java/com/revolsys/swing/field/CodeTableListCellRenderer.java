package com.revolsys.swing.field;

import java.awt.Component;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.code.CodeTable;
import com.revolsys.util.Strings;

public class CodeTableListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;

  private final CodeTable codeTable;

  public CodeTableListCellRenderer(final CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  @Override
  public Component getListCellRendererComponent(final JList list, Object value, final int index,
    final boolean isSelected, final boolean cellHasFocus) {
    if (value == null || value == Identifier.NULL) {
      value = "-";
    } else if (index >= 0) {
      final List<Object> values = this.codeTable.getValues(Identifier.newIdentifier(value));
      if (values == null || values.isEmpty()) {
        value = "-";
      } else {
        value = Strings.toString(":", values);
      }
    }
    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
  }
}
