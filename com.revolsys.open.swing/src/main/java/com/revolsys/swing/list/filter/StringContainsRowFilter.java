package com.revolsys.swing.list.filter;

import javax.swing.ListModel;
import javax.swing.RowFilter;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.util.Property;

public class StringContainsRowFilter extends RowFilter<ListModel, Integer> {

  private String filterText;

  private final boolean match;

  public StringContainsRowFilter() {
    this("");
  }

  public StringContainsRowFilter(final String filterText) {
    this(filterText, true);
  }

  public StringContainsRowFilter(final String filterText, final boolean match) {
    this.filterText = filterText;
    this.match = match;
  }

  @Override
  public boolean include(final Entry<? extends ListModel, ? extends Integer> entry) {
    final Integer identifier = entry.getIdentifier();
    final Object value = entry.getValue(identifier);
    final String string = DataTypes.toString(value);
    if (Property.hasValue(this.filterText)) {
      if (Property.hasValue(string)) {
        return string.toUpperCase().contains(this.filterText) == this.match;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }

  public void setFilterText(final String filterText) {
    this.filterText = filterText.toUpperCase();
  }
}
