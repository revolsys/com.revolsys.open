package com.revolsys.swing.field;

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

public class ObjectLabelField extends JLabel implements Field {
  private static final long serialVersionUID = 1L;

  private Object fieldValue;

  private final CodeTable codeTable;

  private final String fieldName;

  private String errorMessage;

  private final Color defaultBackground = getBackground();

  private final Color defaultForeground = getForeground();

  private String originalToolTip;

  public ObjectLabelField() {
    this("fieldValue", null);
  }

  public ObjectLabelField(final String fieldName) {
    this(fieldName, null);
  }

  public ObjectLabelField(final String fieldName, final CodeTable codeTable) {
    setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createBevelBorder(BevelBorder.LOWERED),
      BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    this.fieldName = fieldName;
    this.codeTable = codeTable;
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getFieldValue() {
    final Object value = fieldValue;
    return (T)value;
  }

  @Override
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
  }

  @Override
  public void setFieldValid() {
    setForeground(defaultForeground);
    setBackground(defaultBackground);
    this.errorMessage = null;
    super.setToolTipText(originalToolTip);
  }

  @Override
  public void setFieldValue(final Object object) {
    final Object oldValue = this.fieldValue;
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
    firePropertyChange(fieldName, oldValue, object);
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

}
