package com.revolsys.swing.field;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.revolsys.gis.data.model.codes.CodeTable;

@SuppressWarnings("serial")
public class CodeTableComboBoxModel extends AbstractListModel implements
  ComboBoxModel {
  private Object selectedItem;

  private final CodeTable codeTable;

  private boolean allowNull;
  
  public CodeTableComboBoxModel(final CodeTable codeTable) {
    this(codeTable,true);
  }

  public CodeTableComboBoxModel(CodeTable codeTable, boolean allowNull) {
    this.codeTable = codeTable;
    this.allowNull = allowNull;
  }

  @Override
  public Object getElementAt(final int index) {
    int i = 0;
    if (allowNull) {
      if (index == 0) {
        return "";
      }
      i = 1;
    }
    final Map<Object, List<Object>> codes = codeTable.getCodes();
    for (final Entry<Object, List<Object>> entry : codes.entrySet()) {
      if (index == i) {
        return entry;
      } else {
        i++;
      }
    }
    return null;
  }

  @Override
  public Object getSelectedItem() {
    return selectedItem;
  }

  @Override
  public int getSize() {
    int size = codeTable.getCodes().size();
    if (allowNull) {
      size++;
    }
    return size;
  }

  @Override
  public void setSelectedItem(final Object item) {
    if ((selectedItem != null && !selectedItem.equals(item))
      || selectedItem == null && item != null) {
      selectedItem = item;
      fireContentsChanged(this, -1, -1);
    }
  }

}
