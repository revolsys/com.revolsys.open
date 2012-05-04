package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class FieldWithSubmitButton extends Field {

  private final Field field;

  public FieldWithSubmitButton(final Field field, final String name,
    final String submitTitle) {
    super(name, false);
    this.field = field;
    setValue(submitTitle);
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    field.initialize(form, request);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    field.serialize(out);
    HtmlUtil.serializeSubmitInput(out, getName(), getValue());
  }
}
