package com.revolsys.swing.map.table;

import java.awt.Color;
import java.awt.Component;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.swing.WebColors;
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
    DataObjectLayer layer = model.getLayer();
    DataObject object = model.getObject(row);
    final boolean selected = layer.isSelected(object);
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
    if (object == null) {
      hasValue = true;
      text = "...";
    }
    boolean isNew = layer.isNew(object);
    if (isNew && !hasValue) {
      if (metaData.getIdAttributeIndex() == column) {
        text = "NEW";
        hasValue = true;
      }
    }

    final Component component = super.getTableCellRendererComponent(table,
      text, selected, hasFocus, row, column);

    boolean valid = !required || hasValue;

    setColors(component, row, selected, valid, getBackground(), Color.RED,
      Color.LIGHT_GRAY, WebColors.DarkSalmon, Color.WHITE, Color.PINK);
    if (layer.isModified(object)) {
      if (object instanceof LayerDataObject) {
        LayerDataObject layerDataObject = (LayerDataObject)object;
        if (layerDataObject.isModified(attributeName)) {
          if (selected) {
            component.setBackground(WebColors.Green);
          } else {
            boolean even = row % 2 == 0;
            if (even) {
              component.setBackground(WebColors.GreenYellow);
            } else {
              component.setBackground(WebColors.YellowGreen);
            }
          }
        }
      }
    }
    return component;
  }

  protected void setColors(final Component component, final int row,
    final boolean selected, boolean valid, Color selectedBackground,
    Color invalidSelectedBackground, Color oddBackground,
    Color invalidOddBackground, Color evenBackground,
    Color invalidEvenBackground) {
    if (selected) {
      if (valid) {
        component.setBackground(selectedBackground);
        component.setForeground(Color.WHITE);
      } else {
        component.setBackground(invalidSelectedBackground);
        component.setForeground(Color.WHITE);
      }
    } else {
      boolean even = row % 2 == 0;
      if (even) {
        if (valid) {
          component.setBackground(evenBackground);
          component.setForeground(Color.BLACK);
        } else {
          component.setBackground(invalidEvenBackground);
          component.setForeground(Color.RED);
        }
      } else {
        if (valid) {
          component.setBackground(oddBackground);
          component.setForeground(Color.BLACK);
        } else {
          component.setBackground(invalidOddBackground);
          component.setForeground(Color.RED);
        }
      }
    }
  }
}
