package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BorderLayout;

import javax.swing.JSlider;

import com.revolsys.i18n.I18n;
import com.revolsys.swing.component.ValuePanel;

public class TransparencyPanel extends ValuePanel<Number> {

  /**
   * 
   */
  private static final long serialVersionUID = 3533569797414988165L;

  private final JSlider alphaSlider;

  public TransparencyPanel(final Number opacity) {
    super(new BorderLayout());
    setValue(opacity);
    alphaSlider = new JSlider(0, 255, opacity.intValue());
    alphaSlider.setMajorTickSpacing(32);
    alphaSlider.setMinorTickSpacing(8);
    alphaSlider.setPaintTicks(true);
    alphaSlider.setToolTipText(I18n.getString(getClass(), "transparency"));
    add(alphaSlider, BorderLayout.NORTH);
  }

  public int getOpacity() {
    return alphaSlider.getValue();
  }

  @Override
  public void save() {
    setValue(getOpacity());
  }
}
