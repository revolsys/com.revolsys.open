package com.revolsys.swing.map.layer.dataobject.style;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

public class RuleSet {
  private Selector selector;
 
  private Map<CartoCssProperty, String> declarations = new LinkedHashMap<CartoCssProperty, String>();

  public RuleSet(Selector selector, Map<CartoCssProperty, String> declarations) {
    this.selector = selector;
    this.declarations = declarations;
  }

  public Selector getSelector() {
    return selector;
  }

  public Map<CartoCssProperty, String> getDeclarations() {
    return Collections.unmodifiableMap(declarations);
  }

  @Override
  public String toString() {
    StringBuffer s = new StringBuffer();
    s.append(selector);
    s.append("{\n");
    for (Entry<CartoCssProperty, String> entry : declarations.entrySet()) {
      s.append(entry.getKey());
      s.append(": ");
      s.append(entry.getValue());
      s.append(";\n");
    }
    s.append("}\n");
    return s.toString();
  }

  public float getFloat(CartoCssProperty property, float defaultValue) {
    String value = declarations.get(property);
    if (StringUtils.hasText(value)) {
      try {
        return Float.parseFloat(value);
      } catch (Throwable t) {
        throw new RuntimeException("Not a valid float " + property + "="
          + value);
      }
    } else {
      return defaultValue;
    }
  }
  public double getDouble(CartoCssProperty property) {
    return getDouble(property, 1);
  }
  public double getDouble(CartoCssProperty property, double defaultValue) {
    String value = declarations.get(property);
    if (StringUtils.hasText(value)) {
      try {
        return Double.parseDouble(value);
      } catch (Throwable t) {
        throw new RuntimeException("Not a valid float " + property + "="
          + value);
      }
    } else {
      return defaultValue;
    }
  }
  public Color getColor(CartoCssProperty property) {
    return getColor(property,null);
  }
  public Color getColor(CartoCssProperty property, Color defaultColor) {
    String value = declarations.get(property);
    if (StringUtils.hasText(value)) {
      if (value.startsWith("#")) {
        return null;
      } else if (value.startsWith("rgb(")) {
        try {
          String[] values = value.replaceAll("[^0-9,]", "").split(",");
          int red = Integer.valueOf(values[0]);
          int green = Integer.valueOf(values[1]);
          int blue = Integer.valueOf(values[2]);
          Color color = new Color(red, green, blue,255);
            return color;
        } catch (Throwable e) {
          throw new IllegalArgumentException("Not a valid CSS color " + value,
            e);
        }
      } else if (value.startsWith("rgba(")) {
        try {
          String[] values = value.replaceAll("[^0-9,.]", "").split(",");
          int red = Integer.valueOf(values[0]);
          int green = Integer.valueOf(values[1]);
          int blue = Integer.valueOf(values[2]);
          int alpha = (int)(Double.valueOf(values[3]) * 255);
          Color color = new Color(red, green, blue, alpha);
          return color;
        } catch (Throwable e) {
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
}
