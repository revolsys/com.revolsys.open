package com.revolsys.swing.field;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.jdesktop.swingx.autocomplete.AutoCompleteComboBoxEditor;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.list.ResultPagerListCellRenderer;

public class DataStoreSearchComboBox extends JComboBox implements ItemListener,
  KeyListener {
  private static final long serialVersionUID = 1L;

  private int textPosition;

  private final ComboBoxEditor comboBoxEditor;

  private final String displayAttributeName;

  public DataStoreSearchComboBox(final DataObjectStore dataStore,
    final String tableName, final String whereClause,
    final String displayAttributeName) {
    super(new DataStoreSearchComboBoxModel(dataStore, tableName, whereClause,
      displayAttributeName));
    this.displayAttributeName = displayAttributeName;
    final ResultPagerListCellRenderer renderer = new ResultPagerListCellRenderer(
      displayAttributeName);
    setRenderer(renderer);

    setEditable(true);
    addItemListener(this);
    comboBoxEditor = getEditor();
    final Component editorComponent = editor.getEditorComponent();
    editorComponent.addKeyListener(this);

    final AutoCompleteComboBoxEditor wrappedEditor = new AutoCompleteComboBoxEditor(
      editor, new DataObjectToStringConverter(displayAttributeName));
    setEditor(wrappedEditor);
  }

  @Override
  public DataStoreSearchComboBoxModel getModel() {
    return (DataStoreSearchComboBoxModel)super.getModel();
  }

  @Override
  public DataObject getSelectedItem() {
    return (DataObject)super.getSelectedItem();
  }

  public String getText() {
    return (String)comboBoxEditor.getItem();
  }

  @Override
  public void itemStateChanged(final ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      final Object item = e.getItem();
      if (item instanceof DataObject) {
        final DataObject object = (DataObject)item;
        final Object name = object.getValue(displayAttributeName);
        comboBoxEditor.setItem(name);
        setSelectedItem(item);
      } else {
        comboBoxEditor.setItem(item.toString());
        setSelectedItem(item);
      }
    }
  }

  @Override
  public void keyPressed(final KeyEvent e) {
  }

  @Override
  public void keyReleased(final KeyEvent e) {
    final String str = comboBoxEditor.getItem().toString();
    final JTextField textField = (JTextField)comboBoxEditor.getEditorComponent();
    textPosition = textField.getCaretPosition();

    if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
      if (e.getKeyCode() != KeyEvent.VK_ENTER) {
        comboBoxEditor.setItem(str);
        textField.setCaretPosition(textPosition);
      }
    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      setSelectedIndex(getSelectedIndex());
    } else {
      final DataStoreSearchComboBoxModel model = getModel();
      model.updateModel(comboBoxEditor.getItem().toString());
      hidePopup();
      showPopup();
      comboBoxEditor.setItem(str);
      textField.setCaretPosition(textPosition);
    }
  }

  @Override
  public void keyTyped(final KeyEvent e) {
  }
}
