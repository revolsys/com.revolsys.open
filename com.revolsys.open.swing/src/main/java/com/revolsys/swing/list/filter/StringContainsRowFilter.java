package com.revolsys.swing.list.filter;

import javax.swing.ListModel;
import javax.swing.RowFilter;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverterRegistry;

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
  public boolean include(
    final Entry<? extends ListModel, ? extends Integer> entry) {
    final Integer identifier = entry.getIdentifier();
    final Object value = entry.getValue(identifier);
    final String string = StringConverterRegistry.toString(value);
    if (StringUtils.hasText(filterText)) {
      if (StringUtils.hasText(string)) {
        return string.contains(filterText) == match;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }

  public void setFilterText(final String filterText) {
    this.filterText = filterText;
  }
}
