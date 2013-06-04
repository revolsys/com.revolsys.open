package com.revolsys.swing.table.dataobject;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.builder.DataObjectMetaDataUiBuilderRegistry;
import com.revolsys.swing.builder.ValueUiBuilder;

@SuppressWarnings("serial")
public class DataObjectTableCellEditor extends AbstractCellEditor implements
  TableCellEditor {
  private DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry;

  private JComponent editorComponent;

  private ValueUiBuilder uiBuilder;

  private String attributeName;

  public DataObjectTableCellEditor() {
    this(DataObjectMetaDataUiBuilderRegistry.getInstance());
  }

  public DataObjectTableCellEditor(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }

  public String getAttributeName() {
    return attributeName;
  }

  @Override
  public Object getCellEditorValue() {
    if (uiBuilder != null) {
      return uiBuilder.getCellEditorValue();
    } else {
      return SwingUtil.getValue(editorComponent);
    }
  }

  @Override
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, final int row,
    final int column) {
    int attributeIndex;
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      attributeIndex = jxTable.convertRowIndexToModel(row);
    } else {
      attributeIndex = row;
    }
    if (column == 2) {
      final AbstractDataObjectTableModel model = (AbstractDataObjectTableModel)table.getModel();
      final DataObjectMetaData metaData = model.getMetaData();
      this.attributeName = metaData.getAttributeName(attributeIndex);
      uiBuilder = uiBuilderRegistry.getValueUiBuilder(metaData, attributeIndex);
      if (uiBuilder != null) {
        return uiBuilder.getEditorComponent(value);
      } else {
        editorComponent = SwingUtil.createField(metaData, attributeName, true);
        if (editorComponent instanceof JTextField) {
          final JTextField textField = (JTextField)editorComponent;
          textField.setHorizontalAlignment(SwingConstants.LEFT);
        }
        SwingUtil.setFieldValue(editorComponent, value);
      }
    } else {
      editorComponent = null;
    }
    return editorComponent;
  }

  public void setUiBuilderRegistry(
    final DataObjectMetaDataUiBuilderRegistry uiBuilderRegistry) {
    this.uiBuilderRegistry = uiBuilderRegistry;
  }
}
