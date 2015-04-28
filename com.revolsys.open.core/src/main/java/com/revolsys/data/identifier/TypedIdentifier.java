package com.revolsys.data.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TypedIdentifier extends AbstractIdentifier {

  private final String type;

  private final Identifier id;

  public TypedIdentifier(final String type, final Identifier id) {
    this.type = type;
    this.id = id;
  }

  @SuppressWarnings("unchecked")
  public <V> V getId() {
    return (V)this.id;
  }

  public Identifier getIdentifier() {
    return this.id;
  }

  public String getType() {
    return this.type;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    if (index == 0) {
      final V type2 = (V)this.type;
      return type2;
    } else {
      return this.id.getValue(index - 1);
    }
  }

  @Override
  public List<Object> getValues() {
    final List<Object> values = new ArrayList<>();
    values.add(this.type);
    values.addAll(this.id.getValues());
    return Arrays.asList(this.type, this.id);
  }

  @Override
  public String toString() {
    return this.type + ":" + this.id;
  }
}
