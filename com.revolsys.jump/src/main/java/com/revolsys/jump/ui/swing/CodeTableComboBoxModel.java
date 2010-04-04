package com.revolsys.jump.ui.swing;

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

  private CodeTable codeTable;

  public CodeTableComboBoxModel(final CodeTable codeTable) {
    this.codeTable = codeTable;
  }

  public Object getSelectedItem() {
    return selectedItem;
  }

  public void setSelectedItem(final Object item) {
    if ((selectedItem != null && !selectedItem.equals(item))
      || selectedItem == null && item != null) {
      selectedItem = item;
      fireContentsChanged(this, -1, -1);
    }
  }

  public Object getElementAt(final int index) {
    Map<Number, List<Object>> codes = codeTable.getCodes();
    int i = 0;
    for (Entry<Number, List<Object>> entry : codes.entrySet()) {
      if (index == i) {
        return entry;
      } else {
        i++;
      }
    }
    return null;
  }

  public int getSize() {
    return codeTable.getCodes().size();
  }

}
