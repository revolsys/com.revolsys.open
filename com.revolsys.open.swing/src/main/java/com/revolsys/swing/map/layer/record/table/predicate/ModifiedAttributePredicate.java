package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.SingleRecordIdentifier;
import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.swing.map.layer.record.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;
import com.revolsys.swing.table.dataobject.row.DataObjectRowTable;

public class ModifiedAttributePredicate implements HighlightPredicate {
  public static void add(final DataObjectRowTable table) {
    final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();
    final ModifiedAttributePredicate predicate = new ModifiedAttributePredicate(
      model);
    addModifiedHighlighters(table, predicate);
  }

  public static void addModifiedHighlighters(final JXTable table,
    final HighlightPredicate predicate) {

    table.addHighlighter(new ColorHighlighter(new AndHighlightPredicate(
      predicate, HighlightPredicate.EVEN), ColorUtil.setAlpha(
        WebColors.YellowGreen, 127), WebColors.Black, WebColors.LimeGreen,
        Color.WHITE));

    table.addHighlighter(new ColorHighlighter(new AndHighlightPredicate(
      predicate, HighlightPredicate.ODD), WebColors.YellowGreen,
      WebColors.Black, WebColors.Green, Color.WHITE));
  }

  private final DataObjectRowTableModel model;

  public ModifiedAttributePredicate(final DataObjectRowTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    String toolTip = null;
    boolean highlighted = false;
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final DataObject record = this.model.getRecord(rowIndex);
      if (record instanceof LayerRecord) {
        final LayerRecord layerRecord = (LayerRecord)record;
        final AbstractDataObjectLayer layer = layerRecord.getLayer();
        if (layer.isDeleted(layerRecord)) {
          highlighted = false;
        } else {
          final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
          final String attributeName = this.model.getFieldName(columnIndex);
          highlighted = layerRecord.isModified(attributeName);
          if (highlighted) {
            final DataObjectMetaData metaData = layerRecord.getMetaData();
            final String fieldName = metaData.getAttributeName(columnIndex);
            final Object originalValue = layerRecord.getOriginalValue(fieldName);
            final CodeTable codeTable = metaData.getCodeTableByColumn(fieldName);
            String text;
            if (originalValue == null) {
              text = "-";
            } else if (codeTable == null) {
              text = StringConverterRegistry.toString(originalValue);
            } else {
              text = codeTable.getValue(SingleRecordIdentifier.create(
                originalValue));
              if (text == null) {
                text = "-";
              }
            }
            toolTip = text;
          }
        }
      }
    } catch (final IndexOutOfBoundsException e) {
      highlighted = false;
    }
    final JComponent component = adapter.getComponent();
    if (toolTip != null && toolTip.length() > 100) {
      toolTip = toolTip.substring(0, 100) + "...";
    }
    component.setToolTipText(toolTip);
    return highlighted;
  }
}
