package com.revolsys.swing.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.Closeable;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.collection.EmptyReference;
import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.table.lambda.column.ColumnBasedTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class TablePanel extends JPanel implements MouseListener, Closeable {
  private static int eventColumn;

  private static int eventRow;

  private static Reference<BaseJTable> eventTable = new WeakReference<>(null);

  private static Reference<MouseEvent> popupMouseEvent = new WeakReference<>(null);

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

  protected static void setEventRow(final BaseJTable table, final MouseEvent e) {
    if (e.getSource() == table) {
      final Point point = e.getPoint();
      eventTable = new WeakReference<>(table);
      final int eventRow = table.rowAtPoint(point);
      final int eventColumn = table.columnAtPoint(point);
      if (eventRow > -1 && eventColumn > -1) {
        TablePanel.eventRow = table.convertRowIndexToModel(eventRow);
        TablePanel.eventColumn = table.convertColumnIndexToModel(eventColumn);

        if (e.getButton() == MouseEvent.BUTTON3) {
          if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
          }
        }
      } else {
        TablePanel.eventRow = -1;
        TablePanel.eventColumn = -1;
      }
    }
  }

  protected static void setHeaderEventColumn(final BaseJTable table, final JTableHeader tableHeader,
    final MouseEvent e) {
    final Object source = e.getSource();
    if (source == tableHeader) {
      final Point point = e.getPoint();
      eventTable = new WeakReference<>(table);
      final int modelColumn = tableHeader.columnAtPoint(point);
      TablePanel.eventRow = -1;
      if (modelColumn > -1) {
        TablePanel.eventColumn = table.convertColumnIndexToModel(modelColumn);
      } else {
        TablePanel.eventColumn = -1;
      }
    }
  }

  private final MouseListener tableHeaderMouseListener = new MouseAdapter() {
    @Override
    public void mousePressed(final MouseEvent e) {
      doHeaderMenu(e);
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
      doHeaderMenu(e);
    }
  };

  protected JScrollPane scrollPane;

  private BaseJTable table;

  private ToolBar toolBar = new ToolBar();

  private final MenuFactory headerMenu = new MenuFactory(getClass().getName());

  public TablePanel(final AbstractTableModel tableModel) {
    this(new BaseJTable(tableModel));
  }

  public TablePanel(final BaseJTable table) {
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
    table.getTableHeader().addMouseListener(this.tableHeaderMouseListener);

    add(this.scrollPane, BorderLayout.CENTER);

    if (!(model instanceof ColumnBasedTableModel)) {
      final MenuFactory menu = model.getMenu();

      menu.addMenuItemTitleIcon("dataTransfer", "Copy Field Value", "page_copy",
        new ObjectPropertyEnableCheck(this, "canCopy"), this::copyFieldValue);

      menu.addMenuItemTitleIcon("dataTransfer", "Cut Field Value", "cut", this::isCanCut,
        this::cutFieldValue);

      menu.addMenuItemTitleIcon("dataTransfer", "Paste Field Value", "paste_plain",
        new ObjectPropertyEnableCheck(this, "canPaste"), this::pasteFieldValue);
    }
  }

  @Override
  public void close() {
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
  }

  private void copyCurrentCell() {
    final TableModel model = getTableModel();
    final Object value = model.getValueAt(eventRow, eventColumn);

    final String copyValue;
    if (model instanceof AbstractTableModel) {
      final AbstractTableModel tableModel = (AbstractTableModel)model;
      copyValue = tableModel.toCopyValue(eventRow, eventColumn, value);
    } else {
      copyValue = DataTypes.toString(value);
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

  protected void doHeaderMenu(final MouseEvent e) {
    setHeaderEventColumn(this.table, this.table.getTableHeader(), e);
    if (eventColumn > -1 && e.isPopupTrigger()) {
      e.consume();
      final Object menuSource = getHeaderMenuSource();
      MenuFactory.setMenuSource(menuSource);
      final JPopupMenu menu = getHeaderMenu(eventColumn);
      if (menu != null) {
        final TableCellEditor cellEditor = this.table.getCellEditor();
        if (cellEditor != null) {
          cellEditor.stopCellEditing();
        }
        popupMouseEvent = new WeakReference<>(e);
        final int x = e.getX();
        final int y = e.getY();
        MenuFactory.showMenu(menu, this.table, x, y);
      }
    }
  }

  protected void doMenu(final MouseEvent e) {
    setEventRow(this.table, e);
    if (eventRow > -1 && e.isPopupTrigger()) {
      e.consume();
      final AbstractTableModel tableModel = getTableModel();
      final Object menuSource = getMenuSource();
      MenuFactory.setMenuSource(menuSource);
      final JPopupMenu menu = tableModel.getMenu(eventRow, eventColumn);
      if (menu != null) {
        final TableCellEditor cellEditor = this.table.getCellEditor();
        if (cellEditor == null || cellEditor.stopCellEditing()) {
          popupMouseEvent = new WeakReference<>(e);
          final Component component = e.getComponent();
          if (component == this.table) {
            final int x = e.getX();
            final int y = e.getY();
            MenuFactory.showMenu(menu, this.table, x + 5, y);
          } else {
            final int xOnScreen = e.getXOnScreen();
            final int yOnScreen = e.getYOnScreen();
            final Point locationOnScreen = getLocationOnScreen();
            final int x = xOnScreen - locationOnScreen.x;
            final int y = yOnScreen - locationOnScreen.y;
            MenuFactory.showMenu(menu, this, x + 5, y);
          }
        }
      }
    }
  }

  public MenuFactory getHeaderMenu() {
    return this.headerMenu;
  }

  public JPopupMenu getHeaderMenu(final int eventColumn) {
    final AbstractTableModel tableModel = getTableModel();
    if (tableModel != null) {
      final JPopupMenu menu = tableModel.getHeaderMenu(eventColumn);
      if (menu != null) {
        return menu;
      }
    }
    return this.headerMenu.newJPopupMenu();
  }

  protected Object getHeaderMenuSource() {
    return null;
  }

  protected Object getMenuSource() {
    return null;
  }

  public JScrollPane getScrollPane() {
    return this.scrollPane;
  }

  @SuppressWarnings("unchecked")
  public <T extends BaseJTable> T getTable() {
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
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    setEventRow(this.table, e);
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

}
