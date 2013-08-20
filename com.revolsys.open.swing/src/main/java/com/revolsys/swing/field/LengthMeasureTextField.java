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
    this.valueField = new NumberTextField(fieldName, DataTypes.DOUBLE, 6, 2);
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
    this.valueField.setFieldValue(this.number);
    final InvokeMethodListener updateNumberListener = new InvokeMethodListener(
      this, "updateNumber");
    this.valueField.addFocusListener(updateNumberListener);
    add(this.valueField);
    this.valueField.addActionListener(updateNumberListener);

    this.unitField = new ComboBox(NonSI.PIXEL, SI.METRE, SI.KILOMETRE,
      NonSI.FOOT, NonSI.MILE);
    this.unitField.addItemListener(this);
    this.unitField.setSelectedItem(unit);
    add(this.unitField);
    GroupLayoutUtil.makeColumns(this, 2);
  }

  public Measure<Length> getLength() {
    return Measure.valueOf(this.number.doubleValue(), this.unit);
  }

  public Number getNumber() {
    final String text = this.valueField.getText();
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
    if (e.getSource() == this.unitField
      && e.getStateChange() == ItemEvent.SELECTED) {
      setUnit((Unit<Length>)this.unitField.getSelectedItem());
    }
  }

  @Override
  public void save() {
    updateNumber();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    this.valueField.setEnabled(enabled);
    this.unitField.setEnabled(enabled);
  }

  public void setNumber(final Number value) {
    final Object oldValue = this.number;
    this.number = value.doubleValue();
    this.valueField.setText(value.toString());
    firePropertyChange("number", oldValue, this.number);
    setFieldValue(Measure.valueOf(this.number.doubleValue(), this.unit));
  }

  public void setText(final CharSequence text) {
    if (text == null) {
      this.valueField.setText(null);
    } else {
      this.valueField.setText(text.toString());
    }
  }

  public void setUnit(final Unit<Length> unit) {
    final Object oldValue = this.unit;
    this.unit = unit;
    this.unitField.setSelectedItem(this.unit);
    firePropertyChange("unit", oldValue, this.unit);
    setFieldValue(Measure.valueOf(this.number.doubleValue(), unit));
  }

  public void updateNumber() {
    final Number number = this.valueField.getFieldValue();
    if (number == null) {
      setNumber(0);
    } else {
      setNumber(number);
    }
  }
}
