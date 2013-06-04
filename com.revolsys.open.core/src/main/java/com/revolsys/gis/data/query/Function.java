package com.revolsys.gis.data.query;

import java.util.Arrays;
import java.util.List;

public class Function extends AbstractMultiCondition {

  public static Function toChar(final String name, String format) {
    return new Function("TO_CHAR", new Column(name), new Value(
      format));
  }

  public static Function upper(final Condition condition) {
    return new Function("UPPER", condition);
  }

  public static Function upper(final String name) {
    return upper(new Column(name));
  }

  private final String name;

  public Function(final String name, final Condition... parameters) {
    this(name, Arrays.asList(parameters));
  }

  public Function(final String name, final List<Condition> parameters) {
    super(parameters);
    this.name = name;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append(name);
    buffer.append("(");
    boolean first = true;
    for (final Condition parameter : getConditions()) {
      if (first) {
        first = false;
      } else {
        buffer.append(", ");
      }
      parameter.appendSql(buffer);
    }
    buffer.append(")");
  }

  @Override
  public Function clone() {
    return (Function)super.clone();
  }

}
