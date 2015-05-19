package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import com.revolsys.i18n.I18nCharSequence;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.AbstractActionMainMenuItemFactory;
import com.revolsys.swing.parallel.Invoke;

public class RunnableAction extends AbstractActionMainMenuItemFactory {

  private static final long serialVersionUID = -5339626097125548212L;

  public static JButton createButton(final CharSequence name, final String toolTip,
    final Icon icon, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, toolTip, icon, true, runnable);
    return action.createButton();
  }

  public static JButton createButton(final String name, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, runnable);
    return action.createButton();
  }

  public static JCheckBoxMenuItem createCheckBoxMenuItem(final String name, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, runnable);
    action.setCheckBox(true);
    return (JCheckBoxMenuItem)action.createComponent();
  }

  public static JMenuItem createMenuItem(final String name, final Icon icon, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, icon, runnable);
    return action.createComponent();
  }

  public static JMenuItem createMenuItem(final String name, final Runnable runnable) {
    final RunnableAction action = new RunnableAction(name, runnable);
    return action.createComponent();
  }

  public static JMenuItem createMenuItem(final String name, final String iconName,
    final Runnable runnable) {
    final Icon icon = Icons.getIcon(iconName);
    final RunnableAction action = new RunnableAction(name, icon, runnable);
    action.setIconName(iconName);
    return action.createComponent();
  }

  private boolean invokeLater;

  private Runnable runnable;

  private String iconName;

  public RunnableAction() {
  }

  public RunnableAction(final CharSequence name, final boolean invokeLater, final Runnable runnable) {
    this(name, null, invokeLater, runnable);
  }

  public RunnableAction(final CharSequence name, final Icon icon, final boolean invokeLater,
    final Runnable runnable) {
    this.runnable = runnable;
    this.invokeLater = invokeLater;
    if (name != null) {
      putValue(NAME, name.toString());
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
  };

  public RunnableAction(final CharSequence name, final Icon icon, final Runnable runnable) {
    this(name, icon, false, runnable);
  }

  public RunnableAction(final CharSequence name, final Runnable runnable) {
    this(name, null, false, runnable);
  }

  public RunnableAction(final CharSequence name, final String toolTip, final Icon icon,
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

  public RunnableAction(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final Runnable runnable) {
    this(name, toolTip, icon, true, runnable);
    setEnableCheck(enableCheck);
  }

  public RunnableAction(final CharSequence name, final String toolTip, final Icon icon,
    final Runnable runnable) {
    this(name, toolTip, icon, true, runnable);
  }

  public RunnableAction(final Icon icon, final boolean invokeLater, final Runnable runnable) {
    this(null, icon, invokeLater, runnable);
  }

  public RunnableAction(final Icon icon, final Runnable runnable) {
    this(null, icon, false, runnable);
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
