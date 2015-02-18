package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ListCellRenderer;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.swing.undo.UndoManager;
import com.revolsys.util.ExceptionUtil;

public class ComboBox extends JComboBox implements Field {
  public static <V> DefaultComboBoxModel<V> model(final Collection<V> items) {
    final Vector<V> vector = new Vector<V>(items);
    return new DefaultComboBoxModel<>(vector);
  }

  private static final long serialVersionUID = 1L;

  private final FieldSupport support;

  public ComboBox() {
    this("fieldValue", null);
  }

  public ComboBox(final boolean editable, final Object... items) {
    this(null, editable, items);
  }

  public ComboBox(final Collection<?> items) {
    this(null, false, items);
  }

  public ComboBox(final ComboBoxModel model) {
    this("fieldValue", model);
  }

  public ComboBox(final Object... items) {
    this(false, items);
  }

  public ComboBox(final ObjectToStringConverter converter, final boolean editable,
    final Collection<?> items) {
    super(new Vector<Object>(items));
    setEditable(editable);
    AutoCompleteDecorator.decorate(this, converter);
    if (converter instanceof ListCellRenderer) {
      final ListCellRenderer renderer = (ListCellRenderer)converter;
      setRenderer(renderer);
    }
    final JComponent editorComponent = (JComponent)getEditor().getEditorComponent();
    this.support = new FieldSupport(this, editorComponent, "fieldValue", null);
  }

  public ComboBox(final ObjectToStringConverter converter, final boolean editable,
    final Object... items) {
    this(converter, editable, Arrays.asList(items));
  }

  public ComboBox(final String fieldName, final ComboBoxModel model) {
    this(fieldName, model, ObjectToStringConverter.DEFAULT_IMPLEMENTATION);
  }

  public ComboBox(final String fieldName, final ComboBoxModel model,
    final ObjectToStringConverter converter) {
    this(fieldName, model, converter, null);
  }

  public ComboBox(final String fieldName, final ComboBoxModel model,
    final ObjectToStringConverter converter, final ListCellRenderer renderer) {
    super(model);
    setEditable(false);
    if (renderer != null) {
      setRenderer(renderer);
    }
    if (converter != null) {
      AutoCompleteDecorator.decorate(this, converter);
    }
    final JComponent editorComponent = (JComponent)getEditor().getEditorComponent();
    this.support = new FieldSupport(this, editorComponent, fieldName, null);
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Object selectedItem = getSelectedItem();
        setFieldValue(selectedItem);
      }
    });
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    final ComboBoxModel model = getModel();
    if (model instanceof Closeable) {
      ((Closeable)model).close();
    }
    super.finalize();
  }

  @Override
  public void firePropertyChange(final String propertyName, final Object oldValue,
    final Object newValue) {
    super.firePropertyChange(propertyName, oldValue, newValue);
  }

  @Override
  public String getFieldName() {
    return this.support.getName();
  }

  @Override
  public String getFieldValidationMessage() {
    return this.support.getErrorMessage();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getFieldValue() {
    return (T)getSelectedItem();
  }

  @Override
  public boolean isFieldValid() {
    return true;
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_BACKGROUND;
    }
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    component.setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = TextField.DEFAULT_FOREGROUND;
    }
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    component.setForeground(color);
  }

  @Override
  public void setFieldInvalid(final String message, final Color foregroundColor,
    final Color backgroundColor) {
    this.support.setFieldInvalid(message, foregroundColor, backgroundColor);
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    final ComboBoxEditor editor = getEditor();
    final JComponent component = (JComponent)editor.getEditorComponent();
    component.setToolTipText(toolTip);
  }

  @Override
  public void setFieldValid() {
    this.support.setFieldValid();
  }

  @Override
  public synchronized void setFieldValue(final Object value) {
    if (!EqualsRegistry.equal(getSelectedItem(), value)) {
      setSelectedItem(value);
    }
    this.support.setValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    if (this.support.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
  }

  @Override
  public void setUndoManager(final UndoManager undoManager) {
    this.support.setUndoManager(undoManager);
  }

  @Override
  public String toString() {
    return getFieldName() + "=" + getFieldValue();
  }

  @Override
  public void updateFieldValue() {
    setFieldValue(getSelectedItem());
  }
}
