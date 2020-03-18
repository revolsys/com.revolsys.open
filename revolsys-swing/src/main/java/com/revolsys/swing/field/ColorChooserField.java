package com.revolsys.swing.field;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;

import org.jdesktop.swingx.JXColorSelectionButton;

import com.revolsys.swing.EventQueue;
import com.revolsys.swing.component.ColorAlphaPanel;
import com.revolsys.swing.component.ValueField;

public class ColorChooserField extends ValueField {
  private static final long serialVersionUID = 1L;

  private final JXColorSelectionButton colorButton = new JXColorSelectionButton();

  public ColorChooserField(final String fieldName, final Color color) {
    super(fieldName, color);
    EventQueue.addPropertyChange(this.colorButton, "background", () -> updateFieldValue());
    setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 0));
    add(this.colorButton);
    final JColorChooser chooser = this.colorButton.getChooser();
    chooser.addChooserPanel(new ColorAlphaPanel());
    setFieldValue(color);
  }

  @Override
  public boolean setFieldValue(final Object color) {
    if (this.colorButton != null) {
      this.colorButton.setBackground((Color)color);
    }
    return super.setFieldValue(color);
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(this.colorButton.getBackground());
  }
}
