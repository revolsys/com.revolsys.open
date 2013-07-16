package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXTextField;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.undo.CascadingUndoManager;
import com.revolsys.swing.undo.UndoManager;

public class NumberTextField extends JXTextField implements Field,
  DocumentListener, FocusListener {

  private static final long serialVersionUID = 1L;

  public static Number createMaximumValue(final DataType dataType,
    final int length, final int scale) {
    final Class<?> javaClass = dataType.getJavaClass();
    final StringBuffer text = new StringBuffer(length);
    for (int i = length - scale + 1; i > 1; i--) {
      text.append('9');
    }
    if (scale > 0) {
      text.append(".");
      for (int i = 0; i < scale; i++) {
        text.append('9');
      }
    }
    if (javaClass == Byte.class) {
      try {
        if (length == 0) {
          return Byte.MAX_VALUE;
        } else {
          return new BigDecimal(text.toString()).byteValueExact();
        }
      } catch (final ArithmeticException e) {
        return Byte.MAX_VALUE;
      }
    } else if (javaClass == Short.class) {
      try {
        if (length == 0) {
          return Short.MAX_VALUE;
        } else {
          return new BigDecimal(text.toString()).shortValueExact();
        }
      } catch (final ArithmeticException e) {
        return Short.MAX_VALUE;
      }
    } else if (javaClass == Integer.class) {
      try {
        if (length == 0) {
          return Integer.MAX_VALUE;
        } else {
          return new BigDecimal(text.toString()).intValueExact();
        }
      } catch (final ArithmeticException e) {
        return Integer.MAX_VALUE;
      }
    } else if (javaClass == Long.class) {
      try {
        if (length == 0) {
          return Long.MAX_VALUE;
        } else {
          return new BigDecimal(text.toString()).longValueExact();
        }
      } catch (final ArithmeticException e) {
        return Long.MAX_VALUE;
      }
    } else if (javaClass == Float.class) {
      if (length == 0) {
        return Float.MAX_VALUE;
      } else {
        return new BigDecimal(text.toString()).floatValue();
      }
    } else if (javaClass == Double.class) {
      if (length == 0) {
        return Double.MAX_VALUE;
      } else {
        return new BigDecimal(text.toString()).doubleValue();
      }
    } else {
      return (Number)StringConverterRegistry.toObject(javaClass, text);
    }
  }

  private static int getLength(final DataType dataType, int length,
    final int scale, final BigDecimal minimumValue) {
    if (length == 0) {
      final Class<?> javaClass = dataType.getJavaClass();
      if (javaClass == Byte.class) {
        length = 3;
      } else if (javaClass == Short.class) {
        length = 5;
      } else if (javaClass == Integer.class) {
        length = 10;
      } else if (javaClass == Long.class) {
        length = 19;
      } else if (javaClass == Float.class) {
        length = 10;
      } else if (javaClass == Double.class) {
        length = 20;
      } else {
        length = 20;
      }
    }
    if (minimumValue == null || new BigDecimal("0").compareTo(minimumValue) > 0) {
      length++;
    }
    if (scale > 0) {
      length++;
    }
    return length;
  }

  private final DataType dataType;

  private String fieldValidationMessage;

  private final int length;

  private final int scale;

  private BigDecimal minimumValue;

  private BigDecimal maximumValue;

  private boolean fieldValid = true;

  private final String fieldName;

  private Object fieldValue;

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  private String errorMessage;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  public NumberTextField(final DataType dataType, final int length) {
    this(dataType, length, 0);
  }

  public NumberTextField(final DataType dataType, final int length,
    final int scale) {
    this(dataType, length, scale, null, createMaximumValue(dataType, length,
      scale));
  }

  public NumberTextField(final DataType dataType, final int length,
    final int scale, final Number minimumValue, final Number maximumValue) {
    this(null, dataType, length, scale, minimumValue, maximumValue);
  }

  public NumberTextField(final String fieldName, final DataType dataType,
    final int length, final int scale) {
    this(fieldName, dataType, length, scale, null, createMaximumValue(dataType,
      length, scale));
  }

  public NumberTextField(final String fieldName, final DataType dataType,
    final int length, final int scale, final Number minimumValue,
    final Number maximumValue) {
    if (StringUtils.hasText(fieldName)) {
      this.fieldName = fieldName;
    } else {
      this.fieldName = "fieldValue";
    }
    setText(StringConverterRegistry.toString(this.fieldValue));
    addFocusListener(this);

    this.dataType = dataType;
    this.length = length;
    this.scale = scale;
    setMinimumValue(minimumValue);
    setMaximumValue(maximumValue);
    setColumns(getLength(dataType, length, scale, this.minimumValue));
    setHorizontalAlignment(RIGHT);
    getDocument().addDocumentListener(this);
    addFocusListener(this);
    PopupMenu.getPopupMenuFactory(this);
    undoManager.addKeyMap(this);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public void firePropertyChange(final String propertyName,
    final Object oldValue, final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    undoManager.discardAllEdits();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    final String text = getText();
    setFieldValue(text);
    undoManager.discardAllEdits();
  }

  @Override
  public String getFieldName() {
    return fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return fieldValidationMessage;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)fieldValue;
  }

  public int getLength() {
    return length;
  }

  public Number getMaximumValue() {
    return maximumValue;
  }

  public Number getMinimumValue() {
    return minimumValue;
  }

  public int getScale() {
    return scale;
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public boolean isFieldValid() {
    return fieldValid;
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_BACKGROUND;
    }
    setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = DEFAULT_FOREGROUND;
    }
    setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message) {
    setForeground(Color.RED);
    setSelectedTextColor(Color.RED);
    setBackground(Color.PINK);
    this.errorMessage = message;
    super.setToolTipText(errorMessage);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    setForeground(TextField.DEFAULT_FOREGROUND);
    setSelectedTextColor(TextField.DEFAULT_SELECTED_FOREGROUND);
    setBackground(TextField.DEFAULT_BACKGROUND);
    this.errorMessage = null;
    super.setToolTipText(originalToolTip);
  }

  @Override
  public void setFieldValue(final Object value) {
    undoManager.discardAllEdits();
    Object newValue;
    if (value == null) {
      newValue = null;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      final BigDecimal bigNumber = new BigDecimal(number.toString());
      final String numberString = bigNumber.toPlainString();
      if (!numberString.equals(bigNumber)) {
        setText(numberString);
      }
    } else {
      final String string = StringConverterRegistry.toString(value);
      if (!EqualsRegistry.equal(string, getText())) {
        setText(string);
      }
    }
    undoManager.discardAllEdits();

    validateField();
    final String text = getText();
    if (StringUtils.hasText(text)) {
      try {
        final BigDecimal bigNumber = new BigDecimal(text);
        final Number number = (Number)StringConverterRegistry.toObject(
          dataType, bigNumber);
        newValue = number;
      } catch (final Throwable t) {
        newValue = value;
      }
    } else {
      newValue = null;
    }

    final Object oldValue = fieldValue;
    if (!EqualsRegistry.equal(oldValue, newValue)) {
      this.fieldValue = newValue;
      firePropertyChange(fieldName, oldValue, newValue);
      SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
        oldValue, newValue);
    }
  }

  public void setMaximumValue(final Number maximumValue) {
    if (maximumValue == null) {
      this.maximumValue = null;
    } else {
      this.maximumValue = new BigDecimal(maximumValue.toString());
    }
  }

  public void setMinimumValue(final Number minimumValue) {
    if (minimumValue == null) {
      this.minimumValue = null;
    } else {
      this.minimumValue = new BigDecimal(minimumValue.toString());
    }
  }

  @Override
  public void setToolTipText(final String text) {
    this.originalToolTip = text;
    if (!StringUtils.hasText(errorMessage)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.undoManager.setParent(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  private void validateField() {
    final boolean oldValid = fieldValid;
    boolean valid = true;
    final String text = getText();
    if (StringUtils.hasText(text)) {
      try {
        BigDecimal number = new BigDecimal(text.trim());
        if (number.scale() < 0) {
          number = number.setScale(scale);
        }
        if (number.scale() > scale) {
          fieldValidationMessage = "Number of decimal places must be < "
            + scale;
          valid = false;
        } else if (minimumValue != null && minimumValue.compareTo(number) > 0) {
          fieldValidationMessage = "Value must be >= " + minimumValue;
          valid = false;
        } else if (maximumValue != null && maximumValue.compareTo(number) < 0) {
          fieldValidationMessage = "Value must be <= " + maximumValue;
          valid = false;
        } else {
          // number = number.setScale(scale);
          // final String newText = number.toPlainString();
          // if (!newText.equals(text)) {
          // setText(newText);
          // }
          fieldValidationMessage = "";
        }
      } catch (final Throwable t) {
        fieldValidationMessage = t.getMessage();
        valid = false;
      }
    } else {
      fieldValidationMessage = "";
    }

    if (valid != oldValid) {
      fieldValid = valid;
      firePropertyChange("fieldValid", oldValid, fieldValid);
    }
  }
}
