package com.revolsys.swing.parallel;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.menu.MenuFactory;

public class BackgroundTaskTabLabel extends JPanel implements MouseListener, TableModelListener {
  private static final long serialVersionUID = 1L;

  private static final Icon ANIMATED = Icons.getAnimatedIcon("error_animated.gif");

  static final Icon STATIC = Icons.getIcon("time");

  private final JTabbedPane tabs;

  private final BackgroundTaskTableModel tableModel;

  private final transient MenuFactory menuFactory;

  private final JLabel doneLabel = new JLabel();

  private final JLabel iconLabel = new JLabel();

  public BackgroundTaskTabLabel(final JTabbedPane tabs, final BackgroundTaskTableModel tableModel) {
    this.tabs = tabs;
    this.tableModel = tableModel;
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 1));
    setOpaque(false);
    addMouseListener(this);
    updateLabel();
    this.iconLabel.setIcon(STATIC);
    this.doneLabel.setOpaque(true);
    this.doneLabel.setFont(SwingUtil.BOLD_FONT);
    this.doneLabel.setBackground(WebColors.Green);
    this.doneLabel.setForeground(WebColors.HoneyDew);
    this.doneLabel.setMinimumSize(new Dimension(8, 16));
    this.doneLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

    add(this.iconLabel);
    add(this.doneLabel);
    GroupLayouts.makeColumns(this, false);
    this.menuFactory = new MenuFactory();
    this.menuFactory.addMenuItem("default", "Clear done tasks", "delete",
      tableModel::isHasDoneTasks, tableModel::clearDoneTasks);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    this.tableModel.addTableModelListener(this);
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent event) {
    if (event.isPopupTrigger()) {
      showMenu(event);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (this.tableModel != null) {
      updateLabel();
    }
    if (event.isPopupTrigger()) {
      showMenu(event);
    } else if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      final int tabIndex = this.tabs.indexOfTabComponent(this);
      if (tabIndex != -1) {
        this.tabs.setSelectedIndex(tabIndex);
      }
    }
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    this.tableModel.removeTableModelListener(this);
  }

  private void showMenu(final MouseEvent event) {
    this.menuFactory.showMenu(this, event);
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    updateLabel();
    this.tabs.repaint();
  }

  private void updateLabel() {
    final int doneCount = this.tableModel.getDoneCount();
    if (doneCount == 0) {
      this.doneLabel.setVisible(false);
    } else {
      final String text = Integer.toString(doneCount);
      this.doneLabel.setText(text);
      this.doneLabel.setVisible(true);
    }
  }
}
