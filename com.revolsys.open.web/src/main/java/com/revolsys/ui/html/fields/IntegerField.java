package com.revolsys.ui.html.fields;

import org.springframework.util.StringUtils;

public class IntegerField extends TextField {

  private int minimumValue = Integer.MIN_VALUE;

  private int maximumValue = Integer.MAX_VALUE;

  public IntegerField() {
    setSize(10);
  }

  public IntegerField(final String name, final boolean required) {
    super(name, 10, required);
  }

  public IntegerField(final String name, final boolean required, Object defaultValue) {
    super(name, 10, 19, defaultValue, required);
    setValue(defaultValue);
  }

  /**
   * @return Returns the maximumValue.
   */
  public int getMaximumValue() {
    return maximumValue;
  }

  /**
   * @return Returns the minimumValue.
   */
  public int getMinimumValue() {
    return minimumValue;
  }

  /**
   * @param maximumValue The maximumValue to set.
   */
  public void setMaximumValue(final int maximumValue) {
    this.maximumValue = maximumValue;
  }

  /**
   * @param minimumValue The minimumValue to set.
   */
  public void setMinimumValue(final int minimumValue) {
    this.minimumValue = minimumValue;
  }

  @Override
  public void setTextValue(final String value) {
    super.setTextValue(value);
    if (StringUtils.hasLength(value)) {
      try {
        final Integer intValue = new Integer(value);
        if (intValue.intValue() < minimumValue) {
          throw new IllegalArgumentException("Must be >= " + minimumValue);
        } else if (intValue.intValue() > maximumValue) {
          throw new IllegalArgumentException("Must be <= " + maximumValue);
        } else {
          setValue(intValue);
        }
      } catch (final NumberFormatException e) {
        throw new IllegalArgumentException("Must be a valid number");
      }
    } else {
      super.setValue(null);
    }
  }
}
