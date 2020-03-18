package com.revolsys.swing.action.enablecheck;

@FunctionalInterface
public interface EnableCheck {
  EnableCheck ENABLED = () -> true;

  EnableCheck DISABLED = () -> false;

  static EnableCheck property(final Object object, final String propertyName) {
    return new ObjectPropertyEnableCheck(object, propertyName);
  }

  static EnableCheck property(final Object object, final String propertyName, final Object value) {
    return new ObjectPropertyEnableCheck(object, propertyName, value);
  }

  static EnableCheck property(final Object object, final String propertyName, final Object value,
    final boolean inverse) {
    return new ObjectPropertyEnableCheck(object, propertyName, value, inverse);
  }

  static EnableCheck propertyNotNull(final Object object, final String propertyName) {
    return new ObjectPropertyEnableCheck(object, propertyName, null, true);
  }

  static EnableCheck propertyNull(final Object object, final String propertyName) {
    return new ObjectPropertyEnableCheck(object, propertyName, null);
  }

  default EnableCheck and(final EnableCheck enableCheck) {
    if (enableCheck == null || enableCheck == this) {
      return this;
    } else if (enableCheck instanceof AndEnableCheck) {
      final AndEnableCheck and = (AndEnableCheck)enableCheck;
      and.addEnableCheck(enableCheck);
      return and;
    } else {
      return new AndEnableCheck(this, enableCheck);
    }
  }

  default EnableCheck and(final Object object, final String propertyName) {
    final ObjectPropertyEnableCheck enableCheck = new ObjectPropertyEnableCheck(object,
      propertyName);
    return and(enableCheck);
  }

  default EnableCheck and(final Object object, final String propertyName, final Object value) {
    final ObjectPropertyEnableCheck enableCheck = new ObjectPropertyEnableCheck(object,
      propertyName, value);
    return and(enableCheck);
  }

  boolean isEnabled();
}
