package com.revolsys.ui.html.fields;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlUtil;

public class CheckBox01Field extends Field {
  private String onClick = null;

  private boolean selected = false;

  private final String selectedValue = "on";

  public CheckBox01Field() {
  }

  public CheckBox01Field(final String name) {
    super(name, false);
  }

  public CheckBox01Field(final String name, final boolean required) {
    super(name, required);
  }

  public String getOnClick() {
    return this.onClick;
  }

  @Override
  public boolean hasValue() {
    return isSelected();
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    final String inputValue = request.getParameter(getName());
    if (inputValue != null) {
      this.selected = inputValue.equals(this.selectedValue);
      if (this.selected) {
        setValue(1);
      } else {
        setValue(0);
      }
    } else if (request.getMethod() == "GET" || !getForm().isMainFormTask()) {
      setValue(getInitialValue(request));
      final Object value = getValue();
      if (getValue() != null) {
        if (value instanceof Number) {
          final Integer number = ((Number)value).intValue();
          if (number.intValue() == 1) {
            this.selected = true;
          } else {
            this.selected = false;
          }
        } else if (value instanceof Boolean) {

          this.selected = (Boolean)value;
        } else {
          final String string = value.toString();
          if (string.equals("1")) {
            this.selected = true;
          } else if (string.equals("Y")) {
            this.selected = true;
          } else if (string.equals("Yes")) {
            this.selected = true;
          } else {
            this.selected = Boolean.parseBoolean(string);
            this.selected = value.equals(false);
          }
        }
      }
    } else {
      setValue(0);
      this.selected = false;
    }
  }

  public boolean isSelected() {
    return this.selected;
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
    HtmlUtil.serializeCheckBox(out, getName(), this.selectedValue, isSelected(), this.onClick);
  }

  public void setOnClick(final String onSelect) {
    this.onClick = onSelect;
  }
}
