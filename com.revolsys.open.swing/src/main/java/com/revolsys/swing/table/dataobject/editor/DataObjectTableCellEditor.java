package com.revolsys.swing.table.dataobject.editor;

import java.awt.AWTEventMulticaster;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.listener.Listener;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.menu.PopupMenu;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.model.AbstractDataObjectTableModel;

public class DataObjectTableCellEditor extends AbstractCellEditor implements
  TableCellEditor, KeyListener, MouseListener, TableModelListener {

  private static final long serialVersionUID = 1L;

  private JComponent editorComponent;

  private String attributeName;

  private final BaseJxTable table;

  private int rowIndex;

  private Object oldValue;

  private int columnIndex;

  private PopupMenu popupMenu = null;

  private DataType dataType;

  private MouseListener mouseListener;

  public DataObjectTableCellEditor(final BaseJxTable table) {
    this.table = table;
    table.getModel().addTableModelListener(this);
  }

  public synchronized void addMouseListener(final MouseListener l) {
    if (l != null) {
      mouseListener = AWTEventMulticaster.add(mouseListener, l);
    }
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  @Override
  public Object getCellEditorValue() {
    final Object value = SwingUtil.getValue(this.editorComponent);
    return value;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public JComponent getEditorComponent() {
    return editorComponent;
  }

  public Object getOldValue() {
    return oldValue;
  }

  public int getRowIndex() {
    return rowIndex;
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    oldValue = value;
    final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
    this.attributeName = model.getFieldName(rowIndex, columnIndex);
    final DataObjectMetaData metaData = model.getMetaData();
    dataType = metaData.getAttributeType(attributeName);
    this.editorComponent = (JComponent)SwingUtil.createField(metaData,
      this.attributeName, true);
    if (this.editorComponent instanceof JTextField) {
      final JTextField textField = (JTextField)this.editorComponent;
      textField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(WebColors.LightSteelBlue),
        BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    }
    editorComponent.setOpaque(false);
    SwingUtil.setFieldValue(this.editorComponent, value);

    this.rowIndex = rowIndex;
    this.columnIndex = columnIndex;
    editorComponent.addKeyListener(this);
    editorComponent.addMouseListener(this);
    if (editorComponent instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)editorComponent;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component comboEditorComponent = editor.getEditorComponent();
      comboEditorComponent.addKeyListener(this);
      comboEditorComponent.addMouseListener(this);
    }
    if (popupMenu != null) {
      popupMenu.addToComponent(editorComponent);
    }
    return this.editorComponent;
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
  public void keyPressed(final KeyEvent e) {
    final int keyCode = e.getKeyCode();
    if (keyCode == KeyEvent.VK_ENTER) {
      if (e.isShiftDown()) {
        table.editCell(rowIndex - 1, columnIndex);
      } else {
        table.editCell(rowIndex + 1, columnIndex);
      }
      e.consume();
    } else if (keyCode == KeyEvent.VK_TAB) {
      if (e.isShiftDown()) {
        table.editCell(rowIndex, columnIndex - 1);
      } else {
        table.editCell(rowIndex, columnIndex + 1);
      }
      e.consume();
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    Listener.mouseEvent(mouseListener, e);
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    Listener.mouseEvent(mouseListener, e);
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    Listener.mouseEvent(mouseListener, e);
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    Listener.mouseEvent(mouseListener, e);
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    Listener.mouseEvent(mouseListener, e);
  }

  public synchronized void removeMouseListener(final MouseListener l) {
    if (l != null) {
      mouseListener = AWTEventMulticaster.remove(mouseListener, l);
    }
  }

  public void setPopupMenu(final MenuFactory menu) {
    setPopupMenu(new PopupMenu(menu));
  }

  public void setPopupMenu(final PopupMenu popupMenu) {
    this.popupMenu = popupMenu;
    popupMenu.setAutoCreateDnd(false);
  }

  @Override
  public boolean stopCellEditing() {
    boolean stopped = false;
    try {
      if (editorComponent instanceof Field) {
        final Field field = (Field)editorComponent;
        field.updateFieldValue();
      }
      stopped = super.stopCellEditing();
    } catch (final IndexOutOfBoundsException e) {
      return true;
    } catch (final Throwable t) {
      final int result = JOptionPane.showConfirmDialog(editorComponent,
        "<html><p><b>'" + getCellEditorValue() + "' is not a valid " + dataType
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
        if (editorComponent != null) {
          editorComponent.removeMouseListener(this);
          editorComponent.removeKeyListener(this);
          if (editorComponent instanceof JComboBox) {
            final JComboBox comboBox = (JComboBox)editorComponent;
            final ComboBoxEditor editor = comboBox.getEditor();
            final Component comboEditorComponent = editor.getEditorComponent();
            comboEditorComponent.removeKeyListener(this);
            comboEditorComponent.removeMouseListener(this);
          }
          if (popupMenu != null) {
            PopupMenu.removeFromComponent(editorComponent);
          }
        }
      }
    }
    return stopped;
  }

  @Override
  public void tableChanged(final TableModelEvent e) {
    if (e.getFirstRow() <= rowIndex && rowIndex <= e.getLastRow()) {
      if (e.getColumn() == TableModelEvent.ALL_COLUMNS) {
        cancelCellEditing();
      }
    }
  }
}
