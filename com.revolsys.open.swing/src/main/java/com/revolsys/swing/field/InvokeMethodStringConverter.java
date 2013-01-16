package com.revolsys.swing.field;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.commons.beanutils.MethodUtils;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class InvokeMethodStringConverter extends ObjectToStringConverter
  implements ListCellRenderer {

  private DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  private Object object;

  private String methodName;

  public InvokeMethodStringConverter(Object object, String methodName) {
    if (object == null) {
      throw new IllegalArgumentException("Object cannot be null " + this);
    }
    this.object = object;
    this.methodName = methodName;
  }

  @Override
  public String getPreferredStringForItem(Object item) {
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

  @Override
  public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
    renderer.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
    String text = getPreferredStringForItem(value);
    renderer.setText(text);
    return renderer;
  }
}
