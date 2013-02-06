package com.revolsys.gis.data.model.filter;

import org.springframework.expression.AccessException;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;

import com.revolsys.gis.data.model.DataObject;

public class DataObjectAccessor implements PropertyAccessor {

  public boolean canRead(EvaluationContext context, Object target, String name)
    throws AccessException {
    DataObject object = (DataObject)target;
    return object.hasAttribute(name);
  }

  public TypedValue read(EvaluationContext context, Object target, String name)
    throws AccessException {
    DataObject object = (DataObject)target;
    Object value = object.getValue(name);
    if (value == null && !object.hasAttribute(name)) {
      throw new DataObjectAccessException(name);
    }
    return new TypedValue(value);
  }

  public boolean canWrite(EvaluationContext context, Object target, String name)
    throws AccessException {
    return true;
  }

  public void write(EvaluationContext context, Object target, String name,
    Object newValue) throws AccessException {
    DataObject object = (DataObject)target;
    object.setValue(name, newValue);
  }

  @SuppressWarnings("rawtypes")
  public Class[] getSpecificTargetClasses() {
    return new Class[] {
      DataObject.class
    };
  }

  @SuppressWarnings("serial")
  private static class DataObjectAccessException extends AccessException {

    private final String key;

    public DataObjectAccessException(String key) {
      super(null);
      this.key = key;
    }

    @Override
    public String getMessage() {
      return "DataObject does not contain a value for key '" + this.key + "'";
    }
  }

}
