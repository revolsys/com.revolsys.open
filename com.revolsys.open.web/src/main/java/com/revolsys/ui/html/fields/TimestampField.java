package com.revolsys.ui.html.fields;

import java.sql.Timestamp;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.datatype.DataTypes;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.Dates;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class TimestampField extends Field {

  private String inputValue;

  public TimestampField(final String name, final boolean required, final Object defaultValue) {
    super(name, required);
    setInitialValue(defaultValue);
    setValue(defaultValue);
    setDefaultInstructions("Enter Timestamp in format yyyy-MM-dd HH:mm:ss.SSS");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.inputValue = request.getParameter(getName());
    if (this.inputValue == null) {
      setValue(getInitialValue(request));
      final Timestamp date = getValue();
      if (date != null) {
        this.inputValue = Dates.toTimestampString(date);
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
          final Date date = Dates.getDate(this.inputValue);
          setValue(date);
        } catch (final Throwable e) {
          addValidationError("Invalid Timestamp");
          valid = false;

        }
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    out.attribute(HtmlUtil.ATTR_CLASS, "form-control input-sm");
    if (Property.hasValue(this.inputValue)) {
      out.attribute(HtmlUtil.ATTR_VALUE, this.inputValue);
    }
    out.attribute(HtmlUtil.ATTR_SIZE, 34);
    if (isRequired()) {
      out.attribute(HtmlUtil.ATTR_REQUIRED, true);
    }

    out.endTag(HtmlUtil.INPUT);
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
