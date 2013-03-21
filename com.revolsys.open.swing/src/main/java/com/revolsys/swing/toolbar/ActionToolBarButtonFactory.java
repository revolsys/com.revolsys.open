package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

import com.revolsys.swing.action.AbstractAction;

public class ActionToolBarButtonFactory extends AbstractAction implements
  ToolBarButtonFactory {
  private static final List<String> KEYS = Arrays.asList(
    Action.ACCELERATOR_KEY, Action.ACTION_COMMAND_KEY, Action.DEFAULT,
    Action.DISPLAYED_MNEMONIC_INDEX_KEY, Action.LARGE_ICON_KEY,
    Action.LONG_DESCRIPTION, Action.MNEMONIC_KEY, Action.NAME,
    Action.SELECTED_KEY, Action.SHORT_DESCRIPTION, Action.SMALL_ICON);

  private static final long serialVersionUID = -5626990626102421865L;

  private ActionListener actionListener;

  public ActionToolBarButtonFactory() {
    this.actionListener = this;
  }

  public ActionToolBarButtonFactory(final Action action) {
    for (final String key : KEYS) {
      final Object value = action.getValue(key);
      putValue(key, value);
    }
    this.actionListener = action;
  }

  public ActionToolBarButtonFactory(final Action action,
    final ActionListener actionListener) {
    this(action);
    this.actionListener = actionListener;
  }

  public ActionToolBarButtonFactory(final ActionListener actionListener) {
    this.actionListener = actionListener;
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    actionListener.actionPerformed(event);

  }

  @Override
  public Component createToolbarButton() {
    if (isCheckBox()) {
      return new JToggleButton(this);
    } else {
      return new JButton(this);
    }
  }

}
