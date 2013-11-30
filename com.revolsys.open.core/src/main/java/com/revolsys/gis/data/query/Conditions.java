package com.revolsys.gis.data.query;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.io.DataObjectStore;

public class Conditions {

  public static Condition like(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return like(left, valueCondition);
  }

  public static Condition like(final QueryValue left, final QueryValue right) {
    return new BinaryCondition(left, "LIKE", right);
  }

  public static Condition like(final String left, final Condition right) {
    return like(new Column(left), right);
  }

  public static Condition like(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return like(name, valueCondition);
  }

  public static Condition likeRegEx(final DataObjectStore dataStore,
    final String fieldName, final Object value) {
    Condition left;
    if (dataStore.getClass().getName().contains("Oracle")) {
      left = new SqlCondition("regexp_replace(upper(" + fieldName
        + "), '[^A-Z0-9]','')");
    } else {
      left = new SqlCondition("regexp_replace(upper(" + fieldName
        + "), '[^A-Z0-9]','', 'g')");
    }
    final String right = "%"
      + StringConverterRegistry.toString(value)
        .toUpperCase()
        .replaceAll("[^A-Z0-0]", "") + "%";
    return like(left, right);
  }

  public static Condition likeUpper(final String left, final String right) {
    return like(Function.upper(new Cast(left, "varchar(4000)")),
      ("%" + right + "%").toUpperCase());
  }

  public static void setValue(final int index, final Condition condition,
    final Object value) {
    setValueInternal(-1, index, condition, value);

  }

  public static int setValueInternal(int i, final int index,
    final QueryValue condition, final Object value) {
    for (final QueryValue subCondition : condition.getQueryValues()) {
      if (subCondition instanceof Value) {
        final Value valueCondition = (Value)subCondition;
        i++;
        if (i == index) {
          valueCondition.setValue(value);
          return i;
        }
        i = setValueInternal(i, index, subCondition, value);
        if (i >= index) {
          return i;
        }
      }
    }
    return i;
  }

  public static SqlCondition sql(final String sql) {
    return new SqlCondition(sql);
  }

  public static SqlCondition sql(final String sql, final Object... parameters) {
    return new SqlCondition(sql, parameters);
  }
}
