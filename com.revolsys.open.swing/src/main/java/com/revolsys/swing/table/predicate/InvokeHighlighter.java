package com.revolsys.swing.table.predicate;

import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import org.jdesktop.swingx.decorator.BorderHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;
import org.slf4j.LoggerFactory;

import com.revolsys.swing.table.highlighter.OutsideBorderHighlighter;

public class InvokeHighlighter implements HighlightPredicate {

  public static Highlighter border(final Object object, final String methodName,
    final Border border) {
    final HighlightPredicate predicate = new InvokeHighlighter(object, methodName);
    return new BorderHighlighter(predicate, border);
  }

  public static Highlighter border(final Object object, final String methodName,
    final Color color) {
    return border(object, methodName, color, 1);
  }

  public static Highlighter border(final Object object, final String methodName, final Color color,
    final int thickness) {
    final Border border = BorderFactory.createLineBorder(color, thickness);
    return border(object, methodName, border);
  }

  public static Highlighter color(final Object object, final String methodName,
    final Color cellBackground, final Color cellForeground, final Color selectedBackground,
    final Color selectedForeground) {
    final HighlightPredicate predicate = new InvokeHighlighter(object, methodName);
    return new ColorHighlighter(predicate, cellBackground, cellForeground, selectedBackground,
      selectedForeground);
  }

  public static Highlighter outsideBorder(final Object object, final String methodName,
    final Color color, final int thickness) {
    final HighlightPredicate predicate = new InvokeHighlighter(object, methodName);
    return new OutsideBorderHighlighter(predicate, color, thickness, true, false);
  }

  private final Object object;

  private Method method;

  public InvokeHighlighter(final Object object, final String methodName) {
    this.object = object;
    Class<?> clazz;
    if (object instanceof Class) {
      clazz = (Class<?>)object;
    } else {
      clazz = object.getClass();
    }
    try {
      this.method = clazz.getMethod(methodName, Component.class, ComponentAdapter.class);
    } catch (final NoSuchMethodException e) {
      throw new IllegalArgumentException("Method does not exist", e);
    } catch (final SecurityException e) {
      throw new IllegalArgumentException("Method not accessible", e);
    }
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      return (Boolean)this.method.invoke(this.object, renderer, adapter);
    } catch (final InvocationTargetException e) {
      LoggerFactory.getLogger(getClass()).debug("Error invoking method " + this.method,
        e.getTargetException());
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).debug("Error invoking method " + this.method, e);
    }
    return false;
  }
}
