package com.revolsys.swing.tree;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;

public class TreeItemRunnable extends InvokeMethodRunnable {

  public static InvokeMethodAction createAction(final CharSequence name,
    final Icon icon, final EnableCheck enableCheck, final String methodName,
    final Object... parameters) {
    final TreeItemRunnable runnable = new TreeItemRunnable(methodName,
      parameters);
    final InvokeMethodAction action = new InvokeMethodAction(name,
      name.toString(), icon, true, runnable);
    action.setEnableCheck(enableCheck);

    return action;
  }

  public static InvokeMethodAction createAction(final CharSequence name,
    final String iconName, final EnableCheck enableCheck,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return createAction(name, icon, enableCheck, methodName, parameters);
  }

  public static InvokeMethodAction createAction(final CharSequence name,
    final String iconName, final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return createAction(name, icon, null, methodName, parameters);
  }

  protected TreeItemRunnable(final String methodName, final Object[] parameters) {
    super(methodName, parameters);
  }

  @Override
  public Object getObject() {
    return ObjectTree.getMouseClickItem();
  }

}
