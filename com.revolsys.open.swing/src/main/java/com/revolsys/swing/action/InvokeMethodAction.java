package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

import com.revolsys.i18n.I18nCharSequence;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.AbstractActionMainMenuItemFactory;
import com.revolsys.swing.parallel.Invoke;

public class InvokeMethodAction extends AbstractActionMainMenuItemFactory {

  private static final long serialVersionUID = -5339626097125548212L;

  private String iconName;

  private boolean invokeLater;

  private Runnable runnable;

  public InvokeMethodAction() {
  }

  public InvokeMethodAction(final CharSequence name, final Object object, final String methodName,
    final Object... parameters) {
    this.runnable = new InvokeMethodRunnable(object, methodName, parameters);
    if (name != null) {
      putValue(NAME, name.toString());
    }
    if (name instanceof I18nCharSequence) {
      final I18nCharSequence i18nName = (I18nCharSequence)name;
      i18nName.getI18n().addListener("locale", new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
          putValue(NAME, name.toString());
        }
      });

    }
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final boolean invokeLater, final Object object, final String methodName,
    final Object... parameters) {
    this(name, toolTip, icon, invokeLater,
      new InvokeMethodRunnable(object, methodName, parameters));
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final boolean invokeLater, final Runnable runnable) {
    this.runnable = runnable;
    this.invokeLater = invokeLater;
    if (name != null) {
      putValue(NAME, name.toString());
    }
    if (toolTip != null) {
      putValue(SHORT_DESCRIPTION, toolTip.toString());
    }
    if (icon != null) {
      putValue(SMALL_ICON, icon);
    }
    if (name instanceof I18nCharSequence) {
      final I18nCharSequence i18nName = (I18nCharSequence)name;
      i18nName.getI18n().addListener("locale", new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
          putValue(NAME, name.toString());
        }
      });

    }
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final Object object, final String methodName,
    final Object... parameters) {
    this(name, toolTip, icon, true, object, methodName, parameters);
    setEnableCheck(enableCheck);
  }

  public InvokeMethodAction(final CharSequence name, final String toolTip, final Icon icon,
    final Object object, final String methodName, final Object... parameters) {
    this(name, toolTip, icon, true, object, methodName, parameters);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (this.invokeLater) {
      Invoke.later(this.runnable);
    } else {
      this.runnable.run();
    }
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  public void setIconName(final String iconName) {
    this.iconName = iconName;
  }

  @Override
  public String toString() {
    return this.runnable.toString();
  }

}
