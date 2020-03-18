package com.revolsys.swing.table.filter;

public class ContainsFilter extends GeneralFilter {

  private final String text;

  public ContainsFilter(final String text, final int... columns) {
    super(columns);
    if (text == null) {
      throw new IllegalArgumentException("text must be non-null");
    }
    this.text = text.toUpperCase().replaceAll("%", "");
  }

  @Override
  protected boolean include(final Entry<? extends Object, ? extends Object> value,
    final int index) {
    return value.getStringValue(index).toUpperCase().contains(this.text);
  }
}
