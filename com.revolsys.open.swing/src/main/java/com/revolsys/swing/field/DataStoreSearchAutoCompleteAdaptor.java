package com.revolsys.swing.field;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.autocomplete.AbstractAutoCompleteAdaptor;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.query.Query;
import com.revolsys.io.Reader;

public class DataStoreSearchAutoCompleteAdaptor extends
  AbstractAutoCompleteAdaptor implements CaretListener {

  private String whereClause;

  private final DataObjectStore dataStore;

  private List<DataObject> objects = new ArrayList<DataObject>();

  private DataStoreSearchTextField textComponent;

  private DataObject selectedItem;

  private Query query;

  private String displayAttributeName;

  public DataStoreSearchAutoCompleteAdaptor(
    DataStoreSearchTextField textComponent, final DataObjectStore dataStore,
    final String tableName, final String whereClause, String displayAttributeName) {
    textComponent.addCaretListener(this);
    this.textComponent = textComponent;
    this.dataStore = dataStore;
    this.whereClause = whereClause;
    this.query = new Query(tableName);
    query.setWhereClause(whereClause);
    if (StringUtils.hasText(displayAttributeName)) {
      query.setOrderByColumns(displayAttributeName);
    }
    query.setLimit(100);
    this.displayAttributeName = displayAttributeName;
  }

  @Override
  public DataObject getSelectedItem() {
    return selectedItem;
  }

  @Override
  public void setSelectedItem(Object selectedItem) {
    if (selectedItem instanceof DataObject) {
      this.selectedItem = (DataObject)selectedItem;
      System.out.println(this.selectedItem.getIdValue());
    } else if (selectedItem != null) {
      search(selectedItem.toString());
    }
  }

  @Override
  public int getItemCount() {
    return objects.size();
  }

  @Override
  public Object getItem(int index) {
    return objects.get(index);
  }

  @Override
  public JTextComponent getTextComponent() {
    return textComponent;
  }

  @Override
  public void caretUpdate(CaretEvent e) {
    int start = e.getDot();
    int end = e.getMark();
    if (start != end) {
      try {
        String text;
        if (start > end) {
          text = textComponent.getText(end, start - end);
        } else {
          text = textComponent.getText(start, end - start);
        }
        search(text);
      } catch (BadLocationException e1) {
        e1.printStackTrace();
      }
    }
  }

  protected void search(String text) {
    if (StringUtils.hasText(text)) {
      Query query = this.query.clone();
      int index = 0;
      do {
        index = whereClause.indexOf('?', index);
        if (index != -1) {
          query.addParameter(text.toUpperCase() + "%");
          index++;
        }
      } while (index != -1);
      final Reader<DataObject> reader = dataStore.query(query);
      try {
        objects = reader.read();
        JPopupMenu findPopupMenu = textComponent.getFindPopupMenu();
        findPopupMenu.removeAll();
        for (DataObject object : objects) {
          String label = object.getValue(displayAttributeName);
          findPopupMenu.add(label);
        }
      } finally {
        reader.close();
      }
    }
  }

}
