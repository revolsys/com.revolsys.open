package com.revolsys.swing.table.dataobject.editor;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.table.BaseJxTable;
import com.revolsys.swing.table.dataobject.model.AbstractDataObjectTableModel;

public class DataObjectTableCellEditor extends AbstractCellEditor implements
  TableCellEditor, KeyListener {

  private static final long serialVersionUID = 1L;

  private JComponent editorComponent;

  private String attributeName;

  private final BaseJxTable table;

  public DataObjectTableCellEditor(final BaseJxTable table) {
    this.table = table;
  }

  public String getAttributeName() {
    return this.attributeName;
  }

  @Override
  public Object getCellEditorValue() {
    return SwingUtil.getValue(this.editorComponent);
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
    this.attributeName = model.getAttributeName(rowIndex, columnIndex);
    final DataObjectMetaData metaData = model.getMetaData();
    this.editorComponent = (JComponent)SwingUtil.createField(metaData,
      this.attributeName, true);
    if (this.editorComponent instanceof JTextField) {
      final JTextField textField = (JTextField)this.editorComponent;
      textField.setHorizontalAlignment(SwingConstants.LEFT);
      textField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(WebColors.LightSteelBlue),
        BorderFactory.createEmptyBorder(1, 2, 1, 2)));
    }
    editorComponent.setOpaque(false);
    SwingUtil.setFieldValue(this.editorComponent, value);

    editorComponent.addKeyListener(this);
    if (editorComponent instanceof JComboBox) {
      final JComboBox comboBox = (JComboBox)editorComponent;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component comboEditorComponent = editor.getEditorComponent();
      comboEditorComponent.addKeyListener(this);

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
        if (SwingUtilities.isLeftMouseButton(mouseEvent)) {
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
        table.selectRelativeCell(-1, 0);
      } else {
        table.selectRelativeCell(1, 0);
      }
    } else if (keyCode == KeyEvent.VK_TAB) {
      if (e.isShiftDown()) {
        table.selectRelativeCell(0, -1);
      } else {
        table.selectRelativeCell(0, 1);
      }
    }
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }
}
