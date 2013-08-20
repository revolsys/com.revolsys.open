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
    this.colorButton.addPropertyChangeListener("background",
      new InvokeMethodListener(this, "updateFieldValue"));
    setFieldValue(color);
    setLayout(new BorderLayout());
    add(this.colorButton, BorderLayout.CENTER);
    final JColorChooser chooser = this.colorButton.getChooser();
    chooser.addChooserPanel(new ColorAlphaPanel());
  }

  @Override
  public void setFieldValue(final Object color) {
    super.setFieldValue(color);
    if (this.colorButton != null) {
      this.colorButton.setBackground((Color)color);
    }
  }

  public void updateFieldValue() {
    setFieldValue(this.colorButton.getBackground());
  }
}
