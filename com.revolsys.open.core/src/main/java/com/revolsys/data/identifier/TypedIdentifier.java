package com.revolsys.data.identifier;

import java.util.Arrays;
import java.util.List;

public class TypedIdentifier extends AbstractIdentifier {

  private final String type;

  private final Object id;

  public TypedIdentifier(final String type, final Object id) {
    this.type = type;
    this.id = id;
  }

  @SuppressWarnings("unchecked")
  public <V> V getId() {
    return (V)this.id;
  }

  public Identifier getIdentifier() {
    return SingleIdentifier.create(this.id);
  }

  public String getType() {
    return this.type;
  }

  @Override
  public List<Object> getValues() {
    return Arrays.asList(this.type, this.id);
  }

  @Override
  public String toString() {
    return this.type + ":" + this.id;
  }
}
