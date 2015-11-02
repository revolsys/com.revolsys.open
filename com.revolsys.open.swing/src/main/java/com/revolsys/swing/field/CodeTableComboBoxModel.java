package com.revolsys.swing.field;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.code.CodeTable;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class CodeTableComboBoxModel extends AbstractListModel<Identifier>
  implements ComboBoxModel<Identifier>, PropertyChangeListener, Closeable {
  private static final long serialVersionUID = 1L;

  public static ComboBox<Identifier> newComboBox(final String fieldName, final CodeTable codeTable,
    final boolean allowNull) {
    final CodeTableComboBoxModel model = new CodeTableComboBoxModel(codeTable, allowNull);

    final ComboBox<Identifier> comboBox = ComboBox.newComboBox(fieldName, model, (value) -> {
      if (value == null || value == Identifier.NULL) {
        return null;
      } else {
        final List<Object> values = codeTable.getValues(value);
        if (values == null || values.isEmpty()) {
          return null;
        } else {
          return Strings.toString(":", values);
        }
      }
    });
    return comboBox;
  }

  private final boolean allowNull;

  private CodeTable codeTable;

  private Object selectedItem;

  public CodeTableComboBoxModel(final CodeTable codeTable) {
    this(codeTable, true);
  }

  public CodeTableComboBoxModel(final CodeTable codeTable, final boolean allowNull) {
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
  public void fireContentsChanged(final Object source, final int index0, final int index1) {
    Invoke.later(() -> super.fireContentsChanged(source, index0, index1));
  }

  @Override
  public Identifier getElementAt(int index) {
    if (this.allowNull) {
      if (index == 0) {
        return Identifier.NULL;
      }
      index--;
    }
    if (index >= 0 && index < getSize()) {
      final List<Identifier> identifiers = this.codeTable.getIdentifiers();
      return identifiers.get(index);
    } else {
      return null;
    }
  }

  @Override
  public Object getSelectedItem() {
    if (this.selectedItem == Identifier.NULL) {
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
      int size = this.codeTable.getIdentifiers().size();
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
