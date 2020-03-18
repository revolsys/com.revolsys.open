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
import org.jeometry.common.awt.WebColors;

import com.revolsys.swing.map.form.LayerRecordForm;
import com.revolsys.swing.map.layer.record.table.model.LayerRecordTableModel;
import com.revolsys.swing.table.BaseJTable;

public class FormAllFieldsErrorPredicate implements HighlightPredicate {

  public static void add(final LayerRecordForm form, final BaseJTable table) {
    final LayerRecordTableModel model = table.getTableModel();
    final FormAllFieldsErrorPredicate predicate = new FormAllFieldsErrorPredicate(form, model);
    addErrorHighlighters(table, predicate);
  }

  public static void addErrorHighlighters(final JXTable table, final HighlightPredicate predicate) {

    table.addHighlighter(new ColorHighlighter(
      new AndHighlightPredicate(predicate, HighlightPredicate.EVEN),
      WebColors.newAlpha(WebColors.LightCoral, 127), WebColors.Black, WebColors.Red, Color.WHITE));

    table.addHighlighter(
      new ColorHighlighter(new AndHighlightPredicate(predicate, HighlightPredicate.ODD),
        WebColors.LightCoral, WebColors.Black, WebColors.DarkRed, WebColors.White));
  }

  private final Reference<LayerRecordForm> form;

  private final LayerRecordTableModel model;

  public FormAllFieldsErrorPredicate(final LayerRecordForm form,
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
