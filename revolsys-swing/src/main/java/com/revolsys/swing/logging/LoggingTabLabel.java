package com.revolsys.swing.logging;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.menu.MenuFactory;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class LoggingTabLabel extends JLabel implements MouseListener, TableModelListener {
  private static final long serialVersionUID = 1L;

  private static final Icon ANIMATED = Icons.getAnimatedIcon("error_animated.gif");

  private static final Icon STATIC = Icons.getIcon("error");

  private final JTabbedPane tabs;

  private final LoggingTableModel tableModel;

  private final MenuFactory menuFactory;

  public LoggingTabLabel(final JTabbedPane tabs, final LoggingTableModel tableModel) {
    this.tabs = tabs;
    this.tableModel = tableModel;
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 1));
    setOpaque(false);
    addMouseListener(this);
    updateLabel();
    this.menuFactory = new MenuFactory();
    this.menuFactory.addMenuItem("default", "Delete all messages", "delete",
      tableModel::isHasMessages, tableModel::clear);
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
      this.tableModel.clearHasNewErrors();
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
    String text = null;
    final int messageCount = this.tableModel.getMessageCount();
    if (messageCount != 0) {
      text = Integer.toString(messageCount);
    }
    if (this.tableModel.isHasNewErrors()) {
      setFont(SwingUtil.BOLD_FONT);
      setForeground(WebColors.Red);
      setIcon(ANIMATED);
    } else {
      setFont(SwingUtil.FONT);
      setForeground(WebColors.Black);
      setIcon(STATIC);
    }
    setText(text);
  }
}
