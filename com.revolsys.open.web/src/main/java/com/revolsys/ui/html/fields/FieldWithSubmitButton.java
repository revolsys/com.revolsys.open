package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.util.HtmlUtil;

public class FieldWithSubmitButton extends Field {

  private final Field field;

  public FieldWithSubmitButton(final Field field, final String name, final String submitTitle) {
    super(name, false);
    this.field = field;
    setValue(submitTitle);
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    this.field.initialize(form, request);
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    this.field.serialize(out);
    HtmlUtil.serializeSubmitInput(out, getName(), getValue());
  }

  @Override
  public void setContainer(final ElementContainer container) {
    super.setContainer(container);
    if (this.field != null) {
      this.field.setContainer(container);
    }
  }
}
