package com.revolsys.swing.field;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class NumberTextField extends JTextField implements DocumentListener,
  FocusListener, ValidatingField {

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

  private static int getLength(final DataType dataType, int length, int scale,
    BigDecimal minimumValue) {
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

  private Number fieldValue;

  private BigDecimal minimumValue;

  private BigDecimal maximumValue;

  private boolean fieldValid = true;

  public NumberTextField(final DataType dataType, final int length,
    final int scale) {
    this(dataType, length, scale, null, createMaximumValue(dataType, length,
      scale));
  }

  public NumberTextField(final DataType dataType, final int length,
    final int scale, Number minimumValue, Number maximumValue) {
    this.dataType = dataType;
    this.length = length;
    this.scale = scale;
    setMinimumValue(minimumValue);
    setMaximumValue(maximumValue);
    setColumns(getLength(dataType, length, scale, this.minimumValue));
    setHorizontalAlignment(RIGHT);
    getDocument().addDocumentListener(this);
    addFocusListener(this);
  }

  public NumberTextField(DataType dataType, int length) {
    this(dataType, length, 0);
  }

  @Override
  public void changedUpdate(final DocumentEvent e) {
    validateField();
  }

  @Override
  public String getFieldValidationMessage() {
    return fieldValidationMessage;
  }

  public Number getFieldValue() {
    return fieldValue;
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

  private void validateField() {
    Number oldValue = fieldValue;
    Number value = null;
    boolean oldValid = fieldValid;
    boolean valid = true;
    final String text = getText();
    if (StringUtils.hasText(text)) {
      try {
        BigDecimal number = new BigDecimal(text);
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
          number = number.setScale(scale);
          value = (Number)StringConverterRegistry.toObject(dataType, number);
        }
      } catch (final Throwable t) {
        fieldValidationMessage = t.getMessage();
        valid = false;
      }
    }

    if (valid != oldValid) {
      fieldValid = valid;
      firePropertyChange("fieldValid", oldValid, fieldValid);
    }
    if (valid) {
      if (!EqualsRegistry.equal(oldValue, value)) {
        fieldValue = value;
        firePropertyChange("fieldValid", oldValue, fieldValue);
      }
    }
  }

  public void setFieldValue(Number value) {
    setText(StringConverterRegistry.toString(value));
    validateField();
    updateText();
  }

  @Override
  public void focusGained(FocusEvent e) {
  }

  @Override
  public void focusLost(FocusEvent e) {
    updateText();
  }

  private void updateText() {
    if (isFieldValid() && fieldValue != null) {
      String text = getText();
      BigDecimal number = new BigDecimal(text);
      number = number.setScale(scale);
      String newText = number.toPlainString();
      if (!newText.equals(text)) {
        setText(newText);
      }
    }
  }

}
