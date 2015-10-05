package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.equals.Equals;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Strings;

public class ComboBox<T> extends JComboBox<T>implements Field, KeyListener {
  private static final long serialVersionUID = 1L;

  public static <V> ArrayListComboBoxModel<V> model(final Collection<V> items) {
    final Vector<V> vector = new Vector<V>(items);
    return new ArrayListComboBoxModel<>(vector);
  }

  public static <V> ComboBox<V> newComboBox(final String fieldName, final Collection<V> items) {
    final ArrayListComboBoxModel<V> model = model(items);
    return new ComboBox<>(fieldName, model);
  }

  private final FieldSupport fieldSupport;

  public ComboBox() {
    this("fieldValue");
  }

  public ComboBox(final boolean editable, final T... items) {
    this(null, editable, items);
  }

  public ComboBox(final Collection<T> items) {
    this(null, false, items);
  }

  public ComboBox(final ComboBoxModel<T> model) {
    this("fieldValue", model);
  }

  public ComboBox(final ObjectToStringConverter converter, final boolean editable,
    final Collection<T> items) {
    super(new Vector<T>(items));
    setEditable(editable);
    AutoCompleteDecorator.decorate(this, converter);
    if (converter instanceof ListCellRenderer) {
      final ListCellRenderer renderer = (ListCellRenderer)converter;
      setRenderer(renderer);
    }
    final JComponent editorComponent = (JComponent)getEditor().getEditorComponent();
    this.fieldSupport = new FieldSupport(this, editorComponent, "fieldValue", null);
  }

  public ComboBox(final ObjectToStringConverter converter, final boolean editable,
    final T... items) {
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
    this.fieldSupport = new FieldSupport(this, editorComponent, fieldName, null);
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Object selectedItem = getSelectedItem();
        setFieldValue(selectedItem);
      }
    });
  }

  public ComboBox(final String fieldName, final T... items) {
    this(fieldName, new ArrayListComboBoxModel<T>(items),
      ObjectToStringConverter.DEFAULT_IMPLEMENTATION);
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
  public synchronized void setFieldValue(final Object value) {
    if (!Equals.equal(getSelectedItem(), value)) {
      setSelectedItem(value);
    }
    this.fieldSupport.setValue(value);
  }

  @Override
  public void setToolTipText(final String text) {
    final FieldSupport fieldSupport = getFieldSupport();
    if (fieldSupport.setOriginalTooltipText(text)) {
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
