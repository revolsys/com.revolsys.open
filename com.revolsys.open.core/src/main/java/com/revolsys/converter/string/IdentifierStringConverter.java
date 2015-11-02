package com.revolsys.converter.string;

import com.revolsys.identifier.Identifier;

public class IdentifierStringConverter implements StringConverter<Identifier> {
  @Override
  public Class<Identifier> getConvertedClass() {
    return Identifier.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public Identifier toObject(final Object value) {
    return Identifier.newIdentifier(value);
  }

  @Override
  public Identifier toObject(final String string) {
    return Identifier.newIdentifier(string);
  }

  @Override
  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

}
