package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.swing.JComboBox;
import javax.swing.SpringLayout;

import com.revolsys.swing.component.ValuePanel;
import com.revolsys.swing.layout.SpringLayoutUtil;

public class DashField extends ValuePanel<List<Measure<Length>>> implements
  ItemListener {

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

  private final JComboBox dashField;

  public DashField(final List<Measure<Length>> dash) {
    setLayout(new SpringLayout());
    setValue(dash);
    this.dash = dash;

    dashField = new JComboBox(new Object[] {
      null, createDash(1), createDash(3), createDash(5), createDash(7),
      createDash(1, 3), createDash(3, 5), createDash(5, 7),
      createDash(1, 2, 4, 2), createDash(3, 5, 9, 5)
    });
    dashField.setSelectedItem(dash);
    dashField.setRenderer(new DashListCellRenderer());
    dashField.addItemListener(this);

    add(dashField);
    SpringLayoutUtil.makeColumns(this, 2, 0, 0, 5, 5);
  }

  public List<Measure<Length>> getDash() {
    return (List<Measure<Length>>)dashField.getSelectedItem();
  }

  @Override
  public void itemStateChanged(final ItemEvent event) {
    if (event.getSource() == dashField
      && event.getStateChange() == ItemEvent.SELECTED) {
      final Object oldValue = this.dash;
      this.dash = (List<Measure<Length>>)dashField.getSelectedItem();
      firePropertyChange("dash", oldValue, dash);
    }
  }

  @Override
  public void save() {
    final List<Measure<Length>> dash = getDash();
    setValue(dash);
  }
}
