package com.revolsys.ui.html.fields;

import java.sql.Date;

import javax.servlet.http.HttpServletRequest;

import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.date.Dates;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;
import com.revolsys.util.Property;

public class DateField extends Field {

  private String inputValue;

  /**
   * @param name
   * @param required
   */
  public DateField(final String name, final boolean required) {
    super(name, required);
    setDefaultInstructions("Enter/select date in format yyyy-MM-dd");
  }

  public DateField(final String name, final boolean required, final Object defaultValue) {
    super(name, required);
    setInitialValue(defaultValue);
    setValue(defaultValue);
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.inputValue = request.getParameter(getName());
    if (this.inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        final java.util.Date date = getValue();
        this.inputValue = Dates.toSqlDateString(date);
      }
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {

      if (valid) {
        try {
          final Date date = new Date(Dates.getDate("yyyy-MM-dd", this.inputValue).getTime());
          setValue(date);
        } catch (final Throwable e) {
          addValidationError("Invalid Date");
          valid = false;

        }
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, "text/javascript");
    out.text("$(function() {$(\"#" + getForm().getName() + " input[name='" + getName()
      + "']\").datepicker(" + "{changeMonth: true,changeYear: true, dateFormat:'" + "yy-mm-dd"
      + "'});});");
    out.endTag(HtmlElem.SCRIPT);

    out.startTag(HtmlElem.INPUT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.TYPE, "date");
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    if (Property.hasValue(this.inputValue)) {
      out.attribute(HtmlAttr.VALUE, this.inputValue);
    }
    if (isRequired()) {
      out.attribute(HtmlAttr.REQUIRED, true);
    }

    out.endTag(HtmlElem.INPUT);
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      this.inputValue = DataTypes.toString(value);
    } else {
      this.inputValue = null;
    }
  }

}
