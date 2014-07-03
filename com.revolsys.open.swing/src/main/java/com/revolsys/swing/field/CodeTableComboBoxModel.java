package com.revolsys.swing.field;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.revolsys.data.codes.CodeTable;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class CodeTableComboBoxModel extends AbstractListModel<Object> implements
ComboBoxModel<Object>, PropertyChangeListener, Closeable {
  public static ComboBox create(final String fieldName,
    final CodeTable codeTable, final boolean allowNull) {
    final CodeTableComboBoxModel model = new CodeTableComboBoxModel(codeTable,
      allowNull);
    final CodeTableObjectToStringConverter stringConverter = new CodeTableObjectToStringConverter(
      codeTable);

    final CodeTableListCellRenderer renderer = new CodeTableListCellRenderer(
      codeTable);
    final ComboBox comboBox = new ComboBox(fieldName, model, stringConverter,
      renderer);
    return comboBox;
  }

  private static final long serialVersionUID = 1L;

  public static final Object NULL = new Object();

  private Object selectedItem;

  private CodeTable codeTable;

  private final boolean allowNull;

  public CodeTableComboBoxModel(final CodeTable codeTable) {
    this(codeTable, true);
  }

  public CodeTableComboBoxModel(final CodeTable codeTable,
    final boolean allowNull) {
    this.codeTable = codeTable;
    this.allowNull = allowNull;
    Property.addListener(codeTable, "valuesChanged", this);
  }

  @Override
  public void close() {
    Property.removeListener(this.codeTable, "valuesChanged", this);
    this.codeTable = null;
    this.selectedItem = null;
  }

  @Override
  public Object getElementAt(int index) {
    if (this.allowNull) {
      if (index == 0) {
        return NULL;
      }
      index--;
    }
    if (index < getSize()) {
      final Map<Identifier, List<Object>> codes = this.codeTable.getCodes();
      final Set<Identifier> keys = codes.keySet();
      return CollectionUtil.get(keys, index);
    } else {
      return null;
    }
  }

  @Override
  public Object getSelectedItem() {
    if (this.selectedItem == NULL) {
      return null;
    } else {
      return this.selectedItem;
    }
  }

  @Override
  public int getSize() {
    if (this.codeTable == null) {
      return 0;
    } else {
      int size = this.codeTable.getCodes().size();
      if (this.allowNull) {
        size++;
      }
      return size;
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getPropertyName().equals("valuesChanged")) {
      final int size = getSize();
      fireContentsChanged(this, 0, size);
    }
  }

  @Override
  public void setSelectedItem(final Object item) {
    if (this.selectedItem != null && !this.selectedItem.equals(item)
        || this.selectedItem == null && item != null) {
      this.selectedItem = item;
      fireContentsChanged(this, -1, -1);
    }
  }

}
