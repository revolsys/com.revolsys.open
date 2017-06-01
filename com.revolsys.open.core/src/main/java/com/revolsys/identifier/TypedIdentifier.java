package com.revolsys.identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.revolsys.util.CompareUtil;
import com.revolsys.util.Property;

public class TypedIdentifier extends AbstractIdentifier implements Comparable<Object> {
  public static Identifier newIdentifier(final Object id) {
    if (id instanceof String) {
      final String string = (String)id;
      final int colonIndex = string.indexOf(':');
      if (colonIndex != -1) {
        final String type = string.substring(0, colonIndex);
        final String valuePart = string.substring(colonIndex + 1);
        Identifier identifier;
        try {
          final long longValue = Long.parseLong(valuePart);
          identifier = Identifier.newIdentifier(longValue);
        } catch (final Throwable e) {
          identifier = Identifier.newIdentifier(valuePart);
        }
        return new TypedIdentifier(type, identifier);
      }
      try {
        final Long longValue = Long.valueOf(string);
        return Identifier.newIdentifier(longValue);
      } catch (final Exception e) {
      }
      try {
        final Long longValue = Long.valueOf(string);
        return Identifier.newIdentifier(longValue);
      } catch (final Exception e) {
      }
    }
    return Identifier.newIdentifier(id);
  }

  public static TypedIdentifier newIdentifier(final String type, Object id) {
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
      final Identifier identifier = Identifier.newIdentifier(id);
      return new TypedIdentifier(type, identifier);
    } else {
      return null;
    }
  }

  private final Identifier identifier;

  private final String type;

  public TypedIdentifier(final String type, final Identifier identifier) {
    if (Property.isEmpty(type)) {
      throw new IllegalArgumentException("type must not be null");
    }
    if (Property.isEmpty(identifier)) {
      throw new IllegalArgumentException("identifier must not be null");
    }
    this.type = type;
    this.identifier = identifier;
  }

  @Override
  public int compareTo(final Identifier identifier) {
    if (identifier == this) {
      return 0;
    } else if (identifier instanceof TypedIdentifier) {
      final TypedIdentifier typedIdentifier2 = (TypedIdentifier)identifier;
      final String type1 = getType();
      final String type2 = typedIdentifier2.getType();
      int compare = type1.compareTo(type2);
      if (compare == 0) {
        final Identifier identifier1 = getIdentifier();
        final Identifier identifier2 = typedIdentifier2.getIdentifier();
        compare = CompareUtil.compare(identifier1, identifier2);
      }
      return compare;
    } else {
      return 1;
    }
  }

  @Override
  public int compareTo(final Object other) {
    if (other instanceof Identifier) {
      final Identifier identifier = (Identifier)other;
      return compareTo(identifier);
    } else {
      return -1;
    }
  }

  public boolean equalsType(final String type) {
    return this.type.equals(type);
  }

  @SuppressWarnings("unchecked")
  public <V> V getId() {
    return (V)this.identifier;
  }

  public Identifier getIdentifier() {
    return this.identifier;
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
      return this.identifier.getValue(index - 1);
    }
  }

  @Override
  public List<Object> getValues() {
    final List<Object> values = new ArrayList<>();
    values.add(this.type);
    values.addAll(this.identifier.getValues());
    return Arrays.asList(this.type, this.identifier);
  }

  @Override
  public String toString() {
    return this.type + ":" + this.identifier;
  }
}
