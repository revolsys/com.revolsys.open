package com.revolsys.swing.field;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.commons.beanutils.MethodUtils;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class InvokeMethodStringConverter extends ObjectToStringConverter
  implements ListCellRenderer {

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  private final Object object;

  private final String methodName;

  public InvokeMethodStringConverter(final Object object,
    final String methodName) {
    if (object == null) {
      throw new IllegalArgumentException("Object cannot be null " + this);
    }
    this.object = object;
    this.methodName = methodName;
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object value, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    renderer.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
    final String text = getPreferredStringForItem(value);
    renderer.setText(text);
    return renderer;
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item == null) {
      return "";
    } else {
      try {
        if (object instanceof Class<?>) {
          final Class<?> clazz = (Class<?>)object;
          return (String)MethodUtils.invokeStaticMethod(clazz, methodName,
            new Object[] {
              item
            });
        } else {
          return (String)MethodUtils.invokeMethod(object, methodName,
            new Object[] {
              item
            });
        }
      } catch (final Throwable e) {
        throw new RuntimeException("Unable to invoke " + this, e);
      }
    }
  }
}
