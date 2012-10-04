package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.revolsys.i18n.I18nCharSequence;
import com.revolsys.parallel.process.InvokeMethodRunnable;

public class InvokeMethodAction extends AbstractAction {

  /**
   * 
   */
  private static final long serialVersionUID = -5339626097125548212L;

  private boolean invokeLater;

  private Runnable runnable;

  public InvokeMethodAction() {
  }

  public InvokeMethodAction(final CharSequence name, final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    this(name, null, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodAction(final CharSequence name, String toolTip,
    final Icon icon, final Object object, final String methodName,
    final Object... parameters) {
    this(name, toolTip, icon, true, object, methodName, parameters);
  }

  public InvokeMethodAction(final CharSequence name, String toolTip,
    final Icon icon, final boolean invokeLater, final Object object,
    final String methodName, final Object... parameters) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
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
      i18nName.getI18n().addPropertyChangeListener("locale",
        new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            putValue(NAME, name.toString());
          }
        });

    }
  }

  public InvokeMethodAction(final CharSequence name, final Icon icon,
    final boolean invokeLater, final Object object, final String methodName,
    final Object... parameters) {
    runnable = new InvokeMethodRunnable(object, methodName, parameters);
    this.invokeLater = invokeLater;
    if (name != null) {
      putValue(NAME, name.toString());
      putValue(SHORT_DESCRIPTION, name.toString());
    }
    if (icon != null) {
      putValue(SMALL_ICON, icon);
    }
    if (name instanceof I18nCharSequence) {
      final I18nCharSequence i18nName = (I18nCharSequence)name;
      i18nName.getI18n().addPropertyChangeListener("locale",
        new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            putValue(NAME, name.toString());
          }
        });

    }
  }

  public InvokeMethodAction(final CharSequence name, final Icon icon,
    final Object object, final String methodName, final Object... parameters) {
    this(name, icon, false, object, methodName, parameters);
  }

  public InvokeMethodAction(final CharSequence name, final Object object,
    final String methodName, final Object... parameters) {
    this(name, null, false, object, methodName, parameters);
  }

  public InvokeMethodAction(final Icon icon, final boolean invokeLater,
    final Object object, final String methodName, final Object... parameters) {
    this(null, icon, invokeLater, object, methodName, parameters);
  }

  public InvokeMethodAction(final Icon icon, final Object object,
    final String methodName, final Object... parameters) {
    this(null, icon, false, object, methodName, parameters);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (invokeLater) {
      SwingUtilities.invokeLater(runnable);
    } else {
      runnable.run();
    }
  }

  public final Icon getIcon() {
    return (Icon)getValue(Action.SMALL_ICON);
  }

  public Integer getMnemonic() {
    return (Integer)getValue(Action.MNEMONIC_KEY);
  }

  public String getName() {
    return (String)getValue(Action.NAME);
  }

  public String getToolTip() {
    return (String)getValue(Action.SHORT_DESCRIPTION);
  }

  protected void setIcon(final Icon icon) {
    putValue(Action.SMALL_ICON, icon);
  }

  protected void setName(final String name) {
    putValue(Action.NAME, name);
  }

  protected void setToolTip(final CharSequence toolTip) {
    putValue(Action.SHORT_DESCRIPTION, toolTip.toString());
  }
}
