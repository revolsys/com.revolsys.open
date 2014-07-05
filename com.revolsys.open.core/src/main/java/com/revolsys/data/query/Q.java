package com.revolsys.data.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.identifier.Identifier;
import com.revolsys.data.io.RecordStore;
import com.revolsys.data.query.functions.F;
import com.revolsys.data.record.schema.Attribute;

public class Q {

  public static Add add(final QueryValue left, final QueryValue right) {
    return new Add(left, right);
  }

  public static And and(final Condition... conditions) {
    final List<Condition> list = Arrays.asList(conditions);
    return and(list);
  }

  public static And and(final List<? extends Condition> conditions) {
    return new And(conditions);
  }

  public static QueryValue arithmatic(final Attribute field,
    final String operator, final Object value) {
    final Column column = new Column(field);
    final Value queryValue = new Value(field, value);
    return arithmatic(column, operator, queryValue);
  }

  public static QueryValue arithmatic(final QueryValue left,
    final String operator, final QueryValue right) {
    if ("+".equals(operator)) {
      return Q.add(left, right);
    } else if ("-".equals(operator)) {
      return Q.subtract(left, right);
    } else if ("*".equals(operator)) {
      return Q.multiply(left, right);
    } else if ("/".equals(operator)) {
      return Q.divide(left, right);
    } else if ("%".equals(operator) || "mod".equals(operator)) {
      return Q.mod(left, right);
    } else {
      throw new IllegalArgumentException("Operator " + operator
        + " not supported");
    }
  }

  public static QueryValue arithmatic(final String fieldName,
    final String operator, final Object value) {
    final Column column = new Column(fieldName);
    final Value queryValue = new Value(value);
    return arithmatic(column, operator, queryValue);

  }

  public static Between between(final Attribute attribute, final Object min,
    final Object max) {
    final Column column = new Column(attribute);
    final Value minCondition = new Value(attribute, min);
    final Value maxCondition = new Value(attribute, max);
    return new Between(column, minCondition, maxCondition);
  }

  public static Condition binary(final Attribute field, final String operator,
    final Object value) {
    final Column column = new Column(field);
    final Value queryValue = new Value(field, value);
    return binary(column, operator, queryValue);
  }

  public static Condition binary(final QueryValue left, final String operator,
    final QueryValue right) {
    if ("=".equals(operator)) {
      return Q.equal(left, right);
    } else if ("<>".equals(operator) || "!=".equals(operator)) {
      return Q.notEqual(left, right);
    } else if ("<".equals(operator)) {
      return Q.lessThan(left, right);
    } else if ("<=".equals(operator)) {
      return Q.lessThanEqual(left, right);
    } else if (">".equals(operator)) {
      return Q.greaterThan(left, right);
    } else if (">=".equals(operator)) {
      return Q.greaterThanEqual(left, right);
    } else {
      throw new IllegalArgumentException("Operator " + operator
        + " not supported");
    }
  }

  public static Condition binary(final String fieldName, final String operator,
    final Object value) {
    final Column column = new Column(fieldName);
    final Value queryValue = new Value(value);
    return binary(column, operator, queryValue);

  }

  private static Divide divide(final QueryValue left, final QueryValue right) {
    return new Divide(left, right);
  }

  public static Equal equal(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new Equal(left, valueCondition);
  }

  public static Equal equal(final QueryValue left, final QueryValue right) {
    return new Equal(left, right);
  }

  public static Condition equal(final Identifier identifier,
    final List<?> attributes) {
    final And and = new And();
    List<Object> values;
    if (identifier == null) {
      values = Arrays.asList(new Object[attributes.size()]);
    } else {
      values = identifier.getValues();
    }
    if (attributes.size() == values.size()) {
      for (int i = 0; i < attributes.size(); i++) {
        final Object attributeKey = attributes.get(i);
        final Object value = values.get(i);

        Condition condition;
        if (value == null) {
          if (attributeKey instanceof Attribute) {
            condition = isNull((Attribute)attributeKey);
          } else {
            condition = isNull(attributeKey.toString());
          }
        } else {
          if (attributeKey instanceof Attribute) {
            condition = equal((Attribute)attributeKey, value);
          } else {
            condition = equal(attributeKey.toString(), value);
          }
        }
        and.add(condition);
      }
    } else {
      throw new IllegalArgumentException("Attribute count for " + attributes
        + " != count for values " + values);
    }
    return and;
  }

  public static Equal equal(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Equal(leftCondition, right);
  }

  public static GreaterThan greaterThan(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return greaterThan(name, valueCondition);
  }

