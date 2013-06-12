package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JColorChooser;

import org.jdesktop.swingx.JXColorSelectionButton;

import com.revolsys.swing.component.ColorAlphaPanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.listener.InvokeMethodListener;

public class ColorChooserField extends ValueField {
  private static final long serialVersionUID = 1L;

  private final JXColorSelectionButton colorButton = new JXColorSelectionButton();

  public ColorChooserField(final String fieldName, final Color color) {
    super(fieldName, color);
    colorButton.addPropertyChangeListener("background",
      new InvokeMethodListener(this, "updateFieldValue"));
    setFieldValue(color);
    setLayout(new BorderLayout());
    add(colorButton, BorderLayout.CENTER);
    final JColorChooser chooser = colorButton.getChooser();
    chooser.addChooserPanel(new ColorAlphaPanel());
  }

  @Override
  public void setFieldValue(final Object color) {
    super.setFieldValue(color);
    if (colorButton != null) {
      colorButton.setBackground((Color)color);
    }
  }

  public void updateFieldValue() {
    setFieldValue(colorButton.getBackground());
  }
}
