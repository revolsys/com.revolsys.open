package com.revolsys.ui.html.fields;

import org.springframework.util.StringUtils;

public abstract class NumberField extends TextField {

  private Number minimumValue;

  private Number maximumValue;

  public NumberField(String name, int size, boolean required) {
    this(name, size, -1, null, required, null, null);
  }

  public NumberField(final String name, int size, int maxLength,
    Object defaultValue, final boolean required) {
    this(name, size, maxLength, defaultValue, required, null, null);
  }

  public NumberField(final String name, int size, int maxLength,
    Object defaultValue, final boolean required, Number minimumValue,
    Number maximumValue) {
    super(name, size, maxLength, defaultValue, required);
    setValue(defaultValue);
    setMinimumValue(minimumValue);
    setMaximumValue(maximumValue);
    setCssClass("number");
  }

  /**
   * @return Returns the maximumValue.
   */
  public Number getMaximumValue() {
    return maximumValue;
  }

  /**
   * @return Returns the minimumValue.
   */
  public Number getMinimumValue() {
    return minimumValue;
  }

  /**
   * @param maximumValue The maximumValue to set.
   */
  public void setMaximumValue(final Number maximumValue) {
    this.maximumValue = maximumValue;
  }

  /**
   * @param minimumValue The minimumValue to set.
   */
  public void setMinimumValue(final Number minimumValue) {
    this.minimumValue = minimumValue;
  }

  @Override
  public void setTextValue(final String value) {
    super.setTextValue(value);
    if (StringUtils.hasLength(value)) {
      try {
        final Number number = getNumber(value);
        if (minimumValue != null
          && ((Comparable<Number>)minimumValue).compareTo(number) > 0) {
          throw new IllegalArgumentException("Must be >= " + minimumValue);
        } else if (maximumValue != null
          && ((Comparable<Number>)maximumValue).compareTo(number) < 0) {
          throw new IllegalArgumentException("Must be <= " + maximumValue);
        } else {
          setValue(number);
        }
      } catch (final NumberFormatException e) {
        throw new IllegalArgumentException("Must be a valid number");
      }
    } else {
      super.setValue(null);
    }
  }

  public abstract Number getNumber(final String value);
}
