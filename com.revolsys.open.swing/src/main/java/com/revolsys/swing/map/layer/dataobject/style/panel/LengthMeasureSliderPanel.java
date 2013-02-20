package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.JCheckBox;
import javax.swing.JSlider;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.revolsys.i18n.I18n;
import com.revolsys.swing.component.ValuePanel;
import com.revolsys.swing.layout.SpringLayoutUtil;

public class LengthMeasureSliderPanel extends ValuePanel<Measure<Length>>
  implements ChangeListener, PropertyChangeListener {

  /**
   * 
   */
  private static final long serialVersionUID = -5316103672110745057L;

  private JCheckBox optionalField;

  private Unit<Length> unit;

  private final JSlider valueSliderField;

  private final LengthMeasureTextField valueTextField;

  public LengthMeasureSliderPanel(final Measure<Length> value,
    final int minimumValue, final int maximumValue, final int majorTickSpacing,
    final boolean optional, final Unit<Length> unit) {
    super(new SpringLayout());
    if (value == null) {
      if (unit == null) {
        this.unit = NonSI.PIXEL;
      } else {
        this.unit = unit;
      }
    } else {
      this.unit = value.getUnit();
    }
    setValue(value);

    if (optional) {
      optionalField = new JCheckBox();
      optionalField.addChangeListener(this);
      add(optionalField);
    } else if (value == null) {
      setValue(Measure.valueOf(0.0, this.unit));
    }

    int intValue = 0;
    if (getValue() != null) {
      intValue = getValue().getValue().intValue();
    }
    valueSliderField = new JSlider(minimumValue, maximumValue, intValue);
    valueSliderField.setMajorTickSpacing(majorTickSpacing);
    valueSliderField.setPaintTicks(true);
    valueSliderField.setPaintLabels(true);
    valueSliderField.setToolTipText(I18n.getString(getClass(), "size"));
    valueSliderField.addChangeListener(this);
    valueSliderField.setPreferredSize(new Dimension(300,
      (int)valueSliderField.getPreferredSize().getHeight()));
    add(valueSliderField);

    valueTextField = new LengthMeasureTextField(getValue(), this.unit);
    valueTextField.addPropertyChangeListener("unit", this);
    add(valueTextField);

    if (optionalField != null) {
      final boolean enabled = value != null;
      optionalField.setSelected(enabled);
      valueSliderField.setEnabled(enabled);
      valueTextField.setEnabled(enabled);
    }
    SpringLayoutUtil.makeColumns(this, 3, 0, 0, 5, 0);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
    if (evt.getSource() == valueTextField) {
      this.unit = valueTextField.getUnit();
    }

  }

  @Override
  public void save() {
    if (optionalField == null || optionalField.isSelected()) {
      setValue(Measure.valueOf((double)valueSliderField.getValue(), unit));
    } else {
      setValue(null);
    }

  }

  @Override
  public void stateChanged(final ChangeEvent e) {
    if (e.getSource() == valueSliderField) {
      valueTextField.setNumber(valueSliderField.getValue());
    } else if (e.getSource() == optionalField) {
      final boolean enabled = optionalField.isSelected();
      valueSliderField.setEnabled(enabled);
      valueTextField.setEnabled(enabled);
    }

  }
}
