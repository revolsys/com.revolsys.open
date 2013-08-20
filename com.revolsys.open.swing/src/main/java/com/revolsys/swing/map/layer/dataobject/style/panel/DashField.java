package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.swing.SpringLayout;

import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.layout.SpringLayoutUtil;

public class DashField extends ValueField implements ItemListener {

  /**
   * 
   */
  private static final long serialVersionUID = -1435164163638312884L;

  private static List<Measure<Length>> createDash(final double... dashArray) {
    final List<Measure<Length>> dashList = new ArrayList<Measure<Length>>();
    for (final double dash : dashArray) {
      dashList.add(Measure.valueOf(dash, NonSI.PIXEL));
    }
    return dashList;
  }

  private List<Measure<Length>> dash;

  private final ComboBox dashField;

  public DashField(final List<Measure<Length>> dash) {
    setLayout(new SpringLayout());
    setFieldValue(dash);
    this.dash = dash;

    this.dashField = new ComboBox(null, createDash(1), createDash(3),
      createDash(5), createDash(7), createDash(1, 3), createDash(3, 5),
      createDash(5, 7), createDash(1, 2, 4, 2), createDash(3, 5, 9, 5));
    this.dashField.setSelectedItem(dash);
    this.dashField.setRenderer(new DashListCellRenderer());
    this.dashField.addItemListener(this);

    add(this.dashField);
    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 5, 5);
  }

  public List<Measure<Length>> getDash() {
    return (List<Measure<Length>>)this.dashField.getSelectedItem();
  }

  @Override
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == this.dashField
      && event.getStateChange() == ItemEvent.SELECTED) {
      final Object oldValue = this.dash;
      this.dash = (List<Measure<Length>>)this.dashField.getSelectedItem();
      firePropertyChange("dash", oldValue, this.dash);
    }
  }

  @Override
  public void save() {
    final List<Measure<Length>> dash = getDash();
    setFieldValue(dash);
  }
}
