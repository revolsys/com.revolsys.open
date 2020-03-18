package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.exception.Exceptions;

import com.revolsys.swing.list.renderer.LambdaListCellRenderer;
import com.revolsys.util.Strings;

public class BaseComboBox<T> extends JComboBox<T> implements Field, KeyListener {
  private static final long serialVersionUID = 1L;

  public static <V> BaseComboBox<V> newComboBox(final String fieldName, final Collection<V> items) {
    final ComboBoxModel<V> model = newModel(items);
    return newComboBox(fieldName, model);
  }

  public static <V, R> BaseComboBox<V> newComboBox(final String fieldName,
    final Collection<V> items, final Function<V, R> converter) {
    final ComboBoxModel<V> model = newModel(items);
    return newComboBox(fieldName, model, converter);
  }

  public static <V> BaseComboBox<V> newComboBox(final String fieldName, final Collection<V> items,
    final ListCellRenderer<V> renderer) {
    final ComboBoxModel<V> model = newModel(items);
    return new BaseComboBox<>(fieldName, model, renderer);
  }

  public static <V> BaseComboBox<V> newComboBox(final String fieldName,
    final ComboBoxModel<V> model) {
    return newComboBox(fieldName, model, (Function<V, V>)null);
  }

  public static <V, R> BaseComboBox<V> newComboBox(final String fieldName,
    final ComboBoxModel<V> model, final Function<V, R> converter) {
    final LambdaListCellRenderer<V, R> renderer = LambdaListCellRenderer.newRenderer(converter);
    return new BaseComboBox<>(fieldName, model, renderer);
  }

  public static <V> BaseComboBox<V> newComboBox(final String fieldName,
    final ComboBoxModel<V> model, final ListCellRenderer<V> renderer) {
    return new BaseComboBox<>(fieldName, model, renderer);
  }

  @SuppressWarnings("unchecked")
  public static <V> BaseComboBox<V> newComboBox(final String fieldName, final V... items) {
    return newComboBox(fieldName, Arrays.asList(items));
  }

  public static <V> ArrayListComboBoxModel<V> newModel(final Collection<V> items) {
    return new ArrayListComboBoxModel<>(items);
  }

  @SuppressWarnings("unchecked")
  public static <V> ArrayListComboBoxModel<V> newModel(final V... items) {
    return new ArrayListComboBoxModel<>(items);
  }

  private final FieldSupport fieldSupport;

  public BaseComboBox(final String fieldName, final ComboBoxModel<T> model,
    final Function<T, ? extends Object> converter) {
    this(fieldName, model, LambdaListCellRenderer.newRenderer(converter));
  }

  public BaseComboBox(final String fieldName, final ComboBoxModel<T> model,
    final ListCellRenderer<T> renderer) {
    super(model);
    setEditable(false);
    setRenderer(renderer);

    final JComponent editorComponent = (JComponent)getEditor().getEditorComponent();
    this.fieldSupport = new FieldSupport(this, editorComponent, fieldName, null, true);
    addActionListener((final ActionEvent e) -> {
      final Object selectedItem = getSelectedItem();
      setFieldValue(selectedItem);
    });
  }

  @Override
  public Field clone() {
    try {
      return (Field)super.clone();
    } catch (final CloneNotSupportedException e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  @Override
  protected void finalize() throws Throwable {
    final ComboBoxModel<T> model = getModel();
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

  @SuppressWarnings("unchecked")
  public <V extends ComboBoxModel<T>> V getComboBoxModel() {
    return (V)super.getModel();
  }

  @Override
  public Color getFieldSelectedTextColor() {
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    if (component instanceof JTextComponent) {
      final JTextComponent textField = (JTextComponent)component;
      return textField.getSelectedTextColor();
    } else {
      return Field.DEFAULT_SELECTED_FOREGROUND;
    }
  }

  @Override
  public FieldSupport getFieldSupport() {
    return this.fieldSupport;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T getSelectedItem() {
    return (T)super.getSelectedItem();
  }

  @Override
  public void keyPressed(final KeyEvent e) {
  }

  @Override
  public void keyReleased(final KeyEvent e) {
  }

  @Override
  public void keyTyped(final KeyEvent e) {
    final char keyChar = e.getKeyChar();
    if (keyChar == KeyEvent.VK_BACK_SPACE || keyChar == KeyEvent.VK_DELETE) {
      final Component editorComponent = getEditor().getEditorComponent();
      if (editorComponent instanceof JTextField) {
        final JTextField textField = (JTextField)editorComponent;
        final String selectedText = textField.getSelectedText();
        final String text = textField.getText();
        if (Strings.equals(selectedText, text)) {
          setSelectedItem(null);
        }
      }
    }
  }

  @Override
  public void setEditor(final ComboBoxEditor editor) {
    final ComboBoxEditor oldEditor = getEditor();
    super.setEditor(editor);
    editor.getEditorComponent().addKeyListener(this);
    if (oldEditor != null) {
      oldEditor.getEditorComponent().removeKeyListener(this);
    }
  }

  @Override
  public void setFieldBackgroundColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_BACKGROUND;
    }
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    component.setBackground(color);
  }

  @Override
  public void setFieldForegroundColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_FOREGROUND;
    }
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    component.setForeground(color);
  }

  @Override
  public void setFieldSelectedTextColor(Color color) {
    if (color == null) {
      color = Field.DEFAULT_SELECTED_FOREGROUND;
    }
    final ComboBoxEditor editor = getEditor();
    final Component component = editor.getEditorComponent();
    if (component instanceof JTextComponent) {
      final JTextComponent textField = (JTextComponent)component;
      textField.setSelectedTextColor(color);
    }
  }

  @Override
  public void setFieldToolTip(final String toolTip) {
    final ComboBoxEditor editor = getEditor();
    final JComponent component = (JComponent)editor.getEditorComponent();
    component.setToolTipText(toolTip);
  }

  @Override
  public synchronized boolean setFieldValue(final Object value) {
    if (!DataType.equal(getSelectedItem(), value)) {
      setSelectedItem(value);
    }
    return this.fieldSupport.setValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    final FieldSupport fieldSupport = getFieldSupport();
    if (fieldSupport == null || fieldSupport.setOriginalTooltipText(text)) {
      super.setToolTipText(text);
    }
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
