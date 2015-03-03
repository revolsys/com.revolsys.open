package com.revolsys.collection.set;

/**
 *
 * Ranges are immutable
 */
public class CharRange extends AbstractRange<Character> {
  private char from;

  private char to;

  public CharRange(final char value) {
    this(value, value);
  }

  public CharRange(char from, char to) {
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
  public int compareFromValue(final Character value) {
    if (this.from < value) {
      return -1;
    } else if (this.from > value) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public int compareToValue(final Character value) {
    if (this.to < value) {
      return -1;
    } else if (this.to > value) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public AbstractRange<Character> createNew(final Character from, final Character to) {
    return new CharRange(from, to);
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
  public Character next(Character value) {
    if (value == null) {
      return null;
    } else {
      value = Character.toUpperCase(value);
      if ('Z' == value || !isValid(value)) {
        return null;
      } else {
        return (char)(value + 1);
      }
    }
  }

  @Override
  public Character previous(Character value) {
    if (value == null) {
      return null;
    } else {
      value = Character.toUpperCase(value);
      if ('A' == value || !isValid(value)) {
        return null;
      } else {
        return (char)(value - 1);
      }
    }
  }

  @Override
  public int size() {
    return this.to - this.from + 1;
  }
}
