package com.revolsys.swing.tree;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;

public class TreeItemRunnable extends InvokeMethodRunnable {

  public static InvokeMethodAction createAction(final CharSequence name,
    final String iconName, EnableCheck enableCheck, String methodName,
    Object... parameters) {
    ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return createAction(name, icon, enableCheck, methodName, parameters);
  }

  public static InvokeMethodAction createAction(final CharSequence name,
    final Icon icon, EnableCheck enableCheck, String methodName,
    Object... parameters) {
    TreeItemRunnable runnable = new TreeItemRunnable(methodName, parameters);
    InvokeMethodAction action = new InvokeMethodAction(name, name.toString(),
      icon, true, runnable);
    action.setEnableCheck(enableCheck);

    return action;
  }

  protected TreeItemRunnable(String methodName, Object[] parameters) {
    super(methodName, parameters);
  }

  @Override
  public Object getObject() {
    return ObjectTree.getMouseClickItem();
  }

}
