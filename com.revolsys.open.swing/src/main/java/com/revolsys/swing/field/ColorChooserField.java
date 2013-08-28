package com.revolsys.swing.field;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;

import org.jdesktop.swingx.JXColorSelectionButton;
import org.jdesktop.swingx.VerticalLayout;

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
    setLayout(new VerticalLayout());
    setBorder(BorderFactory.createEmptyBorder(0, 3, 3, 0));
    add(this.colorButton);
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
