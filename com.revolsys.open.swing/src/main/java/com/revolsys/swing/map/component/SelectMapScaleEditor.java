package com.revolsys.swing.map.component;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.jeometry.common.data.type.DataTypes;

public class SelectMapScaleEditor implements ComboBoxEditor {
  final ObjectToStringConverter stringConverter;

  final ComboBoxEditor wrapped;

  public SelectMapScaleEditor(final ComboBoxEditor editor,
    final ObjectToStringConverter stringConverter) {
    this.wrapped = editor;
    ((JTextField)editor.getEditorComponent()).setHorizontalAlignment(SwingConstants.RIGHT);
    this.stringConverter = stringConverter;
  }

  @Override
  public void addActionListener(final ActionListener l) {
    this.wrapped.addActionListener(l);
  }

  @Override
  public Component getEditorComponent() {
    return this.wrapped.getEditorComponent();
  }

  @Override
  public Object getItem() {
    try {
      final Object item = this.wrapped.getItem();
      String string = DataTypes.toString(item);
      string = string.replaceAll("((^1:)|([^0-9\\.])+)", "");
      final double scale = Double.parseDouble(string);
      return this.stringConverter.getPreferredStringForItem(scale);
    } catch (final NumberFormatException e) {
      return "";
    }
  }

  @Override
  public void removeActionListener(final ActionListener l) {
    this.wrapped.removeActionListener(l);
  }

  @Override
  public void selectAll() {
    this.wrapped.selectAll();
  }

  @Override
  public void setItem(final Object object) {
    this.wrapped.setItem(this.stringConverter.getPreferredStringForItem(object));
  }
}
