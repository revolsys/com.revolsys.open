package com.revolsys.collection.range;

/**
 *
 * Ranges are immutable
 */
public class CharRange extends AbstractRange<Character> {
  private char from;

  private char to;

  protected CharRange(final char value) {
    this(value, value);
  }

  protected CharRange(char from, char to) {
    from = Character.toUpperCase(from);
    if (!isValid(from)) {
      throw new IllegalArgumentException(from + " is not [A-Z]");
    }
    to = Character.toUpperCase(to);
    if (!isValid(to)) {
      throw new IllegalArgumentException(to + " is not [A-Z]");
    }
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
  }

  @Override
  protected AbstractRange<?> createNew(final Object from, final Object to) {
    return Ranges.create((Character)from, (Character)to);
  }

  @Override
  public AbstractRange<?> expand(final Object value) {
    if (value instanceof Character) {
      final Character character = (Character)value;
      return expand(character);
    } else if (value instanceof String) {
      final String string = (String)value;
      if (string.length() == 1) {
        char character = string.charAt(0);
        return super.expand(character);
      }
    }
    return null;
  }

  @Override
  public Character getFrom() {
    return this.from;
  }

  @Override
  public Character getTo() {
    return this.to;
  }

  public boolean isValid(final char character) {
    return character >= 'A' && character <= 'Z';
  }

  @Override
  public Character next(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Character) {
      final char character = Character.toUpperCase((Character)value);
      if ('Z' == character || !isValid(character)) {
        return null;
      } else {
        return (char)(character + 1);
      }
    } else {
      return null;
    }
  }

  @Override
  public Character previous(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Character) {
      final char character = Character.toUpperCase((Character)value);
      if ('A' == character || !isValid(character)) {
        return null;
      } else {
        return (char)(character - 1);
      }
    } else {
      return null;
    }
  }

  @Override
  public long size() {
    return this.to - this.from + 1;
  }
}
