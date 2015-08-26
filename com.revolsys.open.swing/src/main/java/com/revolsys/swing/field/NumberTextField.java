package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXTextField;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.Equals;
import com.revolsys.data.types.DataType;
import com.revolsys.data.types.DataTypes;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;

public class NumberTextField extends JXTextField implements Field, DocumentListener, FocusListener {
  private static final long serialVersionUID = 1L;

  public static Number createMaximumValue(final DataType dataType, final int length,
    final int scale) {
    final Class<?> javaClass = dataType.getJavaClass();
    final StringBuilder text = new StringBuilder(length);
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

  private static int getLength(final DataType dataType, int length, final int scale,
    final BigDecimal minimumValue) {
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

  private boolean fieldValid = true;

  private final int length;

  private BigDecimal maximumValue;

  private BigDecimal minimumValue;

  private final int scale;

  private final FieldSupport support;

  public NumberTextField(final DataType dataType, final int length) {
    this(dataType, length, 0);
  }

  public NumberTextField(final DataType dataType, final int length, final int scale) {
    this(dataType, length, scale, null, createMaximumValue(dataType, length, scale));
  }

  public NumberTextField(final DataType dataType, final int length, final int scale,
    final Number minimumValue, final Number maximumValue) {
    this(null, dataType, length, scale, minimumValue, maximumValue);
  }

  public NumberTextField(final String fieldName, final DataType dataType, final int length,
    final int scale) {
    this(fieldName, dataType, length, scale, null, createMaximumValue(dataType, length, scale));
  }

  public NumberTextField(String fieldName, final DataType dataType, final int length,
    final int scale, final Number minimumValue, final Number maximumValue) {
    if (!Property.hasValue(fieldName)) {
      fieldName = "fieldValue";
    }
    this.support = new FieldSupport(this, fieldName, null);
    this.dataType = dataType;
    this.length = length;
    this.scale = scale;
    setMinimumValue(minimumValue);
    setMaximumValue(maximumValue);
    setColumns(getLength(dataType, length, scale, this.minimumValue) + 1);
    setHorizontalAlignment(RIGHT);
    getDocument().addDocumentListener(this);
    addFocusListener(new WeakFocusListener(this));
    PopupMenu.getPopupMenuFactory(this);
    setFont(SwingUtil.FONT);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public void focusGained(final FocusEvent e) {
    this.support.discardAllEdits();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    updateFieldValue();
    this.support.discardAllEdits();
  }

  @Override
  public String getFieldName() {
    return this.support.getName();
  }

  @Override
  public String getFieldValidationMessage() {
    return this.support.getErrorMessage();
  }

  @Override
  public <V> V getFieldValue() {
    return this.support.getValue();
  }

  public int getLength() {
    return this.length;
  }

  public Number getMaximumValue() {
    return this.maximumValue;
  }

  public Number getMinimumValue() {
    return this.minimumValue;
  }

  public int getScale() {
    return this.scale;
  }

  private Object getTypedValue(final Object value) {
    if (value == null) {
      return null;
    } else {
      if ("NaN".equalsIgnoreCase(value.toString())) {
        return Double.NaN;
      } else if ("-Infinity".equalsIgnoreCase(value.toString())) {
        return Double.NEGATIVE_INFINITY;
      } else if ("Infinity".equalsIgnoreCase(value.toString())) {
        return Double.POSITIVE_INFINITY;
      }
      try {
        final BigDecimal bigNumber = new BigDecimal(value.toString());
        return StringConverterRegistry.toObject(this.dataType, bigNumber);
      } catch (final Throwable t) {
        return value.toString();
      }
    }
  }

  @Override
  public void insertUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public boolean isFieldValid() {
    return this.fieldValid;
  }

  @Override
  public void removeUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    this.support.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    this.support.setFieldValid();
  }

  @Override
  public void setFieldValue(Object value) {
    if (value == null) {
      value = "";
    }
    if (SwingUtilities.isEventDispatchThread()) {
      final Object newValue = getTypedValue(value);
      final Object oldValue = getFieldValue();
      String newText;
      if (newValue == null) {
        newText = "";
      } else if (newValue instanceof Number) {
        newText = MathUtil.toString((Number)newValue);
        if ("NAN".equalsIgnoreCase(newText)) {
          newText = "NaN";
        } else if ("Infinity".equalsIgnoreCase(newText)) {
          newText = "Infinity";
        } else if ("-Infinity".equalsIgnoreCase(newText)) {
          newText = "-Infinity";
        } else {
          final BigDecimal decimal = new BigDecimal(newText);
          newText = decimal.toPlainString();
        }
      } else {
        newText = StringConverterRegistry.toString(newValue);
      }
      if (!Equals.equal(newText, getText())) {
        setText(newText);
      }
      if (!Equals.equal(oldValue, newValue)) {
        validateField();
        this.support.setValue(newValue);
      }
    } else {
      Invoke.later(this, "setFieldValue", value);
    }
  }

  public void setMaximumValue(final Number maximumValue) {
    if (maximumValue == null) {
      this.maximumValue = null;
    } else {
      this.maximumValue = new BigDecimal(MathUtil.toString(maximumValue));
    }
  }

  public void setMinimumValue(final Number minimumValue) {
    if (minimumValue == null) {
      this.minimumValue = null;
    } else {
      this.minimumValue = new BigDecimal(MathUtil.toString(minimumValue));
    }
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.support.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.support.setUndoManager(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    final String text = getText();
    setFieldValue(text);
  }

  private void validateField() {
    final boolean oldValid = this.fieldValid;
    final String text = getText();
    String message = null;
    if (Property.hasValue(text)) {
      if ("NaN".equalsIgnoreCase(text)) {
        if (this.dataType.equals(DataTypes.DOUBLE)) {
        } else if (this.dataType.equals(DataTypes.FLOAT)) {
        } else {
          message = "'" + text + "' is not a valid " + this.dataType.getValidationName() + ".";
        }
      } else if ("Infinity".equalsIgnoreCase(text)) {
        if (this.dataType.equals(DataTypes.DOUBLE)) {
        } else if (this.dataType.equals(DataTypes.FLOAT)) {
        } else {
          message = "'" + text + "' is not a valid " + this.dataType.getValidationName() + ".";
        }
      } else if ("-Infinity".equalsIgnoreCase(text)) {
        if (this.dataType.equals(DataTypes.DOUBLE)) {
        } else if (this.dataType.equals(DataTypes.FLOAT)) {
        } else {
          message = "'" + text + "' is not a valid " + this.dataType.getValidationName() + ".";
        }
      } else {
        try {
          BigDecimal number = new BigDecimal(text.trim());
          if (number.scale() < 0) {
            number = number.setScale(this.scale);
          }
          if (number.scale() > this.scale) {
            message = "Number of decimal places must be < " + this.scale;
          } else if (this.minimumValue != null && this.minimumValue.compareTo(number) > 0) {
            message = MathUtil.toString(number) + " < " + MathUtil.toString(this.minimumValue)
              + " (minimum)";
          } else if (this.maximumValue != null && this.maximumValue.compareTo(number) < 0) {
            message = MathUtil.toString(number) + " > " + MathUtil.toString(this.maximumValue)
              + " (maximum)";
          } else {
            // number = number.setScale(scale);
            // final String newText = number.toPlainString();
            // if (!newText.equals(text)) {
            // setText(newText);
            // }
            message = null;
          }
        } catch (final Throwable t) {
          message = "'" + text + "' is not a valid " + this.dataType.getValidationName() + ".";
        }
      }
    }
    final boolean valid = !Property.hasValue(message);
    if (valid != oldValid) {
      this.fieldValid = valid;
      firePropertyChange("fieldValid", oldValid, this.fieldValid);
    }
    if (valid) {
      this.support.setFieldValid();
    } else {
      this.support.setFieldInvalid(message, WebColors.Red, WebColors.Pink);
    }
  }
}
