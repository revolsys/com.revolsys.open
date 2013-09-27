package com.revolsys.swing.map.component;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JTextField;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;

public class SelectMapScaleEditor implements ComboBoxEditor {
  final ComboBoxEditor wrapped;

  final ObjectToStringConverter stringConverter;

  public SelectMapScaleEditor(final ComboBoxEditor editor,
    final ObjectToStringConverter stringConverter) {
    this.wrapped = editor;
    ((JTextField)editor.getEditorComponent()).setHorizontalAlignment(JTextField.RIGHT);
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
    final Object item = this.wrapped.getItem();
    String string = StringConverterRegistry.toString(item);
    string = string.replaceAll("((^1:)|([^0-9\\.])+)", "");
    final double scale = Double.parseDouble(string);
    return this.stringConverter.getPreferredStringForItem(scale);
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
