package com.revolsys.swing.map.table;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.MethodUtils;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.util.ExceptionUtil;

public class InvokeMethodLayerTablePanelFactory implements
  LayerTablePanelFactory {

  private final Class<? extends Layer> layerClass;

  private final Object object;

  private final String methodName;

  public InvokeMethodLayerTablePanelFactory(final Class<? extends Layer> layerClass,
    final Object object, final String methodName) {
    this.layerClass = layerClass;
    this.object = object;
    this.methodName = methodName;
  }

  @Override
  public Component createPanel(final Layer layer) {
    try {
      if (object instanceof Class<?>) {
        final Class<?> clazz = (Class<?>)object;
        return (Component)MethodUtils.invokeStaticMethod(clazz, methodName,
          layer);
      } else {
        return (Component)MethodUtils.invokeMethod(object, methodName, layer);
      }
    } catch (final NoSuchMethodException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (final IllegalAccessException e) {
      return ExceptionUtil.throwUncheckedException(e);
    } catch (final InvocationTargetException e) {
      return ExceptionUtil.throwCauseException(e);
    }
  }

  public Class<? extends Layer> getLayerClass() {
    return layerClass;
  }

  @Override
  public String toString() {
    return layerClass.getName();
  }
}
