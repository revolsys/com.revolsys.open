package com.revolsys.ui.html.fields;

import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;

public abstract class NumberField extends TextField {

  private Number minimumValue;

  private Number maximumValue;

  private String units;

  public NumberField(final String name, final int size, final boolean required) {
    this(name, size, -1, null, required, null, null);
  }

  public NumberField(final String name, final int size, final int maxLength,
    final Object defaultValue, final boolean required) {
    this(name, size, maxLength, defaultValue, required, null, null);
  }

  public NumberField(final String name, final int size, final int maxLength,
    final Object defaultValue, final boolean required,
    final Number minimumValue, final Number maximumValue) {
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

  public abstract Number getNumber(final String value);

  public String getUnits() {
    return units;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    super.serializeElement(out);
    if (StringUtils.hasText(units)) {
      out.startTag(HtmlUtil.SPAN);
      out.attribute(HtmlUtil.ATTR_CLASS, "units");
      out.text(" ");
      out.text(units);
      out.endTag(HtmlUtil.SPAN);
    }
    if (minimumValue != null || maximumValue != null) {
      out.startTag(HtmlUtil.SCRIPT);
      out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
      out.text("$(document).ready(function() {");
      out.text("$('#");
      out.text(getForm().getName());
      out.text(" input[name=");
      out.text(getName());
      out.text("]').rules('add', {");
      if (minimumValue != null) {
        out.text("min:");
        out.text(minimumValue);
      }
      if (maximumValue != null) {
        if (minimumValue != null) {
          out.text(",");
        }
        out.text("max:");
        out.text(maximumValue);
      }
      out.text("});});");
      out.endTag(HtmlUtil.SCRIPT);
    }
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

  public void setUnits(final String units) {
    this.units = units;
  }
}
