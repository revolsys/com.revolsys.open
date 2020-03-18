package com.revolsys.swing.map.layer.record.table.model;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jeometry.common.data.type.DataType;

import com.revolsys.record.RecordState;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.field.ArrayListComboBoxModel;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.map.form.FieldNamesSetPanel;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.map.layer.record.table.predicate.FormAllFieldsErrorPredicate;
import com.revolsys.swing.map.layer.record.table.predicate.FormAllFieldsModifiedPredicate;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.editor.BaseTableCellEditor;
import com.revolsys.swing.table.record.model.AbstractSingleRecordTableModel;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class LayerRecordTableModel extends AbstractSingleRecordTableModel
  implements PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  public static TablePanel newTablePanel(final LayerRecordForm form) {
    final LayerRecordTableModel tableModel = new LayerRecordTableModel(form);

    return tableModel.newTablePanel();
  }

  private final Reference<LayerRecordForm> form;

  private final AbstractRecordLayer layer;

  private LayerRecord record;

  private final ComboBox<String> fieldNamesSetNamesField;

  public LayerRecordTableModel(final LayerRecordForm form) {
    super(form.getRecordDefinition(), true);
    this.form = new WeakReference<>(form);
    this.layer = form.getLayer();
    this.record = form.getRecord();

    final List<String> fieldNamesSetNames = this.layer.getFieldNamesSetNames();
    this.fieldNamesSetNamesField = ComboBox.newComboBox("fieldNamesSetName", fieldNamesSetNames);
    int maxLength = 3;
    for (final String name : fieldNamesSetNames) {
      maxLength = Math.max(maxLength, name.length());
    }
    this.fieldNamesSetNamesField
      .setMaximumSize(new Dimension(Math.max(300, maxLength * 11 + 40), 22));

    final String fieldNamesSetName = this.layer.getFieldNamesSetName();
    this.fieldNamesSetNamesField.setSelectedItem(fieldNamesSetName);

    Property.addListener(this.fieldNamesSetNamesField, "fieldNamesSetName", this);
    Property.addListener(this.layer, this);

    refreshFieldNames();
  }

  @Override
  public void dispose() {
    super.dispose();
    Property.removeListener(this.layer, this);
  }

  @Override
  public BaseTableCellEditor getCellEditor(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      final String fieldName = getColumnFieldName(columnIndex);
      if (fieldName != null) {
        final BaseJTable table = getTable();
        return this.layer.newTableCellEditor(table, fieldName);
      }
    }
    return null;
  }

  @Override
  public int getColumnCount() {
    return 4;
  }

  @Override
  public String getColumnName(final int column) {
    if (column == 3) {
      return "Original Value";
    } else {
      return super.getColumnName(column);
    }
  }

  @Override
  public String getFieldTitle(final String fieldName) {
    return this.layer.getFieldTitle(fieldName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Map<String, Object>> V getMap(final int columnIndex) {
    if (columnIndex == 2) {
      return (V)this.record;
    } else {
      return null;
    }
  }

  @Override
  public Object getObjectValue(final int rowIndex, final int columnIndex) {
    if (this.record == null) {
      return null;
    } else {
      final String fieldName = getColumnFieldName(rowIndex);
      return this.record.getValue(fieldName);
    }
  }

  public LayerRecord getRecord() {
    return this.record;
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if (this.record == null) {
      return null;
    } else if (columnIndex == 3) {
      final String fieldName = getColumnFieldName(rowIndex);
      return this.record.getOriginalValue(fieldName);
    } else {
      return super.getValueAt(rowIndex, columnIndex);
    }
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 2) {
      if (this.form.get().isEditable()) {
        final String fieldName = getColumnFieldName(rowIndex);
        final RecordDefinition recordDefinition = getRecordDefinition();
        final FieldDefinition idField = recordDefinition.getIdField();
        if (idField != null) {
          if (recordDefinition.isIdField(fieldName)) {
            if (this.record != null && this.record.getState() == RecordState.NEW) {
              if (!Number.class.isAssignableFrom(idField.getTypeClass())) {
                return true;
              }
            }
            return false;
          }
        }
        if (recordDefinition.getGeometryFieldNames().contains(fieldName)) {
          return false;
        } else {
          return this.form.get().isEditable(fieldName);
        }
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public boolean isModified(final int rowIndex) {
    final String fieldName = getColumnFieldName(rowIndex);
    final Object originalValue = this.record.getOriginalValue(fieldName);
    final Object value = this.record.getValue(fieldName);
    return !DataType.equal(originalValue, value);
  }

  @Override
  public TablePanel newTablePanel() {
    final LayerRecordForm form = this.form.get();
    final BaseJTable table = AbstractSingleRecordTableModel.newTable(this);

    FormAllFieldsModifiedPredicate.add(form, table);
    FormAllFieldsErrorPredicate.add(form, table);

    final TableColumnModel columnModel = table.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      final TableColumn column = columnModel.getColumn(i);
      if (i == 2) {
        final TableCellEditor cellEditor = column.getCellEditor();
        cellEditor.addCellEditorListener(form);
      }
    }

    final TablePanel tablePanel = new TablePanel(table);
    final ToolBar toolBar = tablePanel.getToolBar();

    toolBar.addComponent("default", this.fieldNamesSetNamesField);

    toolBar.addButtonTitleIcon("default", "Edit Field Sets", "fields_filter:edit", () -> {
      final String fieldNamesSetName = FieldNamesSetPanel.showDialog(this.layer);
      if (Property.hasValue(fieldNamesSetName)) {
        this.fieldNamesSetNamesField.setFieldValue(fieldNamesSetName);
      }
    });

    int maxHeight = 500;
    for (final GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getScreenDevices()) {
      final GraphicsConfiguration graphicsConfiguration = device.getDefaultConfiguration();
      final Rectangle bounds = graphicsConfiguration.getBounds();

      maxHeight = Math.min(bounds.height, maxHeight);
    }
    final int preferredHeight = Math.min(maxHeight, (this.getRowCount() + 1) * 20 + 45);
    tablePanel.setMinimumSize(new Dimension(100, preferredHeight));
    tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
    tablePanel.setPreferredSize(new Dimension(800, preferredHeight));

    return tablePanel;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    final String propertyName = event.getPropertyName();
    if (source == this.record) {
      final RecordDefinition recordDefinition = getRecordDefinition();
      final int index = recordDefinition.getFieldIndex(propertyName);
      if (index > -1) {
        try {
          fireTableRowsUpdated(index, index);
        } catch (final Throwable t) {
        }
      }
    } else if (source == this.layer) {
      if ("fieldNamesSets".equals(propertyName)) {
        refreshFieldNames();
      }
    } else if (source == this.fieldNamesSetNamesField) {
      final String fieldNamesSetName = this.fieldNamesSetNamesField.getFieldValue();
      final List<String> fieldNames = this.layer.getFieldNamesSet(fieldNamesSetName);
      if (fieldNames != null) {
        setFieldNames(fieldNames);
      }
    }
  }

  protected void refreshFieldNames() {
    final ArrayListComboBoxModel<String> model = this.fieldNamesSetNamesField.getComboBoxModel();
    final List<String> fieldNamesSetNames = this.layer.getFieldNamesSetNames();
    model.setAll(fieldNamesSetNames);

    setFieldNames(this.layer.getFieldNamesSet());

  }

  public void removeListener() {
    Property.removeListener(this.layer, this);
  }

  @Override
  protected Object setObjectValue(final String fieldName, final Object value) {
    final Object oldValue = this.record.getValue(fieldName);
    this.record.setValue(fieldName, value);
    return oldValue;
  }

  public void setRecord(final LayerRecord record) {
    this.record = record;
  }
}
