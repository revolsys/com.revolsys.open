package com.revolsys.swing.table.filter;

public class EqualFilter extends GeneralFilter {

  private final String text;

  public EqualFilter(final String text, final int... columns) {
    super(columns);
    if (text == null) {
      throw new IllegalArgumentException("text must be non-null");
    }
    this.text = text;
  }

  @Override
  protected boolean include(final Entry<? extends Object, ? extends Object> value,
    final int index) {
    return value.getStringValue(index).equalsIgnoreCase(this.text);
  }
}
