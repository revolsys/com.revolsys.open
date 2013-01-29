package com.revolsys.swing.map.symbolizer;

import java.util.List;

import com.revolsys.filter.Filter;
import com.revolsys.gis.data.model.DataObject;

public class DataObjectFilterSymbolizer {
  private Filter<DataObject> filter;

  private List<Symbolizer> symbolizers;

  public Filter<DataObject> getFilter() {
    return filter;
  }

  public List<Symbolizer> getSymbolizers() {
    return symbolizers;
  }

  public void setFilter(final Filter<DataObject> filter) {
    this.filter = filter;
  }

  public void setSymbolizers(final List<Symbolizer> symbolizers) {
    this.symbolizers = symbolizers;
  }

}
