package com.revolsys.ui.html.form;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.decorator.Decorator;
import com.revolsys.ui.html.decorator.TableBody;
import com.revolsys.ui.html.fields.Field;
import com.revolsys.ui.html.fields.HiddenField;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.html.view.ElementContainer;
import com.revolsys.ui.html.view.SetObject;
import com.revolsys.util.Property;

public class UiBuilderObjectForm extends Form {
  private final HtmlUiBuilder<?> builder;

  private final ElementContainer fieldContainer = new ElementContainer(new TableBody());

  private List<String> fieldKeys;

  private final Object object;

  public UiBuilderObjectForm(final Object object, final HtmlUiBuilder<?> uiBuilder,
    final List<String> fieldKeys) {
    super(uiBuilder.getTypeName());
    this.object = object;
    this.builder = uiBuilder;
    this.fieldKeys = fieldKeys;
    add(this.fieldContainer);
  }

  public UiBuilderObjectForm(final Object object, final HtmlUiBuilder<?> uiBuilder,
    final String formName, final List<String> fieldKeys) {
    super(formName);
    this.object = object;
    this.builder = uiBuilder;
    this.fieldKeys = fieldKeys;
    add(new HiddenField("htmlCss", false));
    add(new HiddenField("plain", false));
    add(this.fieldContainer);
  }

  @Override
  public Object getInitialValue(final Field field, final HttpServletRequest request) {
    if (this.object != null) {
      final String propertyName = field.getName();
      if (propertyName != Form.FORM_TASK_PARAM) {
        try {
          return Property.get(this.object, propertyName);
        } catch (final IllegalArgumentException e) {
          return null;
        }
      }
    }
    return null;
  }

  public Object getObject() {
    return this.object;
  }

  @Override
  public void initialize(final HttpServletRequest request) {
    for (final String key : this.fieldKeys) {
      if (!getFieldNames().contains(key)) {
        final Element field = this.builder.getAttribute(request, key);
        if (field instanceof SetObject) {
          ((SetObject)field).setObject(this.object);
        }
        if (field != null) {
          if (!getElements().contains(field)) {
            if (field instanceof HiddenField) {
              final HiddenField hiddenField = (HiddenField)field;
              add(hiddenField);
            } else {
              final Decorator label = this.builder.getAttributeFormGroupLabel(key, field);
              this.fieldContainer.add(field, label);
            }
          }
        }
      }
    }
    this.builder.initializeForm(this, request);
    super.initialize(request);
  }

  public void setFieldKeys(final List<String> fieldKeys) {
    this.fieldKeys = fieldKeys;
  }

  @Override
  public boolean validate() {
    boolean valid = true;
    if (this.object != null) {
      for (final Field field : getFields().values()) {
        if (!field.hasValidationErrors() && !field.isReadOnly()) {
          final String propertyName = field.getName();
          if (propertyName != Form.FORM_TASK_PARAM && this.fieldKeys.contains(propertyName)) {
            final Object value = field.getValue();
            try {
              this.builder.setValue(this.object, propertyName, value);
            } catch (final IllegalArgumentException e) {
              field.addValidationError(e.getMessage());
              valid = false;
            }
          }
        }
      }
      if (valid) {
        valid &= this.builder.validateForm(this);
      }
    }
    valid &= super.validate();
    return valid;
  }
}
