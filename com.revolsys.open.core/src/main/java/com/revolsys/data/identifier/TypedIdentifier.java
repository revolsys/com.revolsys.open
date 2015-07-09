package com.revolsys.data.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.util.Property;

public class TypedIdentifier extends AbstractIdentifier {

  public static TypedIdentifier create(final String type, Object id) {
    if (id == null) {
      return null;
    } else if (id instanceof TypedIdentifier) {
      final TypedIdentifier typedIdentifier = (TypedIdentifier)id;
      final String existingType = typedIdentifier.getType();
      if (existingType.equals(type)) {
        return typedIdentifier;
      } else {
        throw new IllegalArgumentException(
          "Cannot convert id " + typedIdentifier + " to type=" + type);
      }
    } else if (id instanceof String) {
      final String string = (String)id;
      final int colonIndex = string.indexOf(':');
      if (colonIndex != -1) {
        final String existingType = string.substring(0, colonIndex);
        id = string.substring(colonIndex + 1);
        if (!existingType.equals(type)) {
          throw new IllegalArgumentException("Cannot convert id " + string + " to type=" + type);
        }
      }
    }
    if (Property.hasValue(id)) {
      final Identifier identifier = SingleIdentifier.create(id);
      return new TypedIdentifier(type, identifier);
    } else {
      return null;
    }
  }

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
