package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.springframework.util.StringUtils;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.data.identifier.SingleIdentifier;
import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.layer.record.table.model.DataObjectLayerAttributesTableModel;
import com.revolsys.swing.table.BaseJxTable;

public class FormAllFieldsModifiedPredicate implements HighlightPredicate {

  public static void add(final LayerRecordForm form, final BaseJxTable table) {
    final DataObjectLayerAttributesTableModel model = table.getTableModel();
    final FormAllFieldsModifiedPredicate predicate = new FormAllFieldsModifiedPredicate(
      form, model);
    ModifiedAttributePredicate.addModifiedHighlighters(table, predicate);
  }

  private final DataObjectLayerAttributesTableModel model;

  private final Reference<LayerRecordForm> form;

  public FormAllFieldsModifiedPredicate(final LayerRecordForm form,
    final DataObjectLayerAttributesTableModel model) {
    this.form = new WeakReference<>(form);
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final String fieldName = this.model.getFieldName(rowIndex);
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
            boolean equal = EqualsRegistry.equal(originalValue, fieldValue);
            if (!equal) {
              if (originalValue == null) {
                if (fieldValue instanceof String) {
                  final String string = (String)fieldValue;
                  if (!StringUtils.hasText(string)) {
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
