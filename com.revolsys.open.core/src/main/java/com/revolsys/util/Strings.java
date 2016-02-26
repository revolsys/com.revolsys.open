package com.revolsys.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;

public interface Strings {
  public static String cleanWhitespace(final String text) {
    if (text == null) {
      return text;
    } else {
      return text.replaceAll("\\s+", " ").trim();
    }
  }

  public static boolean contains(final CharSequence text, final char character) {
    if (text != null) {
      final int length = text.length();
      for (int i = 0; i < length; i++) {
        final char currentCharacter = text.charAt(i);
        if (currentCharacter == character) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean contains(final String text, final String matchText) {
    if (text == null || matchText == null) {
      return false;
    } else {
      return text.contains(matchText);
    }
  }

  public static boolean endsWith(final String text, final String suffix) {
    if (text != null && suffix != null) {
      return text.endsWith(suffix);
    } else {
      return false;
    }
  }

  public static boolean equalExceptOneCharacter(final String string1, final String string2) {
    final int length1 = string1.length();
    if (length1 != string2.length()) {
      return false;
    } else {
      boolean equal = true;
      for (int i = 0; i < length1; ++i) {
        if (string1.charAt(i) != string2.charAt(i)) {
          if (equal) {
            equal = false;
          } else {
            return false;
          }
        }
      }
      return true;
    }
  }

  public static boolean equalExceptOneExtraCharacter(final String string1, final String string2) {
    final int length1 = string1.length();
    final int length2 = string2.length();
    if (length1 == length2) {
      return string1.equals(string2);
    } else {
      if (length1 == length2 + 1) {
        return equalExceptOneExtraCharacter(string2, string1);
      }
      if (length2 == length1 + 1) {
        int startMatchCount = 0;
        for (int i = 0; i < length1; i++) {
          final char c1 = string1.charAt(i);
          final char c2 = string2.charAt(i);
          if (c1 == c2) {
            startMatchCount++;
          } else {
            break;
          }
        }
        int endMatchCount = 0;
        for (int i = 1; i <= length1 - startMatchCount; i++) {
          final char c1 = string1.charAt(length1 - i);
          final char c2 = string2.charAt(length2 - i);
          if (c1 == c2) {
            endMatchCount++;
          } else {
            break;
          }
        }
        return startMatchCount + endMatchCount == length1;
      } else {
        return false;
      }
    }
  }

  public static boolean equals(final String string1, final String string2) {
    if (string1 == null) {
      return string2 == null;
    } else {
      return string1.equals(string2);
    }
  }

  public static boolean equalsIgnoreCase(final String string1, final String string2) {
    if (Property.hasValue(string1)) {
      return string1.equalsIgnoreCase(string2);
    } else {
      return Property.isEmpty(string2);
    }
  }

  public static String firstPart(final String text, final char character) {
    final int index = text.indexOf(character);
    if (index == -1) {
      return "";
    } else {
      return text.substring(0, index);
    }
  }

  public static int indexOf(final CharSequence text, final char character) {
    if (text != null) {
      final int length = text.length();
      for (int i = 0; i < length; i++) {
        final char currentCharacter = text.charAt(i);
        if (currentCharacter == character) {
          return i;
        }
      }
    }
    return -1;
  }

  static boolean isEqualTrim(final String oldValue, final String newValue) {
    final boolean oldHasValue = Property.hasValue(oldValue);
    final boolean newHasValue = Property.hasValue(newValue);
    if (oldHasValue) {
      if (newHasValue) {
        if (DataType.equal(oldValue.trim(), newValue.trim())) {
          return true;
        } else {
          return false;
        }
      } else {
        return false;
      }
    } else {
      if (newHasValue) {
        return false;
      } else {
        return true;
      }
    }
  }

  public static String lastPart(final String text, final char character) {
    final int index = text.lastIndexOf(character);
    if (index == -1) {
      return "";
    } else {
      return text.substring(0, index);
    }
  }

  public static String lowerCase(final String text) {
    if (text == null) {
      return null;
    } else {
      return text.toLowerCase();
    }
  }

  public static boolean matches(final String text, final String regex) {
    if (text == null || regex == null) {
      return false;
    } else {
      return text.matches(regex);
    }
  }

  static String replaceAll(final String text, final String from, final Object to) {
    if (text == null) {
      return null;
    } else {
      String toText;
      if (to == null) {
        toText = "";
      } else {
        toText = to.toString();
      }
      return text.replaceAll(from, toText);
    }
  }

  static String replaceAll(final String text, final String from, final String to) {
    if (text == null) {
      return null;
    } else {
      return text.replaceAll(from, to);
    }
  }

  public static boolean startsWith(final String text, final String prefix) {
    if (text != null && prefix != null) {
      return text.startsWith(prefix);
    } else {
      return false;
    }
  }

  public static String substring(final String text, final char character, final int toIndex) {
    int startIndex = 0;
    for (int i = 0; i < toIndex && startIndex != -1; i++) {
      final int index = text.indexOf(character, startIndex);
      if (index == -1) {
        return "";
      }
      startIndex = index + 1;
    }
    if (startIndex == -1) {
      return text;
    } else {
      return text.substring(startIndex);
    }
  }

  public static String substring(final String text, final char character, final int fromIndex,
    final int toIndex) {
    if (fromIndex < 0) {
      throw new StringIndexOutOfBoundsException(fromIndex);
    } else if (toIndex < 0) {
      throw new StringIndexOutOfBoundsException(toIndex);
    }
    int startIndex = 0;
    for (int i = 0; i < fromIndex && startIndex != -1; i++) {
      final int index = text.indexOf(character, startIndex);
      if (index == -1) {
        return "";
      }
      startIndex = index + 1;
    }
    int endIndex = startIndex;
    for (int i = fromIndex; i < toIndex && endIndex != -1; i++) {
      if (i > fromIndex) {
        endIndex++;
      }
      final int index = text.indexOf(character, endIndex);
      if (index == -1) {
        return text.substring(startIndex);
      } else {
        endIndex = index;
      }
    }
    if (endIndex == -1) {
      return "";
    } else {
      return text.substring(startIndex, endIndex);
    }
  }

  /**
   * Construct a new string using the same style as java.util.List.toString.
   * @param iterator
   * @return
   */
  static String toListString(final Iterable<? extends Object> iterable) {
    if (iterable == null) {
      return "[]";
    } else {
      final Iterator<? extends Object> iterator = iterable.iterator();
      return toListString(iterator);
    }
  }

  static String toListString(final Iterator<? extends Object> iterator) {
    if (iterator == null) {
      return "[]";
    } else {
      final StringBuilder string = new StringBuilder("[");
      if (iterator.hasNext()) {
        string.append(iterator.next());
        while (iterator.hasNext()) {
          string.append(", ");
          string.append(iterator.next());
        }
      }
      string.append("]");
      return string.toString();
    }
  }

  static String toString(final boolean skipNulls, final String separator,
    final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      StringBuilders.append(string, values, skipNulls, separator);
      return string.toString();
    }
  }

  static String toString(final boolean skipNulls, final String separator, final Object... values) {
    return toString(skipNulls, separator, Arrays.asList(values));
  }

  /**
   * Convert the collection to a string, using the "," separator between each
   * value. Nulls will be the empty string "".
   *
   * @param values The values.
   * @param separator The separator.
   * @return The string.
   */
  static String toString(final Collection<? extends Object> values) {
    return toString(",", values);
  }

  public static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

  /**
   * Convert the collection to a string, using the separator between each value.
   * Nulls will be the empty string "".
   *
   * @param separator The separator.
   * @param values The values.
   * @return The string.
   */
  static String toString(final String separator, final Collection<? extends Object> values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      StringBuilders.append(string, values, separator);
      return string.toString();
    }
  }

  static String toString(final String separator, final int... values) {
    if (values == null) {
      return null;
    } else {
      final StringBuilder string = new StringBuilder();
      boolean first = true;
      for (final int value : values) {
        if (first) {
          first = false;
        } else {
          string.append(separator);
        }
        string.append(value);
      }
      return string.toString();
    }
  }

  static String toString(final String separator, final Object... values) {
    return toString(separator, Arrays.asList(values));
  }

  static List<String> toStringList(final Collection<?> values) {
    final List<String> strings = new ArrayList<>();
    if (values != null) {
      for (final Object value : values) {
        strings.add(DataTypes.toString(value));
      }
    }
    return strings;
  }

  public static String trim(final String text) {
    if (text == null) {
      return null;
    } else {
      return text.trim();
    }
  }

  public static int trimLength(final String text) {
    if (text == null) {
      return 0;
    } else {
      return text.trim().length();
    }
  }

  public static String upperCase(final String text) {
    if (text == null) {
      return null;
    } else {
      return text.toUpperCase();
    }
  }
}
