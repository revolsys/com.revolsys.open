package com.revolsys.swing.table.editor;

import java.awt.Component;
import java.awt.event.KeyAdapter;
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
import javax.swing.table.TableModel;

import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.type.DataType;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.listener.MouseListeners;
import com.revolsys.swing.listener.MouseListenersBase;
import com.revolsys.swing.menu.BaseJPopupMenu;
import com.revolsys.swing.menu.ShowMenuMouseListener;
import com.revolsys.swing.table.AbstractTableModel;
import com.revolsys.swing.table.BaseJTable;

public class BaseTableCellEditor extends AbstractCellEditor
  implements TableCellEditor, TableModelListener {

  private static final long serialVersionUID = 1L;

  protected int columnIndex;

  protected DataType dataType = DataTypes.OBJECT;

  private boolean editing = false;

  protected JComponent editorComponent;

  protected KeyListener keyListener = new KeyAdapter() {
    @Override
    public void keyPressed(final KeyEvent event) {
      if (isEditing()) {
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
    }

  };

  protected final MouseListeners mouseListeners = new MouseListenersBase();

  protected Callable<BaseJPopupMenu> popupMenuFactory;

  protected ShowMenuMouseListener popupMenuListener;

  protected int rowIndex;

  protected BaseJTable table;

  public BaseTableCellEditor() {
  }

  public BaseTableCellEditor(final BaseJTable table) {
    setTable(table);
  }

  public synchronized void addMouseListener(final MouseListener listener) {
    this.mouseListeners.addMouseListener(listener);
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
    final int rowIndex = this.table.convertRowIndexToView(this.rowIndex) + rowDelta;
    if (rowIndex >= 0 && rowIndex <= this.table.getRowCount()) {
      final int columnIndex = this.table.convertColumnIndexToView(this.columnIndex) + columnDelta;
      if (columnIndex >= 0 && columnIndex < this.table.getColumnCount()) {
        this.table.editCellAt(rowIndex, columnIndex);
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

  @SuppressWarnings("rawtypes")
  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, final int rowIndex, final int columnIndex) {
    startEditing(rowIndex, columnIndex);

    final AbstractTableModel model = (AbstractTableModel)table.getModel();

    this.editorComponent = model.getEditorField(this.rowIndex, this.columnIndex, value);
    if (this.editorComponent instanceof JTextField) {
      final JTextField textField = (JTextField)this.editorComponent;
      textField.setBorder(
        BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WebColors.LightSteelBlue),
          BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    }
    this.editorComponent.setOpaque(false);
    SwingUtil.setFieldValue(this.editorComponent, value);

    this.editorComponent.addKeyListener(this.keyListener);
    this.editorComponent.addMouseListener(this.mouseListeners);
    if (this.editorComponent instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)this.editorComponent;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component comboEditorComponent = editor.getEditorComponent();
      comboEditorComponent.addKeyListener(this.keyListener);
      comboEditorComponent.addMouseListener(this.mouseListeners);
    }
    return this.editorComponent;
  }

  protected TableModel getTableModel() {
    return this.table.getModel();
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

  public boolean isEditing() {
    return this.editing;
  }

  public synchronized void removeMouseListener(final MouseListener listener) {
    this.mouseListeners.removeMouseListener(listener);
  }

  public void setPopupMenu(final Callable<BaseJPopupMenu> popupMenuFactory) {
    this.popupMenuFactory = popupMenuFactory;
  }

  public void setTable(final BaseJTable table) {
    this.table = table;
    final TableModel model = table.getModel();
    model.addTableModelListener(this);
  }

  protected void startEditing(final int rowIndex, final int columnIndex) {
    this.editing = true;
    this.rowIndex = this.table.convertRowIndexToModel(rowIndex);
    this.columnIndex = this.table.convertColumnIndexToModel(columnIndex);
  }

  @Override
  public boolean stopCellEditing() {
    if (isEditing()) {
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
            this.editorComponent.removeKeyListener(this.keyListener);
            if (this.editorComponent instanceof JComboBox) {
              final JComboBox<?> comboBox = (JComboBox<?>)this.editorComponent;
              final ComboBoxEditor editor = comboBox.getEditor();
              final Component comboEditorComponent = editor.getEditorComponent();
              comboEditorComponent.removeKeyListener(this.keyListener);
              comboEditorComponent.removeMouseListener(this.mouseListeners);
            }
          }
        }
      }
      if (stopped) {
        this.editing = false;
      }
      return stopped;
    } else {
      return false;
    }
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
