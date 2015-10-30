package com.revolsys.identifier;

import com.revolsys.util.HashEquals;

/**
 * <p>A {@link HashEquals} implementation for of {@link Identifiable} classes to
 * use the value returned from {@link Identifiable#getIdentifier()} to calculate
 * the hash code and test for equality.
 */
public class IdentifiableHashEquals implements HashEquals {

  /**
   * Get the {@link Identifiable#getIdentifier()} values of a {@link Identifiable} object.
   *
   * @param value The value.
   * @return The identifier if the value has an identifier.
   */
  public static Identifier getIdentifier(final Object value) {
    if (value instanceof Identifiable) {
      final Identifiable idenifiable = (Identifiable)value;
      final Identifier identifier = idenifiable.getIdentifier();
      return identifier;
    } else {
      return Identifier.newIdentifier(value);
    }
  }

  /**
   * <p>Test the {@link Identifiable#getIdentifier()} values of two {@link Identifiable}
   * objects to see if the identifiers are equal. If the records are ==, then true is returned.
   * If either of the objects are not {@link Identifiable}
   * or have a null identifier then false is returned. Otherwise true is returned if the identifiers
   * are equal.
   *
   * @param value1 The first value.
   * @param value2 The second value.
   * @return True if the identifiers are equal.
   */
  @Override
  public boolean equals(final Object value1, final Object value2) {
    if (value1 == value2) {
      return true;
    } else {
      final Identifier identifier1 = getIdentifier(value1);
      final Identifier identifier2 = getIdentifier(value2);
      if (identifier1 == null || identifier2 == null) {
        return false;
      } else {
        return identifier1.equals(identifier2);
      }
    }
  }

  /**
   * <p>Calculate the hash code for an {@link Identifiable} object. 0 is returned if the
   * {@link Identifiable#getIdentifier()} is null or the object is not {@link Identifiable}.
   *
   * @param value The value to calculate the hash code for.
   * @return The hashCode
   */
  @Override
  public int hashCode(final Object value) {
    final Identifier identifier = getIdentifier(value);
    if (identifier == null) {
      return 0;
    } else {
      return identifier.hashCode();
    }
  }
}
