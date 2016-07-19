package com.revolsys.ui.html.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class SelectField extends Field {
  private Object defaultValue;

  private String nullValueLabel = "(None)";

  private String onChange;

  private final Map<String, FieldValue> optionMap = new HashMap<>();

  private final List<FieldValue> options = new ArrayList<>();

  private final Map<Object, FieldValue> optionValueMap = new HashMap<>();

  private String stringValue;

  public SelectField() {
  }

  public SelectField(final String name, final boolean required) {
    this(name, required, "(None)");
  }

  public SelectField(final String name, final boolean required, final String nullValueLabel) {
    super(name, required);
    this.nullValueLabel = nullValueLabel;
  }

  public SelectField(final String name, final Object defaultValue, final boolean required,
    final Map<? extends Object, ? extends Object> options) {
    this(name, required);
    this.defaultValue = defaultValue;
    if (defaultValue != null) {
      this.stringValue = defaultValue.toString();
    }
    for (final Entry<?, ?> entry : options.entrySet()) {
      final Object value = entry.getKey();
      final Object label = entry.getValue();
      addOption(value, label);
    }
  }

  public SelectField(final String name, final String defaultValue, final boolean required) {
    this(name, required);
    this.defaultValue = defaultValue;
    this.stringValue = defaultValue;
  }

  public SelectField addOption(final int index, final Object value, final String stringValue,
    final String label) {
    final FieldValue option = new FieldValue(value, stringValue, label);
    this.options.add(index, option);
    this.optionMap.put(stringValue, option);
    this.optionValueMap.put(value, option);
    return this;
  }

  public SelectField addOption(final Object value, final Object label) {
    return addOption(value, label.toString());
  }

  public SelectField addOption(final Object value, final Object stringValue, final String label) {
    return addOption(value, stringValue.toString(), label);
  }

  public SelectField addOption(final Object value, final String label) {
    String stringValue = null;
    if (value != null) {
      stringValue = value.toString();
    }
    return addOption(value, stringValue, label);
  }

  public SelectField addOption(final Object value, final String stringValue, final String label) {
    final FieldValue option = new FieldValue(value, stringValue, label);
    this.options.add(option);
    this.optionMap.put(stringValue, option);
    this.optionValueMap.put(value, option);
    return this;
  }

  public SelectField addOption(final String label) {
    return addOption(label, label);
  }

  public SelectField addOption(final String value, final String label) {
    return addOption(value, value, label);
  }

  public SelectField addOptions(final Map<Object, String> options) {
    for (final Entry<Object, String> entry : options.entrySet()) {
      final Object key = entry.getKey();
      final String label = entry.getValue();
      addOption(key, label);
    }
    return this;
  }

  @Override
  public SelectField clone() {
    final SelectField field = new SelectField();
    field.setName(getName());
    field.setDefaultValue(getDefaultValue());
    field.setRequired(isRequired());
    field.setReadOnly(isReadOnly());
    field.setNullValueLabel(getNullValueLabel());
    for (final FieldValue fieldValue : this.options) {
      final Object value = fieldValue.getValue();
      final String stringValue = fieldValue.getStringValue();
      final String label = fieldValue.getLabel();
      field.addOption(value, stringValue, label);
    }
    return field;
  }

  public Object getDefaultValue() {
    return this.defaultValue;
  }

  public String getNullValueLabel() {
    return this.nullValueLabel;
  }

  public FieldValue getSelectedOption() {
    return this.optionMap.get(this.stringValue);
  }

  public String getStringValue() {
    return this.stringValue;
  }

  @Override
  public boolean hasValue() {
    return this.stringValue != null && !this.stringValue.equals("");
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    if (!isRequired()) {
      addOption(0, null, null, this.nullValueLabel);
    }

    this.stringValue = request.getParameter(getName());
    if (this.stringValue == null) {
      setValue(getInitialValue(request));
    }
  }

  @Override
  public boolean isValid() {
    boolean valid = true;
    if (!super.isValid()) {
      valid = false;
    } else if (hasValue()) {
      final FieldValue option = getSelectedOption();
      if (option == null) {
        addValidationError("Invalid Value");
        valid = false;
      }
      if (valid) {
        setValue(option.getValue());
      }
    }
    return valid;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.startTag(HtmlElem.SELECT);
    out.attribute(HtmlAttr.NAME, getName());
    out.attribute(HtmlAttr.CLASS, "form-control input-sm");
    if (this.onChange != null) {
      out.attribute(HtmlAttr.ON_CHANGE, this.onChange);
    }
    if (isRequired()) {
      out.attribute(HtmlAttr.REQUIRED, true);
    }
    serializeOptions(out);
    out.endTag(HtmlElem.SELECT);
  }

  private void serializeOptions(final XmlWriter out) {
    if (this.options.size() == 0) {
      addOption(null, "(None)");
    }
    for (final FieldValue option : this.options) {
      out.startTag(HtmlElem.OPTION);
      if (option.getStringValue().equals(this.stringValue)) {
        out.attribute(HtmlAttr.SELECTED, "true");
      }
      if (!option.getStringValue().equals(option.getLabel())) {
        out.attribute(HtmlAttr.VALUE, option.getStringValue());
      }
      out.text(option.getLabel());
      out.endTag(HtmlElem.OPTION);
    }
  }

  public void setDefaultValue(final Object defaultValue) {
    this.defaultValue = defaultValue;
  }

  public void setNullValueLabel(final String nullValueLabel) {
    this.nullValueLabel = nullValueLabel;
  }

  public void setOnChange(final String onChange) {
    this.onChange = onChange;
  }

  public void setOptions(final Map<Object, String> options) {
    this.options.clear();
    this.optionMap.clear();
    this.optionValueMap.clear();
    for (final Entry<Object, String> entry : options.entrySet()) {
      final Object key = entry.getKey();
      final String label = entry.getValue();
      addOption(key, label);
    }
  }

  @Override
  public void setValue(final Object value) {
    super.setValue(value);
    if (value != null) {
      final FieldValue option = this.optionValueMap.get(value);
      if (option != null) {
        this.stringValue = option.getStringValue();
      } else {
        super.setValue(null);
        this.stringValue = null;
      }
    }
  }

}
