package com.revolsys.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.toolbar.ToolBar;

@SuppressWarnings("serial")
public class TablePanel extends JPanel implements MouseListener {
  private static Reference<MouseEvent> popupMouseEvent = new WeakReference<MouseEvent>(
    null);

  public static MouseEvent getPopupMouseEvent() {
    return popupMouseEvent.get();
  }

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

  private void doMenu(final MouseEvent e) {
    setEventRow(e);
    if (e.isPopupTrigger()) {
      popupMouseEvent = new WeakReference<MouseEvent>(e);
      final int x = e.getX();
      final int y = e.getY();
      final JPopupMenu popupMenu = menu.createJPopupMenu();
      final Component component = e.getComponent();
      popupMenu.show(component, x, y);
    }
  }

  public int getEventRow() {
    return eventRow;
  }

  public MenuFactory getMenu() {
    return menu;
  }

  @SuppressWarnings("unchecked")
  public <T extends JTable> T getTable() {
    return (T)table;
  }

  @SuppressWarnings("unchecked")
  public <T extends TableModel> T getTableModel() {
    return (T)table.getModel();
  }

  public ToolBar getToolBar() {
    return toolBar;
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
  public void mousePressed(final MouseEvent e) {
    doMenu(e);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    doMenu(e);
  }

  protected void setEventRow(final MouseEvent e) {
    final Point point = e.getPoint();
    eventRow = table.rowAtPoint(point);
    if (eventRow > -1) {
      eventRow = table.convertRowIndexToModel(eventRow);
    }
  }
}
