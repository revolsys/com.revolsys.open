package com.revolsys.swing.field;

import java.util.List;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

public class CodeTableObjectToStringConverter extends ObjectToStringConverter {

  private CodeTable codeTable;

  public CodeTableObjectToStringConverter(CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  @Override
  public String getPreferredStringForItem(Object value) {
    if (value == null || value == CodeTableComboBoxModel.NULL) {
      return null;
    } else {
      List<Object> values = codeTable.getValues(value);
      if (values == null || values.isEmpty()) {
        return null;
      } else {
        return CollectionUtil.toString(values);
      }
    }
  }
}
