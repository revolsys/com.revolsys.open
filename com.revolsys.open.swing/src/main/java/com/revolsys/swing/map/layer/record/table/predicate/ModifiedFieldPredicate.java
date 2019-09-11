package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;

import javax.swing.JComponent;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.table.highlighter.OddEvenColorHighlighter;
import com.revolsys.swing.table.record.RecordRowTable;
import com.revolsys.swing.table.record.model.RecordRowTableModel;

public class ModifiedFieldPredicate implements HighlightPredicate {
  public static void add(final RecordRowTable table) {
    final RecordRowTableModel model = table.getModel();
    final ModifiedFieldPredicate predicate = new ModifiedFieldPredicate(model);
    table
      .addHighlighter(new OddEvenColorHighlighter(predicate, WebColors.Lime, WebColors.LimeGreen));
  }

  private final RecordRowTableModel model;

  public ModifiedFieldPredicate(final RecordRowTableModel model) {
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    String toolTip = null;
    boolean highlighted = false;
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final Record record = this.model.getRecord(rowIndex);
      if (record instanceof LayerRecord) {
        final LayerRecord layerRecord = (LayerRecord)record;
        final AbstractRecordLayer layer = layerRecord.getLayer();
        if (layer.isDeleted(layerRecord)) {
          highlighted = false;
        } else {
          final int columnIndex = adapter.convertColumnIndexToModel(adapter.column);
          final String fieldName = this.model.getColumnFieldName(columnIndex);
          highlighted = layerRecord.isModified(fieldName);
          if (highlighted) {
            final RecordDefinition recordDefinition = layerRecord.getRecordDefinition();
            final Object originalValue = layerRecord.getOriginalValue(fieldName);
            final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(fieldName);
            String text;
            if (originalValue == null) {
              text = "-";
            } else if (codeTable == null) {
              text = DataTypes.toString(originalValue);
            } else {
              text = codeTable.getValue(Identifier.newIdentifier(originalValue));
              if (text == null) {
                text = "-";
              }
            }
            toolTip = text;
          }
          if (columnIndex < this.model.getColumnFieldsOffset()) {
            highlighted = false;
          }
        }
      }
    } catch (final IndexOutOfBoundsException e) {
      highlighted = false;
    }
    final JComponent component = (JComponent)renderer;
    if (toolTip != null && toolTip.length() > 100) {
      toolTip = toolTip.substring(0, 100) + "...";
    }
    component.setToolTipText(toolTip);
    return highlighted;
  }
}
