package com.revolsys.swing.map.symbolizer;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;

public class FilterSymbolizer extends AbstractSymbolizer {

  private final Symbolizer symbolizer;

  private final Filter<DataObject> filter;

  public FilterSymbolizer(final Filter<DataObject> filter,
    final Symbolizer symbolizer) {
    this.filter = filter;
    this.symbolizer = symbolizer;
  }

  public Filter<DataObject> getFilter() {
    return filter;
  }

  public Symbolizer getSymbolizer() {
    return symbolizer;
  }

}
