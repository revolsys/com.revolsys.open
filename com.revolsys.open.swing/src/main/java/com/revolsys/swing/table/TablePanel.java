package com.revolsys.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import com.revolsys.collection.EmptyReference;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class TablePanel extends JPanel implements MouseListener {
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
      final int eventRow = table.rowAtPoint(point);
      final int eventColumn = table.columnAtPoint(point);
      if (eventRow > -1 && eventColumn > -1) {
        TablePanel.eventRow = table.convertRowIndexToModel(eventRow);
        TablePanel.eventColumn = table.convertColumnIndexToModel(eventColumn);

        if (e.getButton() == MouseEvent.BUTTON3) {
          // table.getSelectionModel().setSelectionInterval(eventRow, eventRow);
          if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
          }
        }
      }
    }
  }

  private static int eventColumn;

  private static int eventRow;

  private static Reference<JTable> eventTable = new WeakReference<JTable>(null);

  private static Reference<MouseEvent> popupMouseEvent = new WeakReference<MouseEvent>(null);

  private static final long serialVersionUID = 1L;

  private JTable table;

  private ToolBar toolBar = new ToolBar();

  private JScrollPane scrollPane;

  public TablePanel(final JTable table) {
    super(new BorderLayout());
    eventRow = -1;
    eventColumn = -1;
    eventTable = new EmptyReference<>();
    popupMouseEvent = new EmptyReference<>();
    this.table = table;
    final AbstractTableModel model = (AbstractTableModel)table.getModel();
    add(this.toolBar, BorderLayout.NORTH);

    this.scrollPane = new JScrollPane(table);
    table.addMouseListener(this);
    add(this.scrollPane, BorderLayout.CENTER);

    final MenuFactory menu = model.getMenu();

    menu.addMenuItemTitleIcon("dataTransfer", "Copy Field Value", "page_copy",
      new ObjectPropertyEnableCheck(this, "canCopy"), this, "copyFieldValue");

    menu.addMenuItemTitleIcon("dataTransfer", "Cut Field Value", "cut",
      new ObjectPropertyEnableCheck(this, "canCut"), this, "cutFieldValue");

    menu.addMenuItemTitleIcon("dataTransfer", "Paste Field Value", "paste_plain",
      new ObjectPropertyEnableCheck(this, "canPaste"), this, "pasteFieldValue");
  }

  private void copyCurrentCell() {
    final TableModel model = getTableModel();
    final Object value = model.getValueAt(eventRow, eventColumn);

    final String copyValue;
    if (model instanceof AbstractTableModel) {
      final AbstractTableModel tableModel = (AbstractTableModel)model;
      copyValue = tableModel.toCopyValue(eventRow, eventColumn, value);
    } else {
      copyValue = StringConverterRegistry.toString(value);
    }
    final StringSelection transferable = new StringSelection(copyValue);
    ClipboardUtil.setContents(transferable);
  }

  public void copyFieldValue() {
    if (isEditingCurrentCell()) {
      if (!this.table.getCellEditor().stopCellEditing()) {
        return;
      }
    }
    copyCurrentCell();
  }

  public void cutFieldValue() {
    if (isEditingCurrentCell()) {
      if (!this.table.getCellEditor().stopCellEditing()) {
        return;
      }
    }
    copyCurrentCell();
    if (isCurrentCellEditable()) {
      final TableModel tableModel = getTableModel();
      tableModel.setValueAt(null, eventRow, eventColumn);
    }
  }

  private void doMenu(final MouseEvent e) {
    setEventRow(this.table, e);
    if (eventRow > -1 && e.isPopupTrigger()) {
      e.consume();
      final MenuFactory menu = getTableModel().getMenu(eventRow, eventColumn);
      if (menu != null) {
        popupMouseEvent = new WeakReference<MouseEvent>(e);
        final int x = e.getX();
        final int y = e.getY();

        final Component component = e.getComponent();
        menu.show(getMenuSource(), component, x + 5, y);
      }
    }
  }

  protected Object getMenuSource() {
    return null;
  }

  public JScrollPane getScrollPane() {
    return this.scrollPane;
  }

  @SuppressWarnings("unchecked")
  public <T extends JTable> T getTable() {
    return (T)this.table;
  }

  @SuppressWarnings("unchecked")
  public <T extends AbstractTableModel> T getTableModel() {
    return (T)this.table.getModel();
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public boolean isCanCopy() {
    return isEditingCurrentCell() || isCurrentCellHasValue();
  }

  public boolean isCanCut() {
    return isEditingCurrentCell() || isCurrentCellHasValue() && isCurrentCellEditable();
  }

  public boolean isCanPaste() {
    if (isEditingCurrentCell()) {
      return true;
    } else if (isCurrentCellEditable()) {
      final String value = ClipboardUtil.getContents(DataFlavor.stringFlavor);
      return Property.hasValue(value);
    }
    return false;
  }

  public boolean isCurrentCellEditable() {
    if (eventRow > -1 && eventColumn > -1) {
      return getTableModel().isCellEditable(eventRow, eventColumn);
    }
    return false;
  }

  public boolean isCurrentCellHasValue() {
    if (isEditingCurrentCell()) {
      return true;
    } else if (eventRow > -1 && eventColumn > -1) {
      final TableModel tableModel = getTableModel();
      final Object value = tableModel.getValueAt(eventRow, eventColumn);
      return Property.hasValue(value);
    }
    return false;
  }

  public boolean isEditing() {
    return this.table.isEditing();
  }

  public boolean isEditingCurrentCell() {
    if (isEditing()) {
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
    setEventRow(this.table, e);
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

  public void pasteFieldValue() {
    if (isEditingCurrentCell()) {
      if (!this.table.getCellEditor().stopCellEditing()) {
        return;
      }
    }
    final String value = ClipboardUtil.getContents(DataFlavor.stringFlavor);
    if (Property.hasValue(value)) {
      final TableModel tableModel = getTableModel();
      if (tableModel.isCellEditable(eventRow, eventColumn)) {
        tableModel.setValueAt(value, eventRow, eventColumn);
      }
    }
  }

  @Override
  public void removeNotify() {
    if (this.scrollPane != null) {
      remove(this.scrollPane);
      this.scrollPane = null;
    }
    if (this.table != null) {
      this.table.removeMouseListener(this);
      final AbstractTableModel model = getTableModel();
      model.dispose();
    }
    this.table = null;
    if (this.toolBar != null) {
      remove(this.toolBar);
      this.toolBar = null;
    }
    super.removeNotify();
  }

}
