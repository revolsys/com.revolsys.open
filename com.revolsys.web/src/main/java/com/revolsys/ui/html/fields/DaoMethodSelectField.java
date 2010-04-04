package com.revolsys.ui.html.fields;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.orm.core.DataAccessObject;
import com.revolsys.ui.html.form.Form;
import com.revolsys.util.CaseConverter;

public class DaoMethodSelectField extends SelectField {

  private DataAccessObject<?> dataAccessObject;

  private String methodName;

  private List<Object> methodArguments = Collections.emptyList();

  public DaoMethodSelectField() {
  }

  public DaoMethodSelectField clone() {
    DaoMethodSelectField field = new DaoMethodSelectField();
    field.setName(getName());
    field.setDefaultValue(getDefaultValue());
    field.setRequired(isRequired());
    field.setReadOnly(isReadOnly());
    field.setNullValueLabel(getNullValueLabel());
    field.setDataAccessObject(dataAccessObject);
    field.setMethodName(methodName);
    field.setMethodArguments(methodArguments);
    return field;
  }

  public void initialize(Form form, HttpServletRequest request) {
    try {
      List<Object> options = (List<Object>)MethodUtils.invokeMethod(
        dataAccessObject, methodName, methodArguments.toArray());
      for (Object option : options) {
        addOption(option, option.toString());
      }
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    super.initialize(form, request);
  }

  public DataAccessObject<?> getDataAccessObject() {
    return dataAccessObject;
  }

  public void setDataAccessObject(DataAccessObject<?> dataAccessObject) {
    this.dataAccessObject = dataAccessObject;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public List<Object> getMethodArguments() {
    return methodArguments;
  }

  public void setMethodArguments(List<Object> methodArguments) {
    this.methodArguments = methodArguments;
  }

}
