package com.revolsys.spring.factory;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.GenericCollectionTypeResolver;

import com.revolsys.util.Exceptions;

@SuppressWarnings("rawtypes")
public class ListFactoryBean<T> extends AbstractFactoryBean<List> {

  private List<List<T>> sourceLists;

  private Class<List<T>> targetListClass;

  @Override
  @SuppressWarnings("unchecked")
  protected List createInstance() {
    if (this.sourceLists == null) {
      throw new IllegalArgumentException("'sourceLists' is required");
    }
    List result = null;
    if (this.targetListClass != null) {
      try {
        result = this.targetListClass.newInstance();
      } catch (final Exception e) {
        Exceptions.throwUncheckedException(e);
      }
    } else {
      result = new ArrayList();
    }
    Class valueType = null;
    if (this.targetListClass != null) {
      valueType = GenericCollectionTypeResolver.getCollectionType(this.targetListClass);
    }
    if (valueType != null) {
      final TypeConverter converter = getBeanTypeConverter();
      for (final List<T> list : this.sourceLists) {
        for (final Object value : list) {
          result.add(converter.convertIfNecessary(value, valueType));
        }
      }
    } else {
      for (final List<T> list : this.sourceLists) {
        result.addAll(list);
      }
    }
    return result;
  }

  @Override
  public Class<List> getObjectType() {
    return List.class;
  }

  public List<List<T>> getSourceLists() {
    return this.sourceLists;
  }

  public Class<List<T>> getTargetListClass() {
    return this.targetListClass;
  }

  public void setSourceLists(final List<List<T>> sourceLists) {
    this.sourceLists = sourceLists;
  }

  /**
   * Set the class to use for the target List. Can be populated with a fully
   * qualified class name when defined in a Spring application context.
   * <p>
   * Default is a <code>java.util.ArrayList</code>.
   *
   * @see java.util.ArrayList
   */
  public void setTargetListClass(final Class<List<T>> targetListClass) {
    if (targetListClass == null) {
      throw new IllegalArgumentException("'targetListClass' must not be null");
    }
    if (!List.class.isAssignableFrom(targetListClass)) {
      throw new IllegalArgumentException("'targetListClass' must implement [java.util.List]");
    }
    this.targetListClass = targetListClass;
  }

}
