package com.revolsys.swing.table.json;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
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
  private static final long serialVersionUID = 1L;

  private final JDialog dialog;

  private final JsonObjectTableField field;

  private JsonObject cellEditorValue;

  private final FieldDefinition fieldDefinition;

  public JsonObjectRecordLayerTableCellEditor(final BaseJTable table,
    final FieldDefinition fieldDefinition, final List<FieldDefinition> extendedFields) {
    super(table);
    this.fieldDefinition = fieldDefinition;
    this.dialog = new JDialog(SwingUtil.getWindowAncestor(table));
    this.field = new JsonObjectTableField(fieldDefinition, extendedFields);
    this.field.setRemoveEmptyProperties(false);
    this.field.setMaximumSize(new Dimension(400, 150));

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
        final Window oppositeWindow = e.getOppositeWindow();
        if (oppositeWindow != null
          && !JsonObjectRecordLayerTableCellEditor.this.dialog.isAncestorOf(oppositeWindow)) {
          stopCellEditing();
        }
      }
    });
  }

  @Override
  public void cancelCellEditing() {
    this.table.setTerminateEditOnFocusLost(true);
    this.field.cancelCellEditing();
    super.cancelCellEditing();
    this.dialog.setVisible(false);
  }

  @Override
  public Object getCellEditorValue() {
    return this.cellEditorValue;
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table, final Object value,
    final boolean isSelected, final int rowIndex, final int columnIndex) {
    this.table.setTerminateEditOnFocusLost(false);
    JsonObject jsonObject = this.fieldDefinition.toFieldValue(value);
    if (jsonObject == null) {
      jsonObject = JsonObject.tree();
    } else {
      jsonObject = jsonObject.clone();
    }
    this.cellEditorValue = jsonObject;
    startEditing(rowIndex, columnIndex);
    final EventObject event = ((BaseJTable)table).getEditEvent();
    if (event instanceof MouseEvent) {
      final MouseEvent mouseEvent = (MouseEvent)event;
      SwingUtil.setLocationCentreAtEvent(this.dialog, mouseEvent);
    }

    this.field.setFieldValue(jsonObject);
    this.dialog.setVisible(true);
    return new JLabel();
  }

  @Override
  public boolean stopCellEditing() {
    this.table.setTerminateEditOnFocusLost(true);
    this.field.stopCellEditing();
    final JsonObject fieldValue = this.field.getFieldValue();
    if (fieldValue != null) {
      fieldValue.removeEmptyProperties();
    }
    this.cellEditorValue = fieldValue;
    this.field.setFieldValue(null);
    this.dialog.setVisible(false);
    return super.stopCellEditing();
  }
}
