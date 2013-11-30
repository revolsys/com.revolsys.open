package com.revolsys.gis.data.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class In extends BinaryCondition {

  public In(final Attribute attribute, final Collection<? extends Object> values) {
    this(attribute.getName(), new CollectionValue(attribute, values));
  }

  public In(final QueryValue left, final CollectionValue values) {
    super(left, "in", values);
  }

  public In(final String name, final Collection<? extends Object> values) {
    this(new Column(name), new CollectionValue(values));
  }

  public In(final String name, final Object... values) {
    this(name, Arrays.asList(values));
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return EqualsRegistry.equal(value1, value2);
  }
}
