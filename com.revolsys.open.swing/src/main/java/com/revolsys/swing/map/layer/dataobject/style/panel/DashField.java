package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.swing.JComboBox;
import javax.swing.SpringLayout;

import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.SpringLayoutUtil;

public class DashField extends ValueField implements ItemListener {
  private static final long serialVersionUID = -1435164163638312884L;

  private static List<Measure<Length>> createDash(final double... dashArray) {
    final List<Measure<Length>> dashList = new ArrayList<Measure<Length>>();
    for (final double dash : dashArray) {
      dashList.add(Measure.valueOf(dash, NonSI.PIXEL));
    }
    return dashList;
  }

  private final JComboBox<List<Measure<Length>>> dashField;

  public DashField(final String fieldName, final List<Measure<Length>> dash) {
    super(fieldName, dash);
    setLayout(new SpringLayout());

    final Vector<List<Measure<Length>>> dashes = new Vector<List<Measure<Length>>>();
    dashes.add(null);
    for (final int i : new int[] {
      2, 5, 10, 15
    }) {
      dashes.add(createDash(i));
    }

    dashes.add(createDash(8, 3, 3, 3));
    dashes.add(createDash(8, 3, 3, 3, 3, 3));

    this.dashField = new JComboBox<List<Measure<Length>>>(dashes);
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
    if (event.getSource() == this.dashField
      && event.getStateChange() == ItemEvent.SELECTED) {
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
