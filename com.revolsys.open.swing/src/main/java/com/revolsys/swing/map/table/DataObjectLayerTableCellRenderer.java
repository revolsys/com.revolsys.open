package com.revolsys.swing.map.table;

import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.CollectionUtil;

public class DataObjectLayerTableCellRenderer extends DefaultTableCellRenderer {

  private static final long serialVersionUID = 1L;

  private final DataObjectLayerTableModel model;

  public DataObjectLayerTableCellRenderer(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table,
    final Object value, final boolean isSelected, final boolean hasFocus,
    final int row, final int column) {
    final DataObjectLayer layer = model.getLayer();
    final LayerDataObject object = model.getObject(row);
    final boolean selected = layer.isSelected(object);
    final DataObjectMetaData metaData = model.getMetaData();
    final String attributeName = model.getAttributeName(column);
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
    if (object == null) {
      hasValue = true;
      text = "...";
    }
    final boolean isNew = layer.isNew(object);
    if (isNew && !hasValue) {
      if (metaData.getIdAttributeIndex() == column) {
        text = "NEW";
      }
    }

    final Component component = super.getTableCellRendererComponent(table,
      text, selected, hasFocus, row, column);

    return component;
  }
}
