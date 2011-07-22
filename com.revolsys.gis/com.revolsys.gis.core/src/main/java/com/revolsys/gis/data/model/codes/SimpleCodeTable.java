package com.revolsys.gis.data.model.codes;

import java.util.List;

public class SimpleCodeTable extends AbstractCodeTable {
  private int index = 0;

  @Override
  public void addValue(final Number id, final Object... values) {
    super.addValue(id, values);
  }

  public void addValue(final Object... values) {
    index++;
    addValue(index, values);
  }

  @Override
  public SimpleCodeTable clone() {
    return (SimpleCodeTable)super.clone();
  }

  public String getIdAttributeName() {
    return null;
  }

  @Override
  protected Number loadId(final List<Object> values, final boolean createId) {
    index++;
    return index;
  }

}
