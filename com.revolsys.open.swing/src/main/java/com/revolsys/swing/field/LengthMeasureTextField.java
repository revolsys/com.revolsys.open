package com.revolsys.swing.field;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.listener.InvokeMethodListener;

public class LengthMeasureTextField extends ValueField implements ItemListener {
  /**
   * 
   */
  private static final long serialVersionUID = 6402788548005557723L;

  private Number number;

  private final NumberTextField valueField;

  private Unit<Length> unit;

  private final ComboBox unitField;

  public LengthMeasureTextField(final Measure<Length> value,
    final Unit<Length> unit) {
    this(null, value, unit);
  }

  public LengthMeasureTextField(final String fieldName,
    final Measure<Length> value) {
    this(fieldName, value, value.getUnit());
  }

  public LengthMeasureTextField(final String fieldName,
    final Measure<Length> value, final Unit<Length> unit) {
    super(fieldName, value);
    valueField = new NumberTextField(fieldName, DataTypes.DOUBLE, 6, 2);
    if (value == null) {
      this.number = 0;
      if (unit == null) {
        this.unit = NonSI.PIXEL;
      } else {
        this.unit = unit;
      }
    } else {
      this.number = value.getValue();
      this.unit = value.getUnit();
    }
    valueField.setFieldValue(this.number);
    final InvokeMethodListener updateNumberListener = new InvokeMethodListener(
      this, "updateNumber");
    valueField.addFocusListener(updateNumberListener);
    add(valueField);
    valueField.addActionListener(updateNumberListener);

    unitField = new ComboBox(NonSI.PIXEL, SI.METRE, SI.KILOMETRE, NonSI.FOOT,
      NonSI.MILE);
    unitField.addItemListener(this);
    unitField.setSelectedItem(unit);
    add(unitField);
    GroupLayoutUtil.makeColumns(this, 2);
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

  @SuppressWarnings("unchecked")
  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getSource() == unitField && e.getStateChange() == ItemEvent.SELECTED) {
      setUnit((Unit<Length>)unitField.getSelectedItem());
    }
  }

  @Override
  public void save() {
    updateNumber();
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
    setFieldValue(Measure.valueOf(number.doubleValue(), unit));
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
    setFieldValue(Measure.valueOf(number.doubleValue(), unit));
  }

  public void updateNumber() {
    final Number number = valueField.getFieldValue();
    if (number == null) {
      setNumber(0);
    } else {
      setNumber(number);
    }
  }
}
