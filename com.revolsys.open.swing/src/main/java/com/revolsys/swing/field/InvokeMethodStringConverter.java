package com.revolsys.swing.field;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.apache.commons.beanutils.MethodUtils;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

public class InvokeMethodStringConverter extends ObjectToStringConverter
implements ListCellRenderer {

  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer();

  private final Object object;

  private final String methodName;

  private int horizontalAlignment = JLabel.LEFT;

  public InvokeMethodStringConverter(final Object object,
    final String methodName) {
    if (object == null) {
      throw new IllegalArgumentException("Object cannot be null " + this);
    }
    this.object = object;
    this.methodName = methodName;
  }

  public int getHorizontalAlignment() {
    return this.horizontalAlignment;
  }

  @Override
  public Component getListCellRendererComponent(final JList list,
    final Object value, final int index, final boolean isSelected,
    final boolean cellHasFocus) {
    this.renderer.getListCellRendererComponent(list, value, index, isSelected,
      cellHasFocus);
    final String text = getPreferredStringForItem(value);
    this.renderer.setText(text);
    this.renderer.setHorizontalAlignment(this.horizontalAlignment);
    return this.renderer;
  }

  @Override
  public String getPreferredStringForItem(final Object item) {
    if (item == null) {
      return "";
    } else {
      try {
        if (this.object instanceof Class<?>) {
          final Class<?> clazz = (Class<?>)this.object;
          return (String)MethodUtils.invokeStaticMethod(clazz, this.methodName,
            new Object[] {
            item
          });
        } else {
          return (String)MethodUtils.invokeMethod(this.object, this.methodName,
            new Object[] {
            item
          });
        }
      } catch (final Throwable e) {
        throw new RuntimeException("Unable to invoke " + this, e);
      }
    }
  }

  public void setHorizontalAlignment(final int horizontalAlignment) {
    this.horizontalAlignment = horizontalAlignment;
  }
}
