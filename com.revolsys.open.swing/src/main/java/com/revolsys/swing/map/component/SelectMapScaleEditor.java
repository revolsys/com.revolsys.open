package com.revolsys.swing.map.component;

import java.awt.Component;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxEditor;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;

public class SelectMapScaleEditor implements ComboBoxEditor {
  final ComboBoxEditor wrapped;

  final ObjectToStringConverter stringConverter;

  private Object oldItem;

  public SelectMapScaleEditor(final ComboBoxEditor editor,
    final ObjectToStringConverter stringConverter) {
    this.wrapped = editor;
    this.stringConverter = stringConverter;
  }

  @Override
  public void addActionListener(final ActionListener l) {
    wrapped.addActionListener(l);
  }

  @Override
  public Component getEditorComponent() {
    return wrapped.getEditorComponent();
  }

  @Override
  public Object getItem() {
    final Object item = wrapped.getItem();
    String string = StringConverterRegistry.toString(item);
    string = string.replaceAll("((^1:)|([^0-9\\.])+)", "");
    final double scale = Double.parseDouble(string);
    return stringConverter.getPreferredStringForItem(scale);
  }

  @Override
  public void removeActionListener(final ActionListener l) {
    wrapped.removeActionListener(l);
  }

  @Override
  public void selectAll() {
    wrapped.selectAll();
  }

  @Override
  public void setItem(final Object object) {
    this.oldItem = object;
    wrapped.setItem(stringConverter.getPreferredStringForItem(object));
  }
}
