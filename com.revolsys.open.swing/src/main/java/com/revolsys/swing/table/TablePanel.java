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

public class TablePanel extends JPanel implements MouseListener {
  private static int eventColumn;

  private static int eventRow;

  private static Reference<JTable> eventTable = new WeakReference<JTable>(null);

  private static Reference<MouseEvent> popupMouseEvent = new WeakReference<MouseEvent>(
    null);

  private static final long serialVersionUID = 1L;

  public static int getEventColumn() {
    return eventColumn;
  }

  public static int getEventRow() {
    return eventRow;
  }

  @SuppressWarnings("unchecked")
  public static <V extends JTable> V getEventTable() {
    return (V)eventTable.get();
  }

  public static MouseEvent getPopupMouseEvent() {
    return popupMouseEvent.get();
  }

  protected static void setEventRow(final JTable table, final MouseEvent e) {
    if (e.getSource() == table) {
      final Point point = e.getPoint();
      eventTable = new WeakReference<JTable>(table);
      eventRow = table.rowAtPoint(point);
      eventColumn = table.columnAtPoint(point);
      if (eventRow > -1) {
        eventRow = table.convertRowIndexToModel(eventRow);
      }
      if (eventColumn > -1) {
        eventColumn = table.convertColumnIndexToModel(eventColumn);
      }
    }
  }

  private final MenuFactory menu = new MenuFactory();

  private final JTable table;

  private final ToolBar toolBar = new ToolBar();

  private final JScrollPane scrollPane;

  public TablePanel(final JTable table) {
    super(new BorderLayout());
    this.table = table;

    add(this.toolBar, BorderLayout.NORTH);

    scrollPane = new JScrollPane(table);
    table.addMouseListener(this);
    add(scrollPane, BorderLayout.CENTER);
  }

  private void doMenu(final MouseEvent e) {
    setEventRow(this.table, e);
    if (eventRow > -1 && e.isPopupTrigger()) {
      e.consume();
      popupMouseEvent = new WeakReference<MouseEvent>(e);
      final int x = e.getX();
      final int y = e.getY();
      final JPopupMenu popupMenu = this.menu.createJPopupMenu();
      final Component component = e.getComponent();
      popupMenu.show(component, x, y);
    }
  }

  public MenuFactory getMenu() {
    return this.menu;
  }

  public JScrollPane getScrollPane() {
    return scrollPane;
  }

  @SuppressWarnings("unchecked")
  public <T extends JTable> T getTable() {
    return (T)this.table;
  }

  @SuppressWarnings("unchecked")
  public <T extends TableModel> T getTableModel() {
    return (T)this.table.getModel();
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public boolean isEditingCurrentCell() {
    if (this.table.isEditing()) {
      if (eventRow > -1 && eventRow == this.table.getEditingRow()) {
        if (eventColumn > -1 && eventColumn == this.table.getEditingColumn()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    setEventRow(table, e);
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
}
