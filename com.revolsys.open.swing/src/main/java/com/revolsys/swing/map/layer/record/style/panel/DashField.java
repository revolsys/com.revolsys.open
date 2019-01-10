package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.JComboBox;
import javax.swing.SpringLayout;

import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.layout.SpringLayoutUtil;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

public class DashField extends ValueField implements ItemListener {
  private static final long serialVersionUID = -1435164163638312884L;

  private final JComboBox<List<Double>> dashField;

  public DashField(final String fieldName, final List<Quantity<Length>> dash) {
    super(new SpringLayout(), fieldName, dash);

    final List<List<Double>> dashes = Arrays.asList(null, //
      GeometryStyle.DOT, //
      GeometryStyle.DASH_5, //
      GeometryStyle.DASH_10, //
      GeometryStyle.DASH_15, //
      GeometryStyle.DASH_DOT, //
      GeometryStyle.DASH_DOT_DOT, //
      GeometryStyle.newDash(8.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0, 3.0)//
    );

    this.dashField = ComboBox.newComboBox("lineDashArray", dashes);
    this.dashField.setEditable(false);
    this.dashField.setSelectedItem(dash);
    this.dashField.setRenderer(new DashListCellRenderer());
    this.dashField.addItemListener(this);

    add(this.dashField);
    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 5, 5);
  }

  @SuppressWarnings("unchecked")
  public List<Quantity<Length>> getDash() {
    return (List<Quantity<Length>>)this.dashField.getSelectedItem();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == this.dashField && event.getStateChange() == ItemEvent.SELECTED) {
      final List<Quantity<Length>> dash = (List<Quantity<Length>>)this.dashField.getSelectedItem();
      setFieldValue(dash);
    }
  }

  @Override
  public void save() {
    final List<Quantity<Length>> dash = getDash();
    setFieldValue(dash);
  }
}
