package com.revolsys.swing.field;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import com.revolsys.gis.data.model.codes.CodeTable;
import com.revolsys.util.CollectionUtil;

@SuppressWarnings("serial")
public class CodeTableComboBoxModel extends AbstractListModel implements
  ComboBoxModel {

  public static final Object NULL = new Object();

  public static JComboBox create(final CodeTable codeTable) {
    return create(codeTable, true);
  }

  public static JComboBox create(final CodeTable codeTable,
    final boolean allowNull) {
    final CodeTableComboBoxModel model = new CodeTableComboBoxModel(codeTable,
      allowNull);
    final JComboBox comboBox = new JComboBox(model);
    final CodeTableListCellRenderer renderer = new CodeTableListCellRenderer(
      codeTable);
    comboBox.setRenderer(renderer);
    comboBox.setEditable(false);
    return comboBox;
  }

  private Object selectedItem;

  private final CodeTable codeTable;

  private final boolean allowNull;

  public CodeTableComboBoxModel(final CodeTable codeTable) {
    this(codeTable, true);
  }

  public CodeTableComboBoxModel(final CodeTable codeTable,
    final boolean allowNull) {
    this.codeTable = codeTable;
    this.allowNull = allowNull;
  }

  @Override
  public Object getElementAt(int index) {
    if (allowNull) {
      if (index == 0) {
        return NULL;
      }
      index--;
    }
    if (index < getSize()) {
      final Map<Object, List<Object>> codes = codeTable.getCodes();
      final Set<Object> keys = codes.keySet();
      return CollectionUtil.get(keys, index);
    } else {
      return null;
    }
  }

  @Override
  public Object getSelectedItem() {
    if (selectedItem == NULL) {
      return null;
    } else {
      return selectedItem;
    }
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
