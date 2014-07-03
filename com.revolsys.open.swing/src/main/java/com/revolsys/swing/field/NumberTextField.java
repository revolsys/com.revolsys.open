package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXTextField;
import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.types.DataType;
import com.revolsys.swing.listener.WeakFocusListener;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.parallel.Invoke;
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

  public static final Color DEFAULT_SELECTED_FOREGROUND = new JTextField().getSelectedTextColor();

  public static final Color DEFAULT_BACKGROUND = new JTextField().getBackground();

  public static final Color DEFAULT_FOREGROUND = new JTextField().getForeground();

  private String errorMessage;

  private String originalToolTip;

  private final CascadingUndoManager undoManager = new CascadingUndoManager();

  private Object fieldValue;

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

    this.dataType = dataType;
    this.length = length;
    this.scale = scale;
    setMinimumValue(minimumValue);
    setMaximumValue(maximumValue);
    setColumns(getLength(dataType, length, scale, this.minimumValue));
    setHorizontalAlignment(RIGHT);
    getDocument().addDocumentListener(this);
    addFocusListener(new WeakFocusListener(this));
    PopupMenu.getPopupMenuFactory(this);
    this.undoManager.addKeyMap(this);
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
    this.undoManager.discardAllEdits();
  }

  @Override
  public void focusLost(final FocusEvent e) {
    updateFieldValue();
    this.undoManager.discardAllEdits();
  }

  @Override
  public String getFieldName() {
    return this.fieldName;
  }

  @Override
  public String getFieldValidationMessage() {
    return this.fieldValidationMessage;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getFieldValue() {
    return (V)fieldValue;
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
  public void setFieldInvalid(final String message,
    final Color foregroundColor, final Color backgroundColor) {
    setForeground(foregroundColor);
    setSelectedTextColor(foregroundColor);
    setBackground(backgroundColor);
    this.errorMessage = message;
    super.setToolTipText(this.errorMessage);
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
    super.setToolTipText(this.originalToolTip);
  }

  @Override
  public void setFieldValue(Object value) {
    if (value == null) {
      value = "";
    }
    if (SwingUtilities.isEventDispatchThread()) {
      final Object newValue = getTypedValue(value);
      if (!EqualsRegistry.equal(this.fieldValue, newValue)) {
        this.undoManager.discardAllEdits();
        String newText = StringConverterRegistry.toString(newValue);
        if (newValue == null) {
          newText = "";
        } else if (newValue instanceof Number) {
          BigDecimal decimal = new BigDecimal(newText);
          if (decimal.scale() < scale) {
            decimal = decimal.setScale(scale, RoundingMode.HALF_UP);
          }
          newText = decimal.toPlainString();
        } else {
          newText = StringConverterRegistry.toString(newValue);
        }
        if (!EqualsRegistry.equal(newText, getText())) {
          setText(newText);
        }
        final Object oldValue = this.fieldValue;
        this.fieldValue = newValue;
        validateField();
        firePropertyChange(this.fieldName, oldValue, this.fieldValue);
        SetFieldValueUndoableEdit.create(this.undoManager.getParent(), this,
          oldValue, this.fieldValue);
      }
    } else {
      Invoke.later(this, "setFieldValue", value);
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
    if (!StringUtils.hasText(this.errorMessage)) {
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

  @Override
  public void updateFieldValue() {
    final String text = getText();
    setFieldValue(text);
  }

  private void validateField() {
    final boolean oldValid = this.fieldValid;
    boolean valid = true;
    final String text = getText();
    if (StringUtils.hasText(text)) {
      try {
        BigDecimal number = new BigDecimal(text.trim());
        if (number.scale() < 0) {
          number = number.setScale(this.scale);
        }
        if (number.scale() > this.scale) {
          this.fieldValidationMessage = "Number of decimal places must be < "
            + this.scale;
          valid = false;
        } else if (this.minimumValue != null
          && this.minimumValue.compareTo(number) > 0) {
          this.fieldValidationMessage = "Value must be >= " + this.minimumValue;
          valid = false;
        } else if (this.maximumValue != null
          && this.maximumValue.compareTo(number) < 0) {
          this.fieldValidationMessage = "Value must be <= " + this.maximumValue;
          valid = false;
        } else {
          // number = number.setScale(scale);
          // final String newText = number.toPlainString();
          // if (!newText.equals(text)) {
          // setText(newText);
          // }
          this.fieldValidationMessage = "";
        }
      } catch (final Throwable t) {
        this.fieldValidationMessage = t.getMessage();
        valid = false;
      }
    } else {
      this.fieldValidationMessage = "";
    }

    if (valid != oldValid) {
      this.fieldValid = valid;
      firePropertyChange("fieldValid", oldValid, this.fieldValid);
    }
  }
}
