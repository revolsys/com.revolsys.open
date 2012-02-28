package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class FieldWithSubmitButton extends Field {

  private Field field;
  
  
  public FieldWithSubmitButton(Field field, String name,
    String submitTitle) {
    super(name, false);
    this.field = field;
    setValue(submitTitle);
  }


  @Override
  public void initialize(Form form, HttpServletRequest request) {
    field.initialize(form, request);
  }

  @Override
  public void serializeElement(XmlWriter out) {
    field.serialize(out);
    HtmlUtil.serializeSubmitInput(out, getName(), getValue());
  }
}
