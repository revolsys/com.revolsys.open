package com.revolsys.record.code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.identifier.Identifier;
import com.revolsys.util.Describable;

public interface Code extends Describable, Identifier {

  static <C extends Code> List<String> descriptions(final Class<C> enumClass) {
    final List<String> descriptions = new ArrayList<>();
    for (final C code : enumClass.getEnumConstants()) {
      final String description = code.getDescription();
      descriptions.add(description);
    }
    return descriptions;
  }

  static List<String> descriptions(final List<? extends Code> codes) {
    final List<String> descriptions = new ArrayList<>();
    for (final Code code : codes) {
      final String description = code.getDescription();
      descriptions.add(description);
    }
    return descriptions;
  }

  @SuppressWarnings("unchecked")
  static <C> C getCode(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Code) {
      final Code code = (Code)value;
      return code.getCode();
    } else {
      return (C)value;
    }
  }

  static <C extends Code> Map<String, C> getEnumCodeMap(final Class<C> enumClass) {
    final Map<String, C> code = new HashMap<>();
    for (final C enumValue : enumClass.getEnumConstants()) {
      final String codeId = enumValue.getCode();
      code.put(codeId, enumValue);
    }
    return Collections.unmodifiableMap(code);
  }

  default boolean equalsCode(final Object code) {
    final Object codeThis = getCode();
    return codeThis.equals(code);
  }

  <C> C getCode();

  @Override
  default List<Object> getValues() {
    final Object code = getCode();
    return Collections.singletonList(code);
  }

  @Override
  default String toIdString() {
    return getCode().toString();
  }
}
