package com.revolsys.swing.map.layer.record.table.predicate;

import java.awt.Color;
import java.awt.Component;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.form.RecordLayerForm;
import com.revolsys.swing.map.layer.record.table.model.LayerRecordTableModel;
import com.revolsys.swing.table.BaseJTable;

public class FormAllFieldsErrorPredicate implements HighlightPredicate {

  public static void add(final RecordLayerForm form, final BaseJTable table) {
    final LayerRecordTableModel model = table.getTableModel();
    final FormAllFieldsErrorPredicate predicate = new FormAllFieldsErrorPredicate(form, model);
    addErrorHighlighters(table, predicate);
  }

  public static void addErrorHighlighters(final JXTable table, final HighlightPredicate predicate) {

    table.addHighlighter(new ColorHighlighter(
      new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
      WebColors.setAlpha(WebColors.LightCoral, 127), WebColors.Black, WebColors.Red, Color.WHITE));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.LightCoral, WebColors.Black, WebColors.DarkRed, WebColors.White));
  }

  private final LayerRecordTableModel model;

  private final Reference<RecordLayerForm> form;

  public FormAllFieldsErrorPredicate(final RecordLayerForm form,
    final LayerRecordTableModel model) {
    this.form = new WeakReference<>(form);
    this.model = model;
  }

  @Override
  public boolean isHighlighted(final Component renderer, final ComponentAdapter adapter) {
    try {
      final int rowIndex = adapter.convertRowIndexToModel(adapter.row);
      final String fieldName = this.model.getFieldName(rowIndex);
      if (fieldName != null) {
        final RecordLayerForm form = this.form.get();
        if (!form.isFieldValid(fieldName)) {
          final JComponent jcomponent = (JComponent)renderer;
          form.setFieldInvalidToolTip(fieldName, jcomponent);
          return true;
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;

  }
}
