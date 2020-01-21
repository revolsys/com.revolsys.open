package com.revolsys.swing.table.json;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.EventObject;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.BasePanel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.toolbar.ToolBar;

public class JsonObjectRecordLayerTableCellEditor extends BaseTableCellEditor {
  final JDialog dialog = new JDialog();

  final JsonObjectTableField field;

  public JsonObjectRecordLayerTableCellEditor(final FieldDefinition fieldDefinition,
    final List<FieldDefinition> extendedFields) {
    this.field = new JsonObjectTableField("x", extendedFields);
    this.field.setPreferredSize(new Dimension(400, 150));

    final ToolBar toolBar = this.field.getToolBar();
    toolBar.addButtonTitleIcon("save", "Cancel", "cross", this::cancelCellEditing);
    toolBar.addButtonTitleIcon("save", "OK", "tick", this::stopCellEditing);

    final BasePanel panel = new BasePanel(new VerticalLayout(), this.field);
    final Container contentPane = this.dialog.getContentPane();
    contentPane.add(panel);
    this.dialog.setTitle(fieldDefinition.getTitle());
    this.dialog.pack();

    this.dialog.addWindowFocusListener(new WindowFocusListener() {
      @Override
      public void windowGainedFocus(final WindowEvent e) {
      }

      @Override
      public void windowLostFocus(final WindowEvent e) {
        fireEditingStopped();
        JsonObjectRecordLayerTableCellEditor.this.dialog.setVisible(false);
      }
    });
  }

  @Override
  public void cancelCellEditing() {
    this.field.cancelCellEditing();
    super.cancelCellEditing();
    this.dialog.setVisible(false);
  }

  @Override
  public Object getCellEditorValue() {
    final JsonObject object = this.field.getFieldValue();
    if (object == null) {
      return null;
    } else {
      return object.clone();
    }
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, final int rowIndex, final int columnIndex) {
    startEditing(rowIndex, columnIndex);
    final EventObject event = ((BaseJTable)table).getEditEvent();
    if (event instanceof MouseEvent) {
      final MouseEvent mouseEvent = (MouseEvent)event;
      SwingUtil.setLocationCentreAtEvent(this.dialog, mouseEvent);
    }
    JsonObject object = (JsonObject)value;
    if (object != null) {
      object = object.clone();
    }
    this.field.setFieldValue(object);
    this.dialog.setVisible(true);
    return new JLabel();
  }

  @Override
  public boolean stopCellEditing() {
    this.field.stopCellEditing();
    if (super.stopCellEditing()) {
      this.dialog.setVisible(false);
      return true;
    } else {
      return false;
    }
  }
}
