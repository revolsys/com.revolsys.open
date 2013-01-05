package com.revolsys.swing.component;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;
import javax.swing.SpringLayout;

import com.revolsys.swing.layout.SpringLayoutUtil;

public class TogglePanel extends ValuePanel<CharSequence> implements
  ItemListener {
  /**
   * 
   */
  private static final long serialVersionUID = -1157521301167776836L;

  private final ButtonGroup group;

  public TogglePanel(final CharSequence value, final Action... actions) {
    this(value, null, actions);
  }

  public TogglePanel(final CharSequence value, final Dimension dimension,
    final Action... actions) {
    super(new SpringLayout());
    setValue(value);
    group = new ButtonGroup();
    for (final Action action : actions) {
      final JToggleButton button = new JToggleButton(action);
      if (dimension != null) {
        button.setPreferredSize(dimension);
      }
      group.add(button);
      add(button);
      final Object actionCommand = action.getValue(Action.ACTION_COMMAND_KEY);
      if (value != null && value.equals(actionCommand)) {
        button.setSelected(true);
      }
      button.addItemListener(this);
    }
    SpringLayoutUtil.makeColumns(this, getComponentCount(), 0, 0, 5, 0);
  }

  public CharSequence getActionCommand() {
    return group.getSelection().getActionCommand();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      firePropertyChange("actionCommand", null, getActionCommand());
    }

  }

  @Override
  public void save() {
    super.save();
    setValue(getActionCommand());
  }
}
