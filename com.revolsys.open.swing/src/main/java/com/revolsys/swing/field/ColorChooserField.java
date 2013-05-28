package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JColorChooser;
import javax.swing.UIManager;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import org.jdesktop.swingx.JXColorSelectionButton;

import com.revolsys.swing.component.ColorAlphaPanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.listener.InvokeMethodListener;

public class ColorChooserField extends ValueField<Color> {
  private static final long serialVersionUID = 1L;

  private JXColorSelectionButton colorButton = new JXColorSelectionButton();

  public ColorChooserField(String fieldName, Color color) {
    super(fieldName, color);
    colorButton.addPropertyChangeListener("background",
      new InvokeMethodListener(this, "updateFieldValue"));
    setFieldValue(color);
    setLayout(new BorderLayout());
    add(colorButton, BorderLayout.CENTER);
    JColorChooser chooser = colorButton.getChooser();
    chooser.addChooserPanel(new ColorAlphaPanel());
  }

  @Override
  public void setFieldValue(Color color) {
    super.setFieldValue(color);
    if (colorButton != null) {
      colorButton.setBackground(color);
    }
  }

  public void updateFieldValue() {
    setFieldValue(colorButton.getBackground());
  }
}
