package com.revolsys.swing.toolbar;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import com.revolsys.swing.action.InvokeMethodAction;

public class InvokeMethodActionToolBarButtonFactory extends InvokeMethodAction
  implements ToolBarButtonFactory {
  private static final long serialVersionUID = -5626990626102421865L;

  private boolean checkBox;

  public InvokeMethodActionToolBarButtonFactory() {
  }

  public InvokeMethodActionToolBarButtonFactory(final boolean checkBox,
    final CharSequence name, final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    super(name, invokeLater, object, methodName, parameters);
    this.checkBox = checkBox;
  }

  public InvokeMethodActionToolBarButtonFactory(final boolean checkBox,
    final CharSequence name, final Icon icon, final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    super(name, icon, invokeLater, object, methodName, parameters);
    this.checkBox = checkBox;
  }

  public InvokeMethodActionToolBarButtonFactory(final boolean checkBox,
    final CharSequence name, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    super(name, icon, object, methodName, parameters);
    this.checkBox = checkBox;
  }

  public InvokeMethodActionToolBarButtonFactory(final boolean checkBox,
    final CharSequence name, final Object object, final String methodName,
    final Object... parameters) {
    super(name, object, methodName, parameters);
    this.checkBox = checkBox;
  }

  public InvokeMethodActionToolBarButtonFactory(final boolean checkBox,
    final Icon icon, final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    super(icon, invokeLater, object, methodName, parameters);
    this.checkBox = checkBox;
  }

  public InvokeMethodActionToolBarButtonFactory(final boolean checkBox,
    final Icon icon, final Object object, final String methodName,
    final Object... parameters) {
    super(icon, object, methodName, parameters);
    this.checkBox = checkBox;
  }

  public InvokeMethodActionToolBarButtonFactory(final CharSequence name,
    final boolean invokeLater, final Object object, final String methodName,
    final Object... parameters) {
    super(name, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodActionToolBarButtonFactory(final CharSequence name,
    final CharSequence toolTip, final Icon icon, final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    super(name, icon, invokeLater, object, methodName, parameters);
    setToolTip(toolTip);
  }

  public InvokeMethodActionToolBarButtonFactory(final CharSequence name,
    final CharSequence toolTip, final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    super(name, icon, object, methodName, parameters);
    setToolTip(toolTip);
  }

  public InvokeMethodActionToolBarButtonFactory(final CharSequence name,
    final Icon icon, final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    super(name, icon, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodActionToolBarButtonFactory(final CharSequence name,
    final Icon icon, final Object object, final String methodName,
    final Object... parameters) {
    super(name, icon, object, methodName, parameters);
  }

  public InvokeMethodActionToolBarButtonFactory(final CharSequence name,
    final Object object, final String methodName, final Object... parameters) {
    super(name, object, methodName, parameters);
  }

  public InvokeMethodActionToolBarButtonFactory(final Icon icon,
    final boolean invokeLater, final Object object, final String methodName,
    final Object... parameters) {
    super(icon, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodActionToolBarButtonFactory(final Icon icon,
    final Object object, final String methodName, final Object... parameters) {
    super(icon, object, methodName, parameters);
  }

  @Override
  public Component createToolbarButton() {
    if (checkBox) {
      final JToggleButton button = new JToggleButton(this);
      return button;
    } else {
      final JButton button = new JButton(this);
      return button;
    }
  }

  public boolean isCheckBox() {
    return checkBox;
  }

  protected void setCheckBox(final boolean checkBox) {
    this.checkBox = checkBox;
  }
}
