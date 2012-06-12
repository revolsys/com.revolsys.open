package com.revolsys.swing.toolbar;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

public class ActionToolBarButtonFactory extends AbstractAction implements
  ToolBarButtonFactory {
  private static final List<String> KEYS = Arrays.asList(
    Action.ACCELERATOR_KEY, Action.ACTION_COMMAND_KEY, Action.DEFAULT,
    Action.DISPLAYED_MNEMONIC_INDEX_KEY, Action.LARGE_ICON_KEY,
    Action.LONG_DESCRIPTION, Action.MNEMONIC_KEY, Action.NAME,
    Action.SELECTED_KEY, Action.SHORT_DESCRIPTION, Action.SMALL_ICON);

  private static final long serialVersionUID = -5626990626102421865L;

  private boolean checkBox;

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
    if (checkBox) {
      return new JToggleButton(this);
    } else {
      return new JButton(this);
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

  public boolean isCheckBox() {
    return checkBox;
  }

  protected void setCheckBox(final boolean checkBox) {
    this.checkBox = checkBox;
  }

  protected void setIcon(final Icon icon) {
    putValue(Action.SMALL_ICON, icon);
  }

  protected void setName(final String name) {
    putValue(Action.NAME, name);
  }

  protected void setToolTip(final String toolTip) {
    putValue(Action.SHORT_DESCRIPTION, toolTip);
  }
}
