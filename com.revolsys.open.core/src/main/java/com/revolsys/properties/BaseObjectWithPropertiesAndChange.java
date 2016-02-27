package com.revolsys.properties;

import java.beans.PropertyChangeSupport;
import java.util.Map;

import com.revolsys.beans.KeyedPropertyChangeEvent;
import com.revolsys.beans.PropertyChangeSupportProxy;
import com.revolsys.datatype.DataType;
import com.revolsys.util.Property;

public class BaseObjectWithPropertiesAndChange extends BaseObjectWithProperties
  implements PropertyChangeSupportProxy {
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  public BaseObjectWithPropertiesAndChange() {
  }

  public BaseObjectWithPropertiesAndChange(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  @Override
  protected BaseObjectWithPropertiesAndChange clone() {
    final BaseObjectWithPropertiesAndChange clone = (BaseObjectWithPropertiesAndChange)super.clone();
    clone.propertyChangeSupport = new PropertyChangeSupport(clone);
    return clone;
  }

  @Override
  public <C> C getProperty(final String name) {
    C value = Property.getSimple(this, name);
    if (value == null) {
      final Map<String, Object> properties = getProperties();
      value = ObjectWithProperties.getProperty(this, properties, name);
    }
    return value;
  }

  @Override
  public PropertyChangeSupport getPropertyChangeSupport() {
    return this.propertyChangeSupport;
  }

  @Override
  public void setProperty(final String name, final Object value) {
    final Object oldValue = getProperty(name);
    if (!DataType.equal(oldValue, value)) {
      super.setProperty(name, value);
      final Object newValue = getProperty(name);
      final KeyedPropertyChangeEvent event = new KeyedPropertyChangeEvent(this, "property",
        oldValue, newValue, name);
      firePropertyChange(event);
    }
  }
}
