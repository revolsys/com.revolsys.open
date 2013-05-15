package com.revolsys.swing.component;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class CodeTableLabel extends JLabel {
  private final CodeTable codeTable;

  public CodeTableLabel(final CodeTable codeTable) {
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),
      BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    this.codeTable = codeTable;
  }

  private Object value;

  public void setValue(Object value) {
    this.value = value;
    if (value == null) {
      setText("-");
    } else {
      final List<Object> values = codeTable.getValues(value);
      if (values == null || values.isEmpty()) {
        setText("-");
      } else {
        setText(CollectionUtil.toString(values));
      }
    }
  }

  public Object getValue() {
    return value;
  }
}
