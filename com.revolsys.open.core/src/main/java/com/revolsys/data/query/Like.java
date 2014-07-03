package com.revolsys.data.query;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.data.equals.EqualsRegistry;

public class Like extends BinaryCondition {

  public static String toPattern(String value2) {
    value2 = value2.replaceAll("\\\\", "\\\\");
    value2 = value2.replaceAll("\\[", "\\[");
    value2 = value2.replaceAll("\\]", "\\]");
    value2 = value2.replaceAll("\\{", "\\{");
    value2 = value2.replaceAll("\\}", "\\}");
    value2 = value2.replaceAll("\\(", "\\)");
    value2 = value2.replaceAll("\\)", "\\)");
    value2 = value2.replaceAll("\\^", "\\^");
    value2 = value2.replaceAll("\\$", "\\$");
    value2 = value2.replaceAll("\\+", "\\+");
    value2 = value2.replaceAll("\\-", "\\-");
    value2 = value2.replaceAll("\\*", "\\*");
    value2 = value2.replaceAll("\\?", "\\?");
    value2 = value2.replaceAll("\\|", "\\|");
    value2 = value2.replaceAll("\\,", "\\,");
    value2 = value2.replaceAll("\\:", "\\:");
    value2 = value2.replaceAll("\\!", "\\!");
    value2 = value2.replaceAll("\\<", "\\<");
    value2 = value2.replaceAll("\\>", "\\>");
    value2 = value2.replaceAll("\\=", "\\=");
    value2 = value2.replaceAll("%", ".*");
    return value2;
  }

  public Like(final QueryValue left, final QueryValue right) {
    super(left, "LIKE", right);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final String value1 = left.getStringValue(record);

    final QueryValue right = getRight();
    String value2 = right.getStringValue(record);

    if (StringUtils.hasText(value1)) {
      if (StringUtils.hasText(value2)) {
        if (value2.contains("%")) {
          value2 = toPattern(value2);
          if (value1.matches(value2)) {
            return true;
          } else {
            return false;
          }
        } else {
          return EqualsRegistry.equal(value1, value2);
        }
      } else {
        return false;
      }
    } else {
      return !StringUtils.hasText(value2);
    }
  }

  @Override
  public Like clone() {
    return (Like)super.clone();
  }

}
