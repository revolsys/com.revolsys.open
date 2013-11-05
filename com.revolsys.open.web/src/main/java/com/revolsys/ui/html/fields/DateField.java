package com.revolsys.ui.html.fields;

import java.sql.Date;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.DateUtil;

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

  public DateField(final String name, final boolean required,
    final Object defaultValue) {
    super(name, required);
    setInitialValue(defaultValue);
    setValue(defaultValue);
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    inputValue = request.getParameter(getName());
    if (inputValue == null) {
      setValue(getInitialValue(request));
      if (getValue() != null) {
        inputValue = StringConverterRegistry.toString(Date.class, getValue());
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
          final Date date = new Date(DateUtil.parse("yyyy-MM-dd", inputValue)
            .getTime());
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
    out.startTag(HtmlUtil.SCRIPT);
    out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
    out.text("$(function() {$(\"#" + getForm().getName() + " input[name='"
      + getName() + "']\").datepicker("
      + "{changeMonth: true,changeYear: true, dateFormat:'" + "yy-mm-dd"
      + "'});});");
    out.endTag(HtmlUtil.SCRIPT);

    out.startTag(HtmlUtil.INPUT);
    out.attribute(HtmlUtil.ATTR_NAME, getName());
    out.attribute(HtmlUtil.ATTR_TYPE, "text");
    if (StringUtils.hasText(inputValue)) {
      out.attribute(HtmlUtil.ATTR_VALUE, inputValue);
    }
    if (isRequired()) {
      out.attribute(HtmlUtil.ATTR_CLASS, "required");
    }

    out.endTag(HtmlUtil.INPUT);
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      inputValue = StringConverterRegistry.toString(value);
    } else {
      inputValue = null;
    }
  }

}
