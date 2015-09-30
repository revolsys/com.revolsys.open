package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;

import javax.swing.Icon;

import com.revolsys.i18n.I18nCharSequence;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.menu.AbstractActionMainMenuItemFactory;
import com.revolsys.swing.parallel.Invoke;

public class ConsumerObjectAction<T> extends AbstractActionMainMenuItemFactory {
  private static final long serialVersionUID = -5339626097125548212L;

  private String iconName;

  private boolean invokeLater;

  private Consumer<T> consumer;

  public ConsumerObjectAction() {
  }

  public ConsumerObjectAction(final CharSequence name, final boolean invokeLater,
    final Consumer<T> consumer) {
    this(name, null, invokeLater, consumer);
  }

  public ConsumerObjectAction(final CharSequence name, final Consumer<T> consumer) {
    this(name, null, false, consumer);
  };

  public ConsumerObjectAction(final CharSequence name, final Icon icon, final boolean invokeLater,
    final Consumer<T> consumer) {
    this.consumer = consumer;
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
  }

  public ConsumerObjectAction(final CharSequence name, final Icon icon,
    final Consumer<T> consumer) {
    this(name, icon, false, consumer);
  }

  public ConsumerObjectAction(final CharSequence name, final String toolTip, final Icon icon,
    final boolean invokeLater, final Consumer<T> consumer) {
    this.consumer = consumer;
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

  public ConsumerObjectAction(final CharSequence name, final String toolTip, final Icon icon,
    final Consumer<T> consumer) {
    this(name, toolTip, icon, true, consumer);
  }

  public ConsumerObjectAction(final CharSequence name, final String toolTip, final Icon icon,
    final EnableCheck enableCheck, final Consumer<T> consumer) {
    this(name, toolTip, icon, true, consumer);
    setEnableCheck(enableCheck);
  }

  public ConsumerObjectAction(final Icon icon, final boolean invokeLater,
    final Consumer<T> consumer) {
    this(null, icon, invokeLater, consumer);
  }

  public ConsumerObjectAction(final Icon icon, final Consumer<T> consumer) {
    this(null, icon, false, consumer);
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final T object = getObject();
    if (this.invokeLater) {
      Invoke.later(() -> this.consumer.accept(object));
    } else {
      this.consumer.accept(object);
    }
  }

  @Override
  public String getIconName() {
    return this.iconName;
  }

  public T getObject() {
    return null;
  }

  public void setIconName(final String iconName) {
    this.iconName = iconName;
  }

  @Override
  public String toString() {
    return this.consumer.toString();
  }

}
