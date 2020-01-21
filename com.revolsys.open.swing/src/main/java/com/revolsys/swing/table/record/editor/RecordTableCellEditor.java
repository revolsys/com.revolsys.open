package com.revolsys.swing.table.record.editor;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.jeometry.common.awt.WebColors;

import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.AbstractRecordQueryField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.menu.ShowMenuMouseListener;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.record.model.AbstractRecordTableModel;

public class RecordTableCellEditor extends BaseTableCellEditor {

  private static final long serialVersionUID = 1L;

  private String fieldName;

  public RecordTableCellEditor(final BaseJTable table) {
    super(table);
  }

  protected String getColumnFieldName(final int rowIndex, final int columnIndex) {
    final AbstractRecordTableModel model = (AbstractRecordTableModel)getTableModel();
    return model.getColumnFieldName(rowIndex, columnIndex);
  }

  public String getFieldName() {
    return this.fieldName;
  }

  protected RecordDefinition getRecordDefinition() {
    final AbstractRecordTableModel tableModel = (AbstractRecordTableModel)getTableModel();
    return tableModel.getRecordDefinition();
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, final int rowIndex, final int columnIndex) {
    startEditing(rowIndex, columnIndex);
    this.fieldName = getColumnFieldName(this.rowIndex, this.columnIndex);
    final RecordDefinition recordDefinition = getRecordDefinition();
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

    this.editorComponent.addKeyListener(this.keyListener);
    this.editorComponent.addMouseListener(this.mouseListeners);
    if (this.editorComponent instanceof JComboBox) {
      final JComboBox<?> comboBox = (JComboBox<?>)this.editorComponent;
      final ComboBoxEditor editor = comboBox.getEditor();
      final Component comboEditorComponent = editor.getEditorComponent();
      comboEditorComponent.addKeyListener(this.keyListener);
      comboEditorComponent.addMouseListener(this.mouseListeners);
    } else if (this.editorComponent instanceof AbstractRecordQueryField) {
      final AbstractRecordQueryField queryField = (AbstractRecordQueryField)this.editorComponent;
      final TextField searchField = queryField.getSearchField();
      searchField.addKeyListener(this.keyListener);
      searchField.addMouseListener(this.mouseListeners);
    }
    this.popupMenuListener = ShowMenuMouseListener.addListener(this.editorComponent,
      this.popupMenuFactory);
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

  protected Field newField(final String fieldName) {
    final RecordDefinition recordDefinition = getRecordDefinition();
    return SwingUtil.newField(recordDefinition, fieldName, true);
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
          this.editorComponent.removeKeyListener(this.keyListener);
          if (this.editorComponent instanceof JComboBox) {
            final JComboBox<?> comboBox = (JComboBox<?>)this.editorComponent;
            final ComboBoxEditor editor = comboBox.getEditor();
            final Component comboEditorComponent = editor.getEditorComponent();
            comboEditorComponent.removeKeyListener(this.keyListener);
            comboEditorComponent.removeMouseListener(this.mouseListeners);
          } else if (this.editorComponent instanceof AbstractRecordQueryField) {
            final AbstractRecordQueryField queryField = (AbstractRecordQueryField)this.editorComponent;
            final TextField searchField = queryField.getSearchField();
            searchField.removeKeyListener(this.keyListener);
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

}
