package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.types.DataType;

public class NumberTextField extends JFormattedTextField implements
  FocusListener {

  private DataType dataType;

  public NumberTextField(DataType dataType, int length, int scale) {
    super(createFormatter(length, scale));
    this.dataType = dataType;
    setColumns(length + 2);
    setFocusLostBehavior(JFormattedTextField.PERSIST);
    addFocusListener(this);
    setMaxValue(createMaxValue(length, scale));
  }

  protected Comparable createMaxValue(int length, int scale) {
    Class<?> javaClass = dataType.getJavaClass();
    StringBuffer text = new StringBuffer(length);
    for (int i = length - scale + 1; i > 1; i--) {
      text.append('9');
    }
    if (scale > 0) {
      text.append(".");
      for (int i = 0; i < scale; i++) {
        text.append('9');
      }
    }
    BigDecimal maxValue = new BigDecimal(text.toString());
    if (javaClass == Byte.class) {
      try {
        return maxValue.byteValueExact();
      } catch (ArithmeticException e) {
        return Byte.MAX_VALUE;
      }
    } else if (javaClass == Short.class) {
      try {
        return maxValue.shortValueExact();
      } catch (ArithmeticException e) {
        return Short.MAX_VALUE;
      }
    } else if (javaClass == Integer.class) {
      try {
        return maxValue.intValueExact();
      } catch (ArithmeticException e) {
        return Integer.MAX_VALUE;
      }
    } else if (javaClass == Long.class) {
      try {
        return maxValue.longValueExact();
      } catch (ArithmeticException e) {
        return Long.MAX_VALUE;
      }
    } else if (javaClass == Float.class) {
      return maxValue.floatValue();
    } else if (javaClass == Double.class) {
      return maxValue.doubleValue();
    } else {
      return (Comparable)StringConverterRegistry.toObject(javaClass, text);
    }
  }

  public static NumberFormatter createFormatter(int length, int scale) {
    StringBuffer pattern = new StringBuffer(length);
    for (int i = length - scale + 1; i > 1; i--) {
      pattern.append('#');
    }
    if (scale > 0) {
      pattern.append(".");
      for (int i = 0; i < scale; i++) {
        pattern.append('#');
      }
    }
    DecimalFormat format = new DecimalFormat(pattern.toString());
    return new NumberFormatter(format);
  }

  public void setMinValue(Comparable minValue) {
    ((NumberFormatter)getFormatter()).setMinimum(minValue);
  }

  public void setMaxValue(Comparable maxValue) {
    NumberFormatter formatter = (NumberFormatter)getFormatter();
    formatter.setMaximum(maxValue);
  }

  @Override
  public void focusGained(FocusEvent e) {
  }

  @Override
  public void focusLost(FocusEvent event) {
    boolean valid = isFieldValid();
    if (valid) {
      setBackground(Color.WHITE);
      super.setToolTipText(originalToolTipText);
    } else {
      try {
        NumberFormatter formatter = (NumberFormatter)getFormatter();
        formatter.stringToValue(getText());

      } catch (ParseException e) {
        super.setToolTipText(null);
        updateUI();
        super.setToolTipText(e.getMessage());
        updateUI();
      }
      setBackground(Color.PINK);
    }
  }

  @Override
  public void setToolTipText(String text) {
    super.setToolTipText(text);
    this.originalToolTipText = text;
  }

  private String originalToolTipText;

  private boolean isFieldValid() {
    boolean valid = isEditValid();
    if (valid) {
      String text = getText();
      if (StringUtils.hasText(text)) {
        try {
          fieldValue = StringConverterRegistry.toObject(
            dataType.getJavaClass(), text);
        } catch (Throwable t) {
          fieldValue = null;
          valid = false;
        }
      } else {
        fieldValue = null;
      }
    } else {
      fieldValue = null;
    }
    return valid;
  }

  private Number fieldValue;

  public Number getFieldValue() {
    return fieldValue;
  }
}
