package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import javax.swing.SpringLayout;

import com.revolsys.swing.component.ValuePanel;
import com.revolsys.swing.layout.SpringLayoutUtil;

public class LengthMeasurePanel extends ValuePanel<Measure<Length>> implements
  PropertyChangeListener {

  /**
   * 
   */
  private static final long serialVersionUID = -4800722863109968797L;

  private Unit<Length> unit;

  private final LengthMeasureTextField valueTextField;

  public LengthMeasurePanel(final Measure<Length> value,
    final int minimumValue, final int maximumValue, final int majorTickSpacing,
    final boolean optional, final Unit<Length> unit) {
    super(new SpringLayout());
    this.unit = value.getUnit();
    setValue(value);

    valueTextField = new LengthMeasureTextField(getValue(), this.unit);
    valueTextField.addPropertyChangeListener("number", this);
    valueTextField.addPropertyChangeListener("unit", this);
    add(valueTextField);

    SpringLayoutUtil.makeColumns(this, 3, 0, 0, 5, 0);
  }

  public Measure<Length> getLength() {
    return valueTextField.getLength();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    if (evt.getSource() == valueTextField) {
      this.unit = valueTextField.getUnit();
      firePropertyChange(evt.getPropertyName(), evt.getOldValue(),
        evt.getNewValue());
    }

  }

  @Override
  public void save() {
    super.save();
    setValue(valueTextField.getValue());
  }
}
