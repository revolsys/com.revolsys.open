package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;
import com.revolsys.ui.html.form.Form;

public class CheckBox01Field extends Field {
  private boolean selected = false;

  private final String selectedValue = "on";

  private String onClick = null;

  public CheckBox01Field() {
  }

  public CheckBox01Field(final String name) {
    super(name, false);
  }

  public CheckBox01Field(final String name, final boolean required) {
    super(name, required);
  }

  public String getOnClick() {
    return onClick;
  }

  @Override
  public boolean hasValue() {
    return isSelected();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String inputValue = request.getParameter(getName());
    if (inputValue != null) {
      selected = inputValue.equals(selectedValue);
      if (selected ) {
        setValue(1);
      } else {
        setValue(0);
      }
    } else if (request.getMethod() == "GET" || !getForm().isMainFormTask()) {
      setValue(getInitialValue(request));
      Object value = getValue();
      if (getValue() != null) {
        if (value instanceof Number) {
          Integer number = ((Number)value).intValue();
          if (number.compareTo(Integer.parseInt(value.toString())) == 0) {
            selected = true;
          } else {
            selected = false;
          }
        } else if (value instanceof Boolean) {

          selected = (Boolean)value;
        } else {
          String string = value.toString();
          if (string.equals("1")) {
            selected = true;
          } else if (string.equals("Y")) {
            selected = true;
          } else if (string.equals("Yes")) {
            selected = true;
          } else {
            selected = Boolean.parseBoolean(string);
            selected = value.equals(false);
          }
        }
      }
    } else {
      setValue(0);
      selected = false;
    }
  }

  public boolean isSelected() {
    return selected;
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else {
      valid = true;
    }
    if (valid) {
      if (isSelected()) {
        setValue(1);
      } else {
        setValue(0);
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    HtmlUtil.serializeCheckBox(out, getName(), selectedValue, isSelected(),
      onClick);
  }

  public void setOnClick(final String onSelect) {
    this.onClick = onSelect;
  }
}
