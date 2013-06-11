package com.revolsys.swing.map.table.predicate;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;

import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.Highlighter;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.swing.map.table.DataObjectLayerTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class ModifiedAttributePredicate implements HighlightPredicate {
  public static void add(final DataObjectRowTable table) {
    final DataObjectLayerTableModel model = (DataObjectLayerTableModel)table.getModel();
    final Highlighter highlighter = getHighlighter(model);
    table.addHighlighter(highlighter);
  }

  public static Highlighter getHighlighter(final DataObjectLayerTableModel model) {
    final ModifiedAttributePredicate predicate = new ModifiedAttributePredicate(
      model);
    return new ColorHighlighter(predicate, ColorUtil.setAlpha(WebColors.Green,
      64), WebColors.Green, WebColors.Green, Color.WHITE);
  }

  private final DataObjectLayerTableModel model;

  public ModifiedAttributePredicate(final DataObjectLayerTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final DataObject object = model.getObject(rowIndex);
      if (object instanceof LayerDataObject) {
        final LayerDataObject layerObject = (LayerDataObject)object;
        final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
        final boolean highlighted = layerObject.isModified(columnIndex);
        if (highlighted) {
          final DataObjectMetaData metaData = layerObject.getMetaData();
          final String fieldName = metaData.getAttributeName(columnIndex);
          final Object originalValue = layerObject.getOriginalValue(fieldName);
          final CodeTable codeTable = metaData.getCodeTableByColumn(fieldName);
          String text;
          if (originalValue == null) {
            text = "-";
          } else if (codeTable == null) {
            text = StringConverterRegistry.toString(originalValue);
          } else {
            text = codeTable.getValue(originalValue);
            if (text == null) {
              text = "-";
            }
          }
          final JComponent component = adapter.getComponent();
          component.setToolTipText(text);
        }
        return highlighted;
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;
  }
}
