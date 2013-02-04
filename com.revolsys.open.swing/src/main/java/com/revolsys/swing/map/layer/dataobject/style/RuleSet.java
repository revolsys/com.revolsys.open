package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

public class RuleSet {
  private final Selector selector;

  private Map<CartoCssProperty, String> declarations = new LinkedHashMap<CartoCssProperty, String>();

  public RuleSet(final Selector selector,
    final Map<CartoCssProperty, String> declarations) {
    this.selector = selector;
    this.declarations = declarations;
  }

  public Color getColor(final CartoCssProperty property) {
    return getColor(property, null);
  }

  public Color getColor(final CartoCssProperty property,
    final Color defaultColor) {
    final String value = declarations.get(property);
    if (StringUtils.hasText(value)) {
      if (value.startsWith("#")) {
        return null;
      } else if (value.startsWith("rgb(")) {
        try {
          final String[] values = value.replaceAll("[^0-9,]", "").split(",");
          final int red = Integer.valueOf(values[0]);
          final int green = Integer.valueOf(values[1]);
          final int blue = Integer.valueOf(values[2]);
          final Color color = new Color(red, green, blue, 255);
          return color;
        } catch (final Throwable e) {
          throw new IllegalArgumentException("Not a valid CSS color " + value,
            e);
        }
      } else if (value.startsWith("rgba(")) {
        try {
          final String[] values = value.replaceAll("[^0-9,.]", "").split(",");
          final int red = Integer.valueOf(values[0]);
          final int green = Integer.valueOf(values[1]);
          final int blue = Integer.valueOf(values[2]);
          final int alpha = (int)(Double.valueOf(values[3]) * 255);
          final Color color = new Color(red, green, blue, alpha);
          return color;
        } catch (final Throwable e) {
          throw new IllegalArgumentException("Not a valid CSS color " + value,
            e);
        }
      } else {
        throw new IllegalArgumentException("Not a valid CSS color " + value);
      }
    } else {
      return defaultColor;
    }

  }

  public Map<CartoCssProperty, String> getDeclarations() {
    return Collections.unmodifiableMap(declarations);
  }

  public double getDouble(final CartoCssProperty property) {
    return getDouble(property, 1);
  }

  public double getDouble(final CartoCssProperty property,
    final double defaultValue) {
    final String value = declarations.get(property);
    if (StringUtils.hasText(value)) {
      try {
        return Double.parseDouble(value);
      } catch (final Throwable t) {
        throw new RuntimeException("Not a valid float " + property + "="
          + value);
      }
    } else {
      return defaultValue;
    }
  }

  public float getFloat(final CartoCssProperty property,
    final float defaultValue) {
    final String value = declarations.get(property);
    if (StringUtils.hasText(value)) {
      try {
        return Float.parseFloat(value);
      } catch (final Throwable t) {
        throw new RuntimeException("Not a valid float " + property + "="
          + value);
      }
    } else {
      return defaultValue;
    }
  }

  public Selector getSelector() {
    return selector;
  }

  @Override
  public String toString() {
    final StringBuffer s = new StringBuffer();
    s.append(selector);
    s.append("{\n");
    for (final Entry<CartoCssProperty, String> entry : declarations.entrySet()) {
      s.append(entry.getKey());
      s.append(": ");
      s.append(entry.getValue());
      s.append(";\n");
    }
    s.append("}\n");
    return s.toString();
  }
}
