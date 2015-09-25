package com.revolsys.io.map;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.util.ExceptionUtil;

public class InvokeMethodMapObjectFactory extends AbstractMapObjectFactory
  implements MapSerializer {
  private final String methodName;

  private final Class<?> typeClass;

  public InvokeMethodMapObjectFactory(final String typeName, final String description,
    final Class<?> typeClass, final String methodName) {
    super(typeName, description);
    this.typeClass = typeClass;
    this.methodName = methodName;
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<>();
    map.put("typeName", getTypeName());
    map.put("description", getDescription());
    map.put("typeClass", this.typeClass);
    map.put("methodName", this.methodName);
    return map;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V toObject(final Map<String, ? extends Object> properties) {
    try {
      final Class<?> clazz = this.typeClass;
      return (V)MethodUtils.invokeStaticMethod(clazz, this.methodName, properties);
    } catch (final NoSuchMethodException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (final IllegalAccessException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (final InvocationTargetException e) {
      return ExceptionUtil.throwCauseException(e);
    }
  }
}
