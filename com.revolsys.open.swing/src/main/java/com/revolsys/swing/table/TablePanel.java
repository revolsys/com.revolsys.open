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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.swing.action.enablecheck.ObjectPropertyEnableCheck;
import com.revolsys.swing.dnd.ClipboardUtil;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.MenuSourceHolder;
import com.revolsys.swing.table.lambda.column.ColumnBasedTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class TablePanel extends JPanel implements MouseListener, Closeable {
  protected class TablePanelEventSource extends MenuSourceHolder {
    private int row;

    private int column;

    private TablePanelEventSource(final int column, final MouseEvent event, final Object source) {
      super(source, event);
      this.row = -1;
      this.column = column;
    }

    protected TablePanelEventSource(final MouseEvent event, final Object source) {
      super(source, event);
      final BaseJTable table = TablePanel.this.table;
      final Point point = event.getLocationOnScreen();
      SwingUtilities.convertPointFromScreen(point, table);
      this.row = table.rowAtPoint(point);
      if (this.row > -1) {
        this.row = table.convertRowIndexToModel(this.row);
      }
      this.column = table.columnAtPoint(point);
      if (this.column > -1) {

        this.column = table.convertColumnIndexToModel(this.column);
      }
      if (event.getButton() == MouseEvent.BUTTON3) {
        if (table.isEditing()) {
          table.getCellEditor().stopCellEditing();
        }
      }
    }

    public TablePanelEventSource(final Object source) {
      super(source);
      this.row = -1;
      this.column = -1;
    }

    public int getColumn() {
      return this.column;
    }

    public int getRow() {
      return this.row;
    }

    @SuppressWarnings("unchecked")
    private <T extends BaseJTable> T getTable() {
      return (T)TablePanel.this.table;
    }
  }

  private static final long serialVersionUID = 1L;

  public static int getEventColumn() {
    final TablePanelEventSource holder = getEventHolder();
    if (holder == null) {
      return -1;
    } else {
      return holder.column;
    }
  }

  private static TablePanelEventSource getEventHolder() {
    final MenuSourceHolder menuSourceHolder = MenuFactory.getMenuSourceHolder();
    if (menuSourceHolder instanceof TablePanelEventSource) {
      return (TablePanelEventSource)menuSourceHolder;

    }
    return null;
  }

  public static int getEventRow() {
    final TablePanelEventSource holder = getEventHolder();
    if (holder == null) {
      return -1;
    } else {
      return holder.row;
    }
  }

  public static <V extends BaseJTable> V getEventTable() {
    final TablePanelEventSource holder = getEventHolder();
    if (holder == null) {
      return null;
    } else {
      return holder.getTable();
    }
  }

  public static MouseEvent getPopupMouseEvent() {
    final TablePanelEventSource holder = getEventHolder();
    if (holder == null) {
      return null;
    } else {
      return holder.getEvent();
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
    final int eventRow = getEventRow();
    final int eventColumn = getEventColumn();
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
      final int eventRow = getEventRow();
      final int eventColumn = getEventColumn();
      tableModel.setValueAt(null, eventRow, eventColumn);
    }
  }

  protected void doHeaderMenu(final MouseEvent e) {
    final Object source = e.getSource();
    final JTableHeader tableHeader = this.table.getTableHeader();
    if (e.isPopupTrigger() && source == tableHeader) {
      e.consume();
      final Point point = e.getPoint();
      final int eventColumn = tableHeader.columnAtPoint(point);
      if (eventColumn > -1) {
        final int column = this.table.convertColumnIndexToModel(eventColumn);
        final Object menuSource = getHeaderMenuSource();
        try (
          final TablePanelEventSource menuSourceHolder = new TablePanelEventSource(column, e,
            menuSource)) {
          if (column > -1) {
            final TableCellEditor cellEditor = this.table.getCellEditor();
            if (cellEditor != null) {
              cellEditor.stopCellEditing();
            }

            BaseJPopupMenu.showMenu(() -> getHeaderMenu(column), menuSourceHolder, this.table, e);
          }
        }
      }

    }
  }

  protected void doMenu(final MouseEvent e) {
    if (e.isPopupTrigger()) {
      final Object menuSource = getMenuSource();
      try (
        final TablePanelEventSource menuSourceHolder = new TablePanelEventSource(e, menuSource)) {
        final int eventRow = menuSourceHolder.getRow();
        final int eventColumn = menuSourceHolder.getColumn();
        if (eventRow > -1) {
          e.consume();
          final AbstractTableModel tableModel = getTableModel();
          final BaseJPopupMenu menu = tableModel.getMenu(eventRow, eventColumn);
          if (menu != null) {
            final TableCellEditor cellEditor = this.table.getCellEditor();
            if (cellEditor == null || cellEditor.stopCellEditing()) {
              final Component component = e.getComponent();
              if (component == this.table) {
                final int x = e.getX();
                final int y = e.getY();
                menu.showMenu(menuSourceHolder, this.table, x + 5, y);
              } else {
                final int xOnScreen = e.getXOnScreen();
                final int yOnScreen = e.getYOnScreen();
                final Point locationOnScreen = getLocationOnScreen();
                final int x = xOnScreen - locationOnScreen.x;
                final int y = yOnScreen - locationOnScreen.y;
                menu.showMenu(menuSourceHolder, this, x + 5, y);
              }
            }
          }
        }
      }
    }
  }

  public MenuFactory getHeaderMenu() {
    return this.headerMenu;
  }

  protected BaseJPopupMenu getHeaderMenu(final int eventColumn) {
    final MenuFactory menuFactory = getHeaderMenuFactory(eventColumn);
    if (menuFactory == null) {
      return null;
    } else {
      return menuFactory.newJPopupMenu();
    }
  }

  public MenuFactory getHeaderMenuFactory(final int eventColumn) {
    final AbstractTableModel tableModel = getTableModel();
    if (tableModel != null) {
      final MenuFactory menuFactory = tableModel.getHeaderMenuFactory(eventColumn);
      if (menuFactory != null) {
        return menuFactory;
      }
    }
    return this.headerMenu;
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
    final int eventRow = getEventRow();
    final int eventColumn = getEventColumn();
    final BaseJTable eventTable = getEventTable();
    if (eventTable == this.table && eventRow > -1 && eventColumn > -1) {
      return getTableModel().isCellEditable(eventRow, eventColumn);
    }
    return false;
  }

  public boolean isCurrentCellHasValue() {
    final int eventRow = getEventRow();
    final int eventColumn = getEventColumn();
    final BaseJTable eventTable = getEventTable();
    if (isEditingCurrentCell()) {
      return true;
    } else if (eventRow > -1 && eventColumn > -1 && eventTable == this.table) {
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
      final int eventRow = getEventRow();
      final int eventColumn = getEventColumn();
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
      final int eventRow = getEventRow();
      final int eventColumn = getEventColumn();
      if (tableModel.isCellEditable(eventRow, eventColumn)) {
        tableModel.setValueAt(value, eventRow, eventColumn);
      }
    }
  }

}
