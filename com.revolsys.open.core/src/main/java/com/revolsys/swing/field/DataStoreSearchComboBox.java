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

  private ComboBoxEditor comboBoxEditor;

  private String displayAttributeName;

  public DataStoreSearchComboBox(DataObjectStore dataStore, String tableName,
    String whereClause, String displayAttributeName) {
    super(new DataStoreSearchComboBoxModel(dataStore, tableName, whereClause));
    this.displayAttributeName = displayAttributeName;
    ResultPagerListCellRenderer renderer = new ResultPagerListCellRenderer(
      displayAttributeName);
    setRenderer(renderer);

    setEditable(true);
    addItemListener(this);
    comboBoxEditor = getEditor();
    Component editorComponent = editor.getEditorComponent();
    editorComponent.addKeyListener(this);

    AutoCompleteComboBoxEditor wrappedEditor = new AutoCompleteComboBoxEditor(
      editor, new DataObjectToStringConverter(displayAttributeName));
    setEditor(wrappedEditor);
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
  }

  @Override
  public DataObject getSelectedItem() {
    return (DataObject)super.getSelectedItem();
  }

  @Override
  public DataStoreSearchComboBoxModel getModel() {
    return (DataStoreSearchComboBoxModel)super.getModel();
  }

  public void keyReleased(KeyEvent e) {
    String str = comboBoxEditor.getItem().toString();
    JTextField textField = (JTextField)comboBoxEditor.getEditorComponent();
    textPosition = textField.getCaretPosition();

    if (e.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
      if (e.getKeyCode() != KeyEvent.VK_ENTER) {
        comboBoxEditor.setItem(str);
        textField.setCaretPosition(textPosition);
      }
    } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      setSelectedIndex(getSelectedIndex());
    } else {
      DataStoreSearchComboBoxModel model = getModel();
      model.updateModel(comboBoxEditor.getItem().toString());
      hidePopup();
      showPopup();
      comboBoxEditor.setItem(str);
      textField.setCaretPosition(textPosition);
    }
  }

  public void itemStateChanged(ItemEvent e) {
    if (e.getStateChange() == ItemEvent.SELECTED) {
      Object item = e.getItem();
      if (item instanceof DataObject) {
        DataObject object = (DataObject)item;
        Object name = object.getValue(displayAttributeName);
        comboBoxEditor.setItem(name);
        setSelectedItem(item);
      } else {
        comboBoxEditor.setItem(item.toString());
        setSelectedItem(item);
      }
    }
  }

  public String getText() {
    return (String)comboBoxEditor.getItem();
  }
}
