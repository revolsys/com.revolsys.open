package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jeometry.common.awt.WebColors;
import org.jeometry.common.data.identifier.SingleIdentifier;
import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.layer.record.table.model.LayerRecordTableModel;
import com.revolsys.swing.table.BaseJTable;
import com.revolsys.util.Property;

public class FormAllFieldsModifiedPredicate implements HighlightPredicate {

  public static void add(final LayerRecordForm form, final BaseJTable table) {
    final LayerRecordTableModel model = table.getTableModel();
    final FormAllFieldsModifiedPredicate predicate = new FormAllFieldsModifiedPredicate(form,
      model);
    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
        WebColors.newAlpha(WebColors.YellowGreen, 127), WebColors.Black, WebColors.LimeGreen,
        Color.WHITE));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.YellowGreen, WebColors.Black, WebColors.Green, Color.WHITE));
  }

  private final Reference<LayerRecordForm> form;

  private final LayerRecordTableModel model;

  public FormAllFieldsModifiedPredicate(final LayerRecordForm form,
    final LayerRecordTableModel model) {
    this.form = new WeakReference<>(form);
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final String fieldName = this.model.getColumnFieldName(rowIndex);
      if (fieldName != null) {
        final LayerRecordForm form = this.form.get();
        if (form.isFieldValid(fieldName)) {
          if (form.hasOriginalValue(fieldName)) {
            Object fieldValue = form.getFieldValue(fieldName);
            if (fieldValue instanceof SingleIdentifier) {
              final SingleIdentifier singleIdentifier = (SingleIdentifier)fieldValue;
              fieldValue = singleIdentifier.getValue(0);
            }
            final Object originalValue = form.getOriginalValue(fieldName);
            boolean equal = DataType.equal(originalValue, fieldValue);
            if (!equal) {
              if (originalValue == null) {
                if (fieldValue instanceof String) {
                  final String string = (String)fieldValue;
                  if (!Property.hasValue(string)) {
                    equal = true;
                  }
                }
              }
            }
            return !equal;
          }
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;

  }
}
