package com.revolsys.swing.table.dataobject.row;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class DataObjectRowTableCellRenderer extends DefaultTableCellRenderer {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DataObjectRowTableCellRenderer() {
    setBorder(new EmptyBorder(1, 2, 1, 2));
    setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {

    final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();

    final DataObjectMetaData metaData = model.getMetaData();
    final String attributeName = model.getAttributeName(column);
    final boolean required = metaData.isAttributeRequired(column);

    final int[] selectedRows = table.getSelectedRows();
    boolean selected = false;
    for (final int selectedRow : selectedRows) {
      if (row == selectedRow) {
        selected = true;
      }
    }

    boolean hasValue;
    String text = "-";
    if (value == null) {
      hasValue = false;
    } else {
      hasValue = true;
      final CodeTable codeTable = metaData.getCodeTableByColumn(attributeName);
      if (attributeName.equals(metaData.getIdAttributeName())
        || codeTable == null) {
        text = StringConverterRegistry.toString(value);
      } else {
        final List<Object> values = codeTable.getValues(value);
        text = CollectionUtil.toString(values);
      }
    }

    if (!StringUtils.hasText(text)) {
      text = "-";
      hasValue = false;
    }
    if (!hasValue) {
      if (metaData.getIdAttributeIndex() == column) {
        text = "NEW";
        hasValue = true;
      }
    }

    super.getTableCellRendererComponent(table, text, selected, hasFocus, row,
      column);
    return this;
  }
}
