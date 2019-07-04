package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;

import org.jeometry.common.logging.Logs;

import com.revolsys.i18n.I18nCharSequence;
import com.revolsys.swing.Icons;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.AbstractActionMainMenuItemFactory;
import com.revolsys.swing.parallel.Invoke;

public class ConsumerAction extends AbstractActionMainMenuItemFactory {

  private static final long serialVersionUID = -5339626097125548212L;

  public static ConsumerAction action(final CharSequence name, final String toolTip,
    final Icon icon, final Consumer<ActionEvent> handler) {
    return action(name, toolTip, icon, null, true, handler);
  }

  public static ConsumerAction action(final CharSequence name, final String toolTip,
    final Icon icon, final EnableCheck enableCheck, final boolean invokeLater,
    final Consumer<ActionEvent> handler) {
    final ConsumerAction action = new ConsumerAction(name, toolTip, icon, enableCheck, invokeLater,
      handler);
    return action;
  }

  public static ConsumerAction action(final String name, final Consumer<ActionEvent> handler) {
    return action(name, null, null, null, true, handler);
  }

  public static ConsumerAction action(final String toolTip, final Icon icon,
    final Consumer<ActionEvent> handler) {
    return action(null, toolTip, icon, null, true, handler);
  }

  public static ConsumerAction action(final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final Consumer<ActionEvent> handler) {
    return action(null, toolTip, icon, enableCheck, true, handler);
  }

  public static JButton button(final CharSequence name, final String toolTip, final Icon icon,
    final Consumer<ActionEvent> handler) {
    return button(name, toolTip, icon, null, true, handler);
  }

  public static JButton button(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final boolean invokeLater, final Consumer<ActionEvent> handler) {
    final ConsumerAction action = new ConsumerAction(name, toolTip, icon, enableCheck, invokeLater,
      handler);
    return action.newButton();
  }

  public static JButton button(final Icon icon, final String toolTip,
    final Consumer<ActionEvent> handler) {
    return button(null, toolTip, icon, null, true, handler);
  }

  public static JButton button(final String name, final Consumer<ActionEvent> handler) {
    return button(name, null, null, null, true, handler);
  }

  public static JCheckBoxMenuItem checkBoxMenuItem(final CharSequence name, final String toolTip,
    final Icon icon, final EnableCheck enableCheck, final boolean invokeLater,
    final Consumer<ActionEvent> handler) {
    final ConsumerAction action = new ConsumerAction(name, toolTip, icon, enableCheck, invokeLater,
      handler);
    return action.newCheckboxMenuItem();
  }

  public static JCheckBoxMenuItem checkBoxMenuItem(final String name,
    final Consumer<ActionEvent> handler) {
    return checkBoxMenuItem(name, null, null, null, false, handler);
  }

  public static JMenuItem menuItem(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final boolean invokeLater, final Consumer<ActionEvent> handler) {
    final ConsumerAction action = new ConsumerAction(name, toolTip, icon, enableCheck, invokeLater,
      handler);
    return action.newMenuItem();
  }

  public static JMenuItem menuItem(final String name, final Consumer<ActionEvent> handler) {
    return menuItem(name, null, null, null, false, handler);
  }

  public static JMenuItem menuItem(final String name, final Icon icon,
    final Consumer<ActionEvent> handler) {
    return menuItem(name, null, icon, null, false, handler);
  }

  public static JMenuItem menuItem(final String name, final String iconName,
    final Consumer<ActionEvent> handler) {
    final Icon icon = Icons.getIcon(iconName);
    return menuItem(name, null, icon, null, false, handler);
  }

  private final Consumer<ActionEvent> handler;

  private final boolean invokeLater;

  public ConsumerAction(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final boolean invokeLater, final Consumer<ActionEvent> handler) {
    this.handler = handler;
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
      i18nName.getI18n().addPropertyChangeListener("locale", new PropertyChangeListener() {
        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
          putValue(NAME, name.toString());
        }
      });

    }
    setEnableCheck(enableCheck);
  }

  public void actionDo(final ActionEvent event) {
    if (this.handler != null) {
      try {
        this.handler.accept(event);
      } catch (final NoSuchElementException e) {
      } catch (final Throwable e) {
        Logs.error(this.handler, "Error Performing action", e);
      }
    }
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    if (this.invokeLater) {
      Invoke.later(() -> {
        actionDo(event);
      });
    } else {
      actionDo(event);
    }
  }

  @Override
  public String toString() {
    return this.handler.toString();
  }

}
