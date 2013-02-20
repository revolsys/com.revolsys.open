package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;

import com.revolsys.swing.component.ValuePanel;
import com.revolsys.swing.listener.InvokeMethodActionListener;

public class LengthMeasureTextField extends ValuePanel<Measure<Length>>
  implements ItemListener {
  /**
   * 
   */
  private static final long serialVersionUID = 6402788548005557723L;

  private Number number;

  private final JTextField valueField;

  private Unit<Length> unit;

  private final JComboBox unitField;

  public LengthMeasureTextField(final Measure<Length> value,
    final Unit<Length> unit) {
    super(new FlowLayout());
    this.number = value.getValue();
    setValue(value);
    valueField = new JFormattedTextField(new DecimalFormat("###0"));
    valueField.setColumns(4);
    if (value == null) {
      valueField.setText("0");
      if (unit == null) {
        this.unit = NonSI.PIXEL;
      } else {
        this.unit = unit;
      }
    } else {
      this.unit = value.getUnit();
      valueField.setText(String.valueOf(value.intValue(this.unit)));
    }
    add(valueField);
    valueField.addActionListener(new InvokeMethodActionListener(this,
      "updateNumber"));

    unitField = new JComboBox(new Object[] {
      NonSI.PIXEL, SI.METRE, SI.KILOMETRE, NonSI.FOOT, NonSI.MILE
    });
    unitField.addItemListener(this);
    unitField.setSelectedItem(unit);
    add(unitField);
  }

  public Measure<Length> getLength() {
    return Measure.valueOf(number.doubleValue(), unit);
  }

  public Number getNumber() {
    final String text = valueField.getText();
    if (text == null) {
      return 0.0;
    } else {
      return Double.parseDouble(text);
    }
  }

  public Unit<Length> getUnit() {
    return this.unit;
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getSource() == unitField && e.getStateChange() == ItemEvent.SELECTED) {
      setUnit((Unit<Length>)unitField.getSelectedItem());
    }
  }

  @Override
  public void save() {
    updateNumber();
    setValue(Measure.valueOf(number.doubleValue(), unit));
  }

  @Override
  public void setEnabled(final boolean enabled) {
    valueField.setEnabled(enabled);
    unitField.setEnabled(enabled);
  }

  public void setNumber(final Number value) {
    final Object oldValue = number;
    this.number = value.doubleValue();
    valueField.setText(value.toString());
    firePropertyChange("number", oldValue, number);
  }

  public void setText(final CharSequence text) {
    if (text == null) {
      valueField.setText(null);
    } else {
      valueField.setText(text.toString());
    }
  }

  public void setUnit(final Unit<Length> unit) {
    final Object oldValue = this.unit;
    this.unit = unit;
    unitField.setSelectedItem(this.unit);
    firePropertyChange("unit", oldValue, this.unit);

  }

  public void updateNumber() {
    final String text = valueField.getText();
    if (text == null) {
      setNumber(0.0);
    } else {
      setNumber(Integer.parseInt(text));
    }
  }
}
