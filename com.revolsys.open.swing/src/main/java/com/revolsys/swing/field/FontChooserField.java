package com.revolsys.swing.field;

import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;

public class FontChooserField extends ComboBox {
  private static Font getFont(final String name) {
    Reference<Font> reference = fontCache.get(name);
    if (reference != null) {
      final Font font = reference.get();
      if (font != null) {
        return font;
      }
    }
    final Font font = new Font(name, Font.PLAIN, 16);
    reference = new WeakReference<>(font);
    fontCache.put(name, reference);
    return font;
  }

  public static String[] getFontNames() {
    return GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getAvailableFontFamilyNames();
  }

  private static final long serialVersionUID = 1L;

  private static final String[] FONT_NAMES = getFontNames();

  private static final Map<String, Reference<Font>> fontCache = new HashMap<>();

  private static final ListCellRenderer RENDERER = new DefaultListCellRenderer() {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent(final JList list,
      final Object value, final int index, final boolean isSelected,
      final boolean cellHasFocus) {
      final String fontName = StringConverterRegistry.toString(value);
      final Font font = FontChooserField.getFont(fontName);

      final Component renderer = super.getListCellRendererComponent(list,
        value, index, isSelected, cellHasFocus);
      renderer.setFont(font);
      return renderer;
    }
  };

  public FontChooserField(final String fieldName, final String fontName) {
    super(fieldName, new DefaultComboBoxModel(FONT_NAMES),
      ObjectToStringConverter.DEFAULT_IMPLEMENTATION, RENDERER);
    setSelectedItem(fontName);
  }
}
