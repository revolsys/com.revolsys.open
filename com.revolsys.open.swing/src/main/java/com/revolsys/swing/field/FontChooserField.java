package com.revolsys.swing.field;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;

public class FontChooserField extends ComboBox {
  private static final long serialVersionUID = 1L;

  private static final ListCellRenderer RENDERER = new DefaultListCellRenderer() {
    @Override
    public Component getListCellRendererComponent(final JList list,
      final Object value, final int index, final boolean isSelected,
      final boolean cellHasFocus) {
      final String fontName = StringConverterRegistry.toString(value);
      final Font font = new Font(fontName, Font.PLAIN, 16);

      final Component renderer = super.getListCellRendererComponent(list,
        value, index, isSelected, cellHasFocus);
      renderer.setFont(font);
      return renderer;
    }
  };

  public static String[] getFontNames() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment()
      .getAvailableFontFamilyNames();
  }

  public FontChooserField(final String fieldName, final String fontName) {
    super(fieldName, new DefaultComboBoxModel(getFontNames()),
      ObjectToStringConverter.DEFAULT_IMPLEMENTATION, RENDERER);
    setSelectedItem(fontName);
  }
}
