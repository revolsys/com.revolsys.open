package com.revolsys.swing.map.layer.dataobject.table.predicate;

import java.awt.Component;

import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerAttributesTableModel;
import com.revolsys.swing.table.BaseJxTable;

public class FormAllFieldsModifiedPredicate implements HighlightPredicate {

  public static void add(final DataObjectLayerForm form, final BaseJxTable table) {
    final DataObjectLayerAttributesTableModel model = table.getTableModel();
    final FormAllFieldsModifiedPredicate predicate = new FormAllFieldsModifiedPredicate(
      form, model);
    ModifiedAttributePredicate.addModifiedHighlighters(table, predicate);
  }

  private final DataObjectLayerAttributesTableModel model;

  private final DataObjectLayerForm form;

  public FormAllFieldsModifiedPredicate(final DataObjectLayerForm form,
    final DataObjectLayerAttributesTableModel model) {
    this.form = form;
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer,
    final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final String fieldName = model.getFieldName(rowIndex);
      if (fieldName != null) {
        if (form.isFieldValid(fieldName)) {
          if (form.hasOriginalValue(fieldName)) {
            final Object fieldValue = form.getFieldValue(fieldName);
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
