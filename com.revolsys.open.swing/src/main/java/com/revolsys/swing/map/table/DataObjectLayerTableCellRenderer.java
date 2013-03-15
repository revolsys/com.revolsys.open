package com.revolsys.swing.map.table;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

public class DataObjectLayerTableCellRenderer extends DefaultTableCellRenderer {

  private static final Color DARK_SALMON = new Color(250, 128, 114);

  private final DataObjectLayerTableModel model;

  public DataObjectLayerTableCellRenderer(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    final boolean selected = model.isSelected(row);
    final DataObjectMetaData metaData = model.getMetaData();
    final String attributeName = model.getAttributeName(column);
    final boolean required = metaData.isAttributeRequired(column);
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
    if (model.getObject(row) == null) {
      hasValue = true;
      text = "...";
    }
    final Component component = super.getTableCellRendererComponent(table,
      text, selected, hasFocus, row, column);
    if (selected) {
      if (required && !hasValue) {
        component.setBackground(Color.RED);
        component.setForeground(Color.WHITE);
      }
    } else {
      if (row % 2 == 0) {
        if (required && !hasValue) {
          component.setBackground(Color.PINK);
          component.setForeground(Color.RED);
        } else {
          component.setBackground(Color.WHITE);
          component.setForeground(table.getForeground());
        }
      } else {
        if (required && !hasValue) {
          component.setBackground(DARK_SALMON);
          component.setForeground(Color.RED);
        } else {
          component.setBackground(Color.LIGHT_GRAY);
          component.setForeground(table.getForeground());
        }
      }
    }
    return component;
  }
}