  public static GreaterThan greaterThan(final QueryValue left,
    final QueryValue right) {
    return new GreaterThan(left, right);
  }

  public static GreaterThan greaterThan(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return greaterThan(name, valueCondition);
  }

  public static GreaterThan greaterThan(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return new GreaterThan(column, right);
  }

  public static GreaterThanEqual greaterThanEqual(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return greaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanEqual(final QueryValue left,
    final QueryValue right) {
    return new GreaterThanEqual(left, right);
  }

  public static GreaterThanEqual greaterThanEqual(final String name,
    final Object value) {
    final Value valueCondition = new Value(value);
    return greaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanEqual(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return greaterThanEqual(column, right);
  }

  public static ILike iLike(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new ILike(left, valueCondition);
  }

  public static ILike iLike(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new ILike(leftCondition, right);
  }

  public static Condition iLike(final String left, final String right) {
    return Q.like(F.upper(new Cast(left, "varchar(4000)")),
      ("%" + right + "%").toUpperCase());
  }

  public static In in(final Attribute attribute,
    final Collection<? extends Object> values) {
    return new In(attribute, values);
  }

  public static In in(final Attribute attribute, final Object... values) {
    final List<Object> list = Arrays.asList(values);
    return new In(attribute, list);
  }

  public static In in(final String name,
    final Collection<? extends Object> values) {
    final Column left = new Column(name);
    final CollectionValue collectionValue = new CollectionValue(values);
    return new In(left, collectionValue);
  }

  public static IsNotNull isNotNull(final Attribute attribute) {
    final String name = attribute.getName();
    return isNotNull(name);
  }

  public static IsNotNull isNotNull(final String name) {
    final Column condition = new Column(name);
    return new IsNotNull(condition);
  }

  public static IsNull isNull(final Attribute attribute) {
    final String name = attribute.getName();
    return isNull(name);
  }

  public static IsNull isNull(final String name) {
    final Column condition = new Column(name);
    return new IsNull(condition);
  }

  public static LessThan lessThan(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final QueryValue left, final QueryValue right) {
    return new LessThan(left, right);
  }

  public static LessThan lessThan(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return lessThan(column, right);
  }

  public static LessThanEqual lessThanEqual(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return lessThanEqual(name, valueCondition);
  }

  public static LessThanEqual lessThanEqual(final QueryValue left,
    final QueryValue right) {
    return new LessThanEqual(left, right);
  }

  public static LessThanEqual lessThanEqual(final String name,
    final Object value) {
    final Value valueCondition = new Value(value);
    return lessThanEqual(name, valueCondition);
  }

  public static LessThanEqual lessThanEqual(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return new LessThanEqual(column, right);
  }

  public static Like like(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return like(name, valueCondition);
  }

  public static Like like(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new Like(left, valueCondition);
  }

  public static Like like(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return like(name, valueCondition);
  }

  public static Like like(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Like(leftCondition, right);
  }

  public static Condition likeRegEx(final RecordStore dataStore,
    final String fieldName, final Object value) {
    QueryValue left;
    if (dataStore.getClass().getName().contains("Oracle")) {
      left = F.regexpReplace(F.upper(fieldName), "[^A-Z0-9]", "");
    } else {
      left = F.regexpReplace(F.upper(fieldName), "[^A-Z0-9]", "", "g");
    }
    final String right = "%"
        + StringConverterRegistry.toString(value)
        .toUpperCase()
        .replaceAll("[^A-Z0-9]", "") + "%";
    return Q.like(left, right);
  }

  private static Mod mod(final QueryValue left, final QueryValue right) {
    return new Mod(left, right);
  }

  private static Multiply multiply(final QueryValue left, final QueryValue right) {
    return new Multiply(left, right);
  }

  public static Not not(final Condition condition) {
    return new Not(condition);
  }

  public static NotEqual notEqual(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return notEqual(name, valueCondition);
  }

  public static NotEqual notEqual(final QueryValue left, final QueryValue right) {
    return new NotEqual(left, right);
  }

  public static NotEqual notEqual(final String name, final Object value) {
    return notEqual(name, new Value(value));
  }

  public static NotEqual notEqual(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new NotEqual(column, right);
  }

  public static Or or(final Condition... conditions) {
    final List<Condition> list = Arrays.asList(conditions);
    return or(list);
  }

  public static Or or(final List<? extends Condition> conditions) {
    return new Or(conditions);
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

  private static Subtract subtract(final QueryValue left, final QueryValue right) {
    return new Subtract(left, right);
  }
}
