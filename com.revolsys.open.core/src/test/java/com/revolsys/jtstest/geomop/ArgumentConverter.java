package com.revolsys.jtstest.geomop;

public class ArgumentConverter {
  public ArgumentConverter() {

  }

  public Object convert(final Class destClass, final Object srcValue) {
    if (srcValue instanceof String) {
      return convertFromString(destClass, (String)srcValue);
    }
    if (destClass.isAssignableFrom(srcValue.getClass())) {
      return srcValue;
    }
    throwInvalidConversion(destClass, srcValue);
    return null;
  }

  public Object[] convert(final Class[] parameterTypes, final Object[] args) {
    final Object[] actualArgs = new Object[args.length];
    for (int i = 0; i < args.length; i++) {
      actualArgs[i] = convert(parameterTypes[i], args[i]);
    }
    return actualArgs;
  }

  private Object convertFromString(final Class destClass, final String src) {
    if (destClass == Boolean.class || destClass == boolean.class) {
      if (src.equals("true")) {
        return new Boolean(true);
      } else if (src.equals("false")) {
        return new Boolean(false);
      }
      throwInvalidConversion(destClass, src);
    } else if (destClass == Integer.class || destClass == int.class) {
      // try as an int
      try {
        return new Integer(src);
      } catch (final NumberFormatException e) {
        // eat this exception - it will be reported below
      }
    } else if (destClass == Double.class || destClass == double.class) {
      // try as an int
      try {
        return new Double(src);
      } catch (final NumberFormatException e) {
        // eat this exception - it will be reported below
      }
    } else if (destClass == String.class) {
      return src;
    }
    throwInvalidConversion(destClass, src);
    return null;
  }

  private void throwInvalidConversion(final Class destClass, final Object srcValue) {
    throw new IllegalArgumentException("Cannot convert " + srcValue + " to " + destClass.getName());
  }
}
