package com.revolsys.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListIdentifier extends AbstractIdentifier {

  private final List<Object> values;

  public ListIdentifier(final Collection<? extends Object> values) {
    if (values == null || values.size() == 0) {
      this.values = Collections.emptyList();
    } else {
      this.values = Collections.unmodifiableList(new ArrayList<>(values));
    }
  }

  public ListIdentifier(final Object... values) {
    if (values == null || values.length == 0) {
      this.values = Collections.emptyList();
    } else {
      this.values = Collections.unmodifiableList(Arrays.asList(values));
    }
  }

  @Override
  public List<Object> getValues() {
    return this.values;
  }
}
