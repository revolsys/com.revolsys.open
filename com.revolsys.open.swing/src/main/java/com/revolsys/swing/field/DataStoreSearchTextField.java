package com.revolsys.swing.field;

import javax.swing.JPopupMenu;

import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.AutoCompleteDocument;

import com.revolsys.gis.data.io.DataObjectStore;
import com.revolsys.gis.data.model.DataObject;

public class DataStoreSearchTextField extends JXSearchField {
  private static final long serialVersionUID = 1L;

  private DataStoreSearchAutoCompleteAdaptor adaptor;

  public DataStoreSearchTextField(final DataObjectStore dataStore,
    final String tableName, final String whereClause,
    final String displayAttributeName) {
    setFindPopupMenu(new JPopupMenu());

    setEditable(true);

    DataObjectToStringConverter stringConverter = new DataObjectToStringConverter(
      displayAttributeName);
    adaptor = new DataStoreSearchAutoCompleteAdaptor(this, dataStore,
      tableName, whereClause, displayAttributeName);
    AutoCompleteDocument document = new AutoCompleteDocument(adaptor, false,
      stringConverter, getDocument());
    AutoCompleteDecorator.decorate(this, document, adaptor);
  }

  public DataObject getSelectedItem() {
    return adaptor.getSelectedItem();
  }

}
