package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.swing.JComboBox;
import javax.swing.SpringLayout;

import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.SpringLayoutUtil;

public class DashField extends ValueField implements ItemListener {
  private static List<Double> createDash(final Double... dashArray) {
    return Arrays.asList(dashArray);
  }

  private static final long serialVersionUID = -1435164163638312884L;

  private final JComboBox<List<Double>> dashField;

  public DashField(final String fieldName, final List<Measure<Length>> dash) {
    super(fieldName, dash);
    setLayout(new SpringLayout());

    final Vector<List<Double>> dashes = new Vector<>();
    dashes.add(null);
    for (final double i : new double[] {
      2, 5, 10, 15
    }) {
      dashes.add(createDash(i));
    }

    dashes.add(createDash(8.0, 3.0, 3.0, 3.0));
    dashes.add(createDash(8.0, 3.0, 3.0, 3.0, 3.0, 3.0));

    this.dashField = new JComboBox<List<Double>>(dashes);
    this.dashField.setEditable(false);
    this.dashField.setSelectedItem(dash);
    this.dashField.setRenderer(new DashListCellRenderer());
    this.dashField.addItemListener(this);

    add(this.dashField);
    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 5, 5);
  }

  @SuppressWarnings("unchecked")
  public List<Measure<Length>> getDash() {
    return (List<Measure<Length>>)this.dashField.getSelectedItem();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == this.dashField && event.getStateChange() == ItemEvent.SELECTED) {
      final List<Measure<Length>> dash = (List<Measure<Length>>)this.dashField.getSelectedItem();
      setFieldValue(dash);
    }
  }

  @Override
  public void save() {
    final List<Measure<Length>> dash = getDash();
    setFieldValue(dash);
  }
}
