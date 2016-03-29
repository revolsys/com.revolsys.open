package com.revolsys.ui.html.fields;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public abstract class NumberField extends TextField {

  private Number maximumValue;

  private Number minimumValue;

  private String units;

  public NumberField(final String name, final int size, final boolean required) {
    this(name, size, -1, null, required, null, null);
  }

  public NumberField(final String name, final int size, final int maxLength,
    final Object defaultValue, final boolean required) {
    this(name, size, maxLength, defaultValue, required, null, null);
  }

  public NumberField(final String name, final int size, final int maxLength,
    final Object defaultValue, final boolean required, final Number minimumValue,
    final Number maximumValue) {
    super(name, size, maxLength, defaultValue, required);
    setValue(defaultValue);
    setMinimumValue(minimumValue);
    setMaximumValue(maximumValue);
    setCssClass("number");
    setType("number");
  }

  /**
   * @return Returns the maximumValue.
   */
  public Number getMaximumValue() {
    return this.maximumValue;
  }

  /**
   * @return Returns the minimumValue.
   */
  public Number getMinimumValue() {
    return this.minimumValue;
  }

  public abstract Number getNumber(final String value);

  public String getUnits() {
    return this.units;
  }

  @Override
  protected void serializeAttributes(final XmlWriter out) {
    if (this.minimumValue != null) {
      out.attribute("min", this.minimumValue);
    }
    if (this.maximumValue != null) {
      out.attribute("max", this.maximumValue);
    }
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    super.serializeElement(out);
    if (Property.hasValue(this.units)) {
      out.startTag(HtmlElem.SPAN);
      out.attribute(HtmlAttr.CLASS, "units");
      out.text(" ");
      out.text(this.units);
      out.endTag(HtmlElem.SPAN);
    }
    if (this.minimumValue != null || this.maximumValue != null) {
      out.startTag(HtmlElem.SCRIPT);
      out.attribute(HtmlAttr.TYPE, "text/javascript");
      out.text("$(document).ready(function() {");
      out.text("$('#");
      out.text(getForm().getName());
      out.text(" input[name=");
      out.text(getName());
      out.text("]').rules('add', {");
      if (this.minimumValue != null) {
        out.text("min:");
        out.text(this.minimumValue);
      }
      if (this.maximumValue != null) {
        if (this.minimumValue != null) {
          out.text(",");
        }
        out.text("max:");
        out.text(this.maximumValue);
      }
      out.text("});});");
      out.endTag(HtmlElem.SCRIPT);
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
    if (Property.hasValue(value)) {
      try {
        final Number number = getNumber(value);
        if (this.minimumValue != null
          && ((Comparable<Number>)this.minimumValue).compareTo(number) > 0) {
          throw new IllegalArgumentException("Must be >= " + this.minimumValue);
        } else if (this.maximumValue != null
          && ((Comparable<Number>)this.maximumValue).compareTo(number) < 0) {
          throw new IllegalArgumentException("Must be <= " + this.maximumValue);
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
