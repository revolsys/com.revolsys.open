package com.revolsys.ui.html.fields;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.data.io.DataAccessObject;
import com.revolsys.ui.html.form.Form;

public class DaoMethodSelectField extends SelectField {

  private DataAccessObject<?> dataAccessObject;

  private String methodName;

  private List<Object> methodArguments = Collections.emptyList();

  public DaoMethodSelectField() {
  }

  @Override
  public DaoMethodSelectField clone() {
    final DaoMethodSelectField field = new DaoMethodSelectField();
    field.setName(getName());
    field.setDefaultValue(getDefaultValue());
    field.setRequired(isRequired());
    field.setReadOnly(isReadOnly());
    field.setNullValueLabel(getNullValueLabel());
    field.setDataAccessObject(this.dataAccessObject);
    field.setMethodName(this.methodName);
    field.setMethodArguments(this.methodArguments);
    return field;
  }

  public DataAccessObject<?> getDataAccessObject() {
    return this.dataAccessObject;
  }

  public List<Object> getMethodArguments() {
    return this.methodArguments;
  }

  public String getMethodName() {
    return this.methodName;
  }

  @Override
  public void initialize(final Form form, final HttpServletRequest request) {
    try {
      final List<Object> options = (List<Object>)MethodUtils.invokeMethod(this.dataAccessObject,
        this.methodName, this.methodArguments.toArray());
      for (final Object option : options) {
        addOption(option, option.toString());
      }
    } catch (final NoSuchMethodException e) {
      e.printStackTrace();
    } catch (final IllegalAccessException e) {
      e.printStackTrace();
    } catch (final InvocationTargetException e) {
      e.printStackTrace();
    }
    super.initialize(form, request);
  }

  public void setDataAccessObject(final DataAccessObject<?> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public void setMethodArguments(final List<Object> methodArguments) {
    this.methodArguments = methodArguments;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

}
