package com.revolsys.swing.map.layer.dataobject.table.predicate;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.color.ColorUtil;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.map.form.DataObjectLayerForm;
import com.revolsys.swing.map.layer.dataobject.table.model.DataObjectLayerAttributesTableModel;
import com.revolsys.swing.table.BaseJxTable;

public class FormAllFieldsErrorPredicate implements HighlightPredicate {

  public static void add(final DataObjectLayerForm form, final BaseJxTable table) {
    final DataObjectLayerAttributesTableModel model = table.getTableModel();
    final FormAllFieldsErrorPredicate predicate = new FormAllFieldsErrorPredicate(
      form, model);
    addErrorHighlighters(table, predicate);
  }

  public static void addErrorHighlighters(final JXTable table,
    final HighlightPredicate predicate) {

    table.addHighlighter(new ColorHighlighter(new AndHighlightPredicate(
      predicate, HighlightPredicate.EVEN), ColorUtil.setAlpha(
      WebColors.LightCoral, 127), WebColors.Black, WebColors.Red, Color.WHITE));

    table.addHighlighter(new ColorHighlighter(new AndHighlightPredicate(
      predicate, HighlightPredicate.ODD), WebColors.LightCoral,
      WebColors.Black, WebColors.DarkRed, WebColors.White));
  }

  private final DataObjectLayerAttributesTableModel model;

  private final DataObjectLayerForm form;

  public FormAllFieldsErrorPredicate(final DataObjectLayerForm form,
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
        if (!form.isFieldValid(fieldName)) {
          JComponent jcomponent = (JComponent)renderer;
          form.setFieldInvalidToolTip(fieldName, jcomponent);
          return true;
        }
      }
    } catch (final IndexOutOfBoundsException e) {
    }
    return false;

  }
}
