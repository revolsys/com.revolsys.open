package com.revolsys.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.toolbar.ToolBar;

@SuppressWarnings("serial")
public class TablePanel extends JPanel implements MouseListener {

  private final ToolBar toolBar = new ToolBar();

  private final MenuFactory menu = new MenuFactory();

  private final JTable table;

  private int eventRow;

  public TablePanel(final JTable table) {
    super(new BorderLayout());
    this.table = table;

    add(toolBar, BorderLayout.NORTH);

    final JScrollPane scrollPane = new JScrollPane(table);
    table.addMouseListener(this);
    add(scrollPane, BorderLayout.CENTER);
  }

  public int getEventRow() {
    return eventRow;
  }

  public MenuFactory getMenu() {
    return menu;
  }

  public JTable getTable() {
    return table;
  }

  public ToolBar getToolBar() {
    return toolBar;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    setEventRow(e);
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    setEventRow(e);
    if (e.isPopupTrigger()) {
      final int x = e.getX();
      final int y = e.getY();
      final JPopupMenu popupMenu = menu.createJPopupMenu();
      final Component component = e.getComponent();
      popupMenu.show(component, x, y);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
  }

  protected void setEventRow(final MouseEvent e) {
    Point point = e.getPoint();
    eventRow = table.rowAtPoint(point);
    if (eventRow > -1) {
      eventRow = table.convertRowIndexToModel(eventRow);
    }
  }
}
