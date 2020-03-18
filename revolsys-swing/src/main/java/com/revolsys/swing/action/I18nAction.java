package com.revolsys.swing.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;

import com.revolsys.i18n.I18n;
import com.revolsys.i18n.I18nCharSequence;

public class I18nAction extends AbstractAction {

  /**
   *
   */
  private static final long serialVersionUID = 256392126027298835L;

  public I18nAction() {
    final Class<? extends I18nAction> clazz = getClass();
    final String nameKey = clazz.getName();
    init(clazz, nameKey, nameKey, nameKey, null);
  }

  public I18nAction(final Class<?> clazz, final String nameKey) {
    init(clazz, nameKey, nameKey, nameKey, null);
  }

  public I18nAction(final Class<?> clazz, final String nameKey, final Icon icon) {
    init(clazz, nameKey, nameKey, nameKey, icon);
  }

  public I18nAction(final Class<?> clazz, final String nameKey, final String shortDescriptionKey,
    final Icon icon) {
    init(clazz, nameKey, nameKey, shortDescriptionKey, icon);
  }

  public I18nAction(final Icon icon) {
    final Class<? extends I18nAction> clazz = getClass();
    final String nameKey = clazz.getName();
    init(getClass(), nameKey, nameKey, nameKey, icon);
  }

  public I18nAction(final String nameKey) {
    init(getClass(), nameKey, nameKey, nameKey, null);
  }

  public I18nAction(final String nameKey, final Icon icon) {
    init(getClass(), nameKey, nameKey, nameKey, icon);
  }

  public I18nAction(final String nameKey, final String shortDescriptionKey) {
    init(getClass(), nameKey, nameKey, shortDescriptionKey, null);
  }

  public I18nAction(final String nameKey, final String shortDescriptionKey, final Icon icon) {
    init(getClass(), nameKey, nameKey, shortDescriptionKey, icon);
  }

  public I18nAction(final String actionCommand, final String nameKey,
    final String shortDescriptionKey, final Icon icon) {
    init(getClass(), actionCommand, nameKey, shortDescriptionKey, icon);
  }

  @Override
  public void actionPerformed(final ActionEvent e) {
  }

  private void init(final Class<?> clazz, final String actionCommand, final String nameKey,
    final String shortDescriptionKey, final Icon icon) {
    putValue(ACTION_COMMAND_KEY, actionCommand);
    if (nameKey != null) {
      final CharSequence name = I18n.getCharSequence(clazz, nameKey);
      setName(name);
    }
    if (shortDescriptionKey != null) {
      final CharSequence shortDescription = I18n.getCharSequence(clazz, shortDescriptionKey);
      setShortDescription(shortDescription);
    }
    putValue(SMALL_ICON, icon);
  }

  public void setName(final CharSequence name) {
    if (name != null) {
      putValue(NAME, name.toString());
      if (name instanceof I18nCharSequence) {
        final I18nCharSequence i18nName = (I18nCharSequence)name;
        i18nName.getI18n().addPropertyChangeListener("locale", new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            putValue(NAME, name.toString());
          }
        });
      }
    }
  }

  public void setShortDescription(final CharSequence shortDescription) {
    if (shortDescription != null) {
      putValue(SHORT_DESCRIPTION, shortDescription.toString());
      if (shortDescription instanceof I18nCharSequence) {
        final I18nCharSequence i18nName = (I18nCharSequence)shortDescription;
        i18nName.getI18n().addPropertyChangeListener("locale", new PropertyChangeListener() {
          @Override
          public void propertyChange(final PropertyChangeEvent evt) {
            putValue(SHORT_DESCRIPTION, shortDescription.toString());
          }
        });
      }
    }
  }

}
