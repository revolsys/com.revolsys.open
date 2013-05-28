package com.revolsys.swing.field;

import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

public class ObjectLabelField extends JLabel implements Field<Object> {

  private Object fieldValue;

  @SuppressWarnings("unchecked")
  public <T> T getFieldValue() {
    return (T)fieldValue;
  }

  private final CodeTable codeTable;

  private String fieldName;

  @Override
  public String getFieldName() {
    return fieldName;
  }

  public ObjectLabelField() {
    this(null, null);
  }

  public ObjectLabelField(String fieldName) {
    this(fieldName, null);
  }

  public ObjectLabelField(String fieldName, final CodeTable codeTable) {
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),
      BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    this.fieldName = fieldName;
    this.codeTable = codeTable;
  }

  public void setFieldValue(Object object) {
    this.fieldValue = object;
    String text;
    if (object == null) {
      text = "-";
    } else if (codeTable == null) {
      text = StringConverterRegistry.toString(object);
    } else {
      final List<Object> values = codeTable.getValues(object);
      if (values == null || values.isEmpty()) {
        text = "-";
      } else {
        text = CollectionUtil.toString(values);
      }
    }
    setText(text);
  }
}
