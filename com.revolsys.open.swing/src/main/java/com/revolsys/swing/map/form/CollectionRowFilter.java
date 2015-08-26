package com.revolsys.swing.map.form;

import java.util.Collection;

import javax.swing.ListModel;
import javax.swing.RowFilter;

public class CollectionRowFilter extends RowFilter<ListModel, Integer> {

  private final boolean match;

  private final Collection<? extends Object> values;

  public CollectionRowFilter(final Collection<? extends Object> values) {
    this(values, true);
  }

  public CollectionRowFilter(final Collection<? extends Object> values, final boolean match) {
    this.values = values;
    this.match = match;
  }

  @Override
  public boolean include(final Entry<? extends ListModel, ? extends Integer> entry) {
    final Integer identifier = entry.getIdentifier();
    final Object value = entry.getValue(identifier);
    return this.values.contains(value) == this.match;
  }
}
