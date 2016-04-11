package com.revolsys.swing.table.record.editor;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;
import java.util.concurrent.Callable;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;

import com.revolsys.awt.WebColors;
import com.revolsys.datatype.DataType;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.AbstractRecordQueryField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.listener.MouseListeners;
import com.revolsys.swing.listener.MouseListenersBase;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.ShowMenuMouseListener;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.record.model.AbstractRecordTableModel;

public class RecordTableCellEditor extends AbstractCellEditor
  implements TableCellEditor, KeyListener, TableModelListener {

  private static final long serialVersionUID = 1L;

  private int columnIndex;

  private DataType dataType;

  private JComponent editorComponent;

  private String fieldName;

  private final MouseListeners mouseListeners = new MouseListenersBase();

  private Object oldValue;

  private Callable<BaseJPopupMenu> popupMenuFactory = null;

  private int rowIndex;

  private BaseJTable table;

  private ShowMenuMouseListener popupMenuListener;

  public RecordTableCellEditor(final BaseJTable table) {
    this.table = table;
    table.getModel().addTableModelListener(this);
  }

  public synchronized void addMouseListener(final MouseListener l) {
    this.mouseListeners.addMouseListener(l);
  }

  public void close() {
    this.editorComponent = null;
    this.mouseListeners.clearMouseListeners();
    this.popupMenuFactory = null;
    this.popupMenuListener = null;
    this.table = null;
    for (final CellEditorListener listener : getCellEditorListeners()) {
      removeCellEditorListener(listener);
    }
  }

  private void editCellRelative(final int rowDelta, final int columnDelta) {
    final int rowIndex = this.table.convertRowIndexToModel(this.rowIndex) + rowDelta;
    if (rowIndex >= 0 && rowIndex <= this.table.getRowCount()) {
      final int columnIndex = this.table.convertColumnIndexToModel(this.columnIndex) + columnDelta;
      if (columnIndex >= 0 && columnIndex < this.table.getColumnCount()) {
        this.table.editCell(rowIndex, columnIndex);
      }
    }
  }

  @Override
  public Object getCellEditorValue() {
    final Object value = SwingUtil.getValue(this.editorComponent);
    return value;
  }

  public JComponent getEditorComponent() {
    return this.editorComponent;
  }

  public String getFieldName() {
    return this.fieldName;
  }

  public Object getOldValue() {
    return this.oldValue;
  }

  protected RecordDefinition getRecordDefinition() {
    final AbstractRecordTableModel tableModel = getTableModel();
    return tableModel.getRecordDefinition();
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, int rowIndex, int columnIndex) {
    rowIndex = table.convertRowIndexToModel(rowIndex);
    columnIndex = table.convertColumnIndexToModel(columnIndex);
    this.oldValue = value;
    final AbstractRecordTableModel model = getTableModel();
    this.fieldName = model.getFieldName(rowIndex, columnIndex);
    final RecordDefinition recordDefinition = model.getRecordDefinition();
    this.dataType = recordDefinition.getFieldType(this.fieldName);
    final Field field = newField(this.fieldName);
    this.editorComponent = (JComponent)field;
    if (this.editorComponent instanceof JTextField) {
      final JTextField textField = (JTextField)this.editorComponent;
      textField.setBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WebColors.LightSteelBlue),
          BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    } else if (this.editorComponent instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)this.editorComponent;
      queryField.setSearchFieldBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WebColors.LightSteelBlue),
          BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    }
    this.editorComponent.setOpaque(false);
    SwingUtil.setFieldValue(this.editorComponent, value);

    this.rowIndex = rowIndex;
    this.columnIndex = columnIndex;
    this.editorComponent.addKeyListener(this);
    this.editorComponent.addMouseListener(this.mouseListeners);
    if (this.editorComponent instanceof JComboBox) {
      final JComboBox<?> comboBox = (JComboBox<?>)this.editorComponent;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component comboEditorComponent = editor.getEditorComponent();
      comboEditorComponent.addKeyListener(this);
      comboEditorComponent.addMouseListener(this.mouseListeners);
    } else if (this.editorComponent instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)this.editorComponent;
      final TextField searchField = queryField.getSearchField();
      searchField.addKeyListener(this);
      searchField.addMouseListener(this.mouseListeners);
    }
    this.popupMenuListener = ShowMenuMouseListener.addListener(this.editorComponent,
      this.popupMenuFactory);
    return this.editorComponent;
  }

  protected AbstractRecordTableModel getTableModel() {
    return (AbstractRecordTableModel)this.table.getModel();
  }

  @Override
  public boolean isCellEditable(final EventObject event) {
    if (event == null) {
      return true;
    } else {
      if (event instanceof MouseEvent) {
        final MouseEvent mouseEvent = (MouseEvent)event;
        if (SwingUtil.isLeftButtonAndNoModifiers(mouseEvent)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public void keyPressed(final KeyEvent event) {
    final int keyCode = event.getKeyCode();
    if (keyCode == KeyEvent.VK_ENTER) {
      stopCellEditing();

      if (SwingUtil.isShiftDown(event)) {
        editCellRelative(-1, 0);
      } else {
        editCellRelative(1, 0);
      }
      event.consume();
    } else if (keyCode == KeyEvent.VK_TAB) {
      stopCellEditing();
      if (SwingUtil.isShiftDown(event)) {
        editCellRelative(0, -1);
      } else {
        editCellRelative(0, 1);
      }
      event.consume();
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  protected Field newField(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return SwingUtil.newField(recordDefinition, fieldName, true);
  }

  public synchronized void removeMouseListener(final MouseListener listener) {
    this.mouseListeners.removeMouseListener(listener);
  }

  public void setPopupMenu(final Callable<BaseJPopupMenu> popupMenuFactory) {
    this.popupMenuFactory = popupMenuFactory;
  }

  @Override
  public boolean stopCellEditing() {
    boolean stopped = false;
    try {
      if (this.editorComponent instanceof Field) {
        final Field field = (Field)this.editorComponent;
        field.updateFieldValue();
      }
      stopped = super.stopCellEditing();
    } catch (final IndexOutOfBoundsException e) {
      return true;
    } catch (final Throwable t) {
      final int result = JOptionPane.showConfirmDialog(this.editorComponent,
        "<html><p><b>'" + getCellEditorValue() + "' is not a valid "
          + this.dataType.getValidationName()
          + ".</b></p><p>Discard changes (Yes) or edit field (No).</p></html>",
        "Invalid value", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        cancelCellEditing();
        return true;
      } else {
        return false;
      }
    } finally {
      if (stopped) {
        if (this.editorComponent != null) {
          this.editorComponent.removeMouseListener(this.mouseListeners);
          this.editorComponent.removeKeyListener(this);
          if (this.editorComponent instanceof JComboBox) {
            final JComboBox<?> comboBox = (JComboBox<?>)this.editorComponent;
            final ComboBoxEditor editor = comboBox.getEditor();
            final Component comboEditorComponent = editor.getEditorComponent();
            comboEditorComponent.removeKeyListener(this);
            comboEditorComponent.removeMouseListener(this.mouseListeners);
          } else if (this.editorComponent instanceof AbstractRecordQueryField) {
            final AbstractRecordQueryField queryField = (AbstractRecordQueryField)this.editorComponent;
            final TextField searchField = queryField.getSearchField();
            searchField.removeKeyListener(this);
            searchField.removeMouseListener(this.mouseListeners);
          }
          if (this.popupMenuListener != null) {
            this.popupMenuListener.close();
            this.popupMenuListener = null;
          }
        }
      }
    }
    return stopped;
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    if (e.getFirstRow() <= this.rowIndex && this.rowIndex <= e.getLastRow()) {
      if (e.getColumn() == TableModelEvent.ALL_COLUMNS) {
        cancelCellEditing();
      }
    }
  }
}
