package com.revolsys.odata.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.core.edm.EdmPropertyImpl;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePrimitiveProperty;
import org.apache.olingo.server.api.uri.queryoption.expression.Alias;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Enumeration;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.LambdaRef;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;
import org.apache.olingo.server.api.uri.queryoption.expression.Method;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;
import org.apache.olingo.server.api.uri.queryoption.expression.TypeLiteral;
import org.apache.olingo.server.api.uri.queryoption.expression.Unary;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

import com.revolsys.record.query.Add;
import com.revolsys.record.query.And;
import com.revolsys.record.query.CollectionValue;
import com.revolsys.record.query.Divide;
import com.revolsys.record.query.Mod;
import com.revolsys.record.query.Multiply;
import com.revolsys.record.query.Negate;
import com.revolsys.record.query.Not;
import com.revolsys.record.query.Or;
import com.revolsys.record.query.Q;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Subtract;
import com.revolsys.record.query.TableReference;
import com.revolsys.record.query.Value;
import com.revolsys.record.query.functions.F;
import com.revolsys.record.query.functions.Upper;
import com.revolsys.record.query.functions.WithinDistance;

public class ODataExpressionHandler {
  private static Map<BinaryOperatorKind, BiFunction<QueryValue, QueryValue, ? extends QueryValue>> BINARY_HANDLERS = new LinkedHashMap<>();

  private static Map<UnaryOperatorKind, Function<QueryValue, QueryValue>> UNARY_HANDLERS = new LinkedHashMap<>();

  private static Map<MethodKind, Function<List<QueryValue>, QueryValue>> METHOD_HANDLERS = new LinkedHashMap<>();

  static {
    BINARY_HANDLERS.put(BinaryOperatorKind.ADD, Add::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.AND, And::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.DIV, Divide::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.EQ, Q.EQUAL);
    BINARY_HANDLERS.put(BinaryOperatorKind.GE, Q.GREATER_THAN_EQUAL);
    BINARY_HANDLERS.put(BinaryOperatorKind.GT, Q.GREATER_THAN);
    // TODO BINARY_HANDLERS.put(BinaryOperatorKind.HAS, Add::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.IN, Add::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.LE, (v1, v2) -> {
      if (v1 instanceof WithinDistance) {
        final WithinDistance wd = (WithinDistance)v1;
        wd.setDistance(v2);
        return wd;
      } else {
        return Q.lessThanEqual(v1, v2);
      }
    });
    BINARY_HANDLERS.put(BinaryOperatorKind.LT, Q.LESS_THAN);
    BINARY_HANDLERS.put(BinaryOperatorKind.MOD, Mod::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.MUL, Multiply::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.NE, Q.NOT_EQUAL);
    BINARY_HANDLERS.put(BinaryOperatorKind.OR, Or::new);
    BINARY_HANDLERS.put(BinaryOperatorKind.SUB, Subtract::new);

    UNARY_HANDLERS.put(UnaryOperatorKind.MINUS, Negate::new);
    UNARY_HANDLERS.put(UnaryOperatorKind.NOT, Not::new);

    // COMPUTE_AGGREGATE("aggregate"), //
    METHOD_HANDLERS.put(MethodKind.CONTAINS, (args) -> {

      QueryValue left = args.get(0);
      QueryValue right = args.get(1);
      if (left instanceof Upper && right instanceof Upper) {
        left = left.getQueryValues().get(0);
        right = right.getQueryValues().get(0);
        if (right instanceof Value) {
          final Value value = (Value)right;
          return Q.iLike(left, "%" + value.getValue() + "%");
        } else {
          return Q.iLike(left, right);
        }
      } else {
        if (right instanceof Value) {
          final Value value = (Value)right;
          return Q.like(left, value.getValue().toString());
        } else {
          return Q.like(left, right);
        }
      }
    });
    // STARTSWITH("startswith"), //
    // ENDSWITH("endswith"), //
    // LENGTH("length"), //
    // INDEXOF("indexof"), //
    // SUBSTRING("substring"), //
    METHOD_HANDLERS.put(MethodKind.TOLOWER, (args) -> F.lower(args.get(0)));
    METHOD_HANDLERS.put(MethodKind.TOUPPER, (args) -> F.upper(args.get(0)));
    // TRIM("trim"), //
    // CONCAT("concat"), //
    // YEAR("year"), //
    // MONTH("month"), //
    // DAY("day"), //
    // HOUR("hour"), //
    // MINUTE("minute"), //
    // SECOND("second"), //
    // FRACTIONALSECONDS("fractionalseconds"), //
    // TOTALSECONDS("totalseconds"), //
    // DATE("date"), //
    // TIME("time"), //
    // TOTALOFFSETMINUTES("totaloffsetminutes"), //
    // MINDATETIME("mindatetime"), //
    // MAXDATETIME("maxdatetime"), //
    // NOW("now"), //
    // ROUND("round"), //
    // FLOOR("floor"), //
    // CEILING("ceiling"), //
    METHOD_HANDLERS.put(MethodKind.GEODISTANCE, (values) -> {
      final QueryValue value1 = values.get(0);
      final QueryValue value2 = values.get(1);
      return new WithinDistance(value1, value2, Value.newValue(0));
    });
    // GEOLENGTH("geo.length"), //
    METHOD_HANDLERS.put(MethodKind.GEOINTERSECTS, (values) -> {
      final QueryValue value2 = values.get(1);
      if (value2 instanceof CollectionValue) {
        final QueryValue newValue = ((CollectionValue)value2).getQueryValues().get(0);
        values.set(1, newValue);
      }
      return F.envelopeIntersects(values);
    });
    // CAST("cast"), //
    // ISOF("isof"), //
    // SUBSTRINGOF("substringof");
  }

  public static QueryValue toQueryValue(final TableReference table, final Expression expression) {
    if (expression instanceof Alias) {
      final Alias alias = (Alias)expression;
      return null;// TODO
    } else if (expression instanceof Binary) {
      final Binary binary = (Binary)expression;
      final Expression leftOperand = binary.getLeftOperand();
      final Expression rightOperand = binary.getRightOperand();
      final BinaryOperatorKind operator = binary.getOperator();
      final QueryValue left = toQueryValue(table, leftOperand);
      final QueryValue right = toQueryValue(table, rightOperand);
      return BINARY_HANDLERS.get(operator).apply(left, right);
    } else if (expression instanceof Enumeration) {
      final Enumeration enumeration = (Enumeration)expression;
      return null;// TODO
    } else if (expression instanceof LambdaRef) {
      final LambdaRef lambdaRef = (LambdaRef)expression;
      return null;// TODO
    } else if (expression instanceof Literal) {
      final Literal literal = (Literal)expression;
      String text = literal.getText();
      final EdmType literalType = literal.getType();
      if (literalType == null) {
        if ("null".equals(text)) {
          return null;
        } else {
          return Value.newValue(text);
        }
      } else {
        final FullQualifiedName typeName = literalType.getFullQualifiedName();
        final EdmPrimitiveTypeKind primitiveKind = EdmPrimitiveTypeKind.valueOfFQN(typeName);
        if (primitiveKind != null) {
          if (text.startsWith("'") && text.endsWith("'")) {
            text = text.substring(1, text.length() - 1);
          }
          final Object value = EdmPropertyImpl.toValue(primitiveKind, text);
          return Value.newValue(value);
        }

        return null;// TODO

      }
    } else if (expression instanceof Member) {
      final Member member = (Member)expression;
      final UriInfoResource path = member.getResourcePath();
      final UriResource uriResource = path.getUriResourceParts().get(0);
      if (uriResource instanceof UriResourcePrimitiveProperty) {
        final EdmProperty edmProperty = ((UriResourcePrimitiveProperty)uriResource).getProperty();
        final String propertyName = edmProperty.getName();
        return table.getColumn(propertyName);
      } else {
        return null;// TODO
      }
    } else if (expression instanceof Method) {
      final Method method = (Method)expression;
      final MethodKind methodKind = method.getMethod();

      final Function<List<QueryValue>, QueryValue> methodHandler = METHOD_HANDLERS.get(methodKind);
      if (methodHandler == null) {
        System.err.println(methodKind);
        return null;// TODO;
      } else {
        final List<Expression> parameters = method.getParameters();
        final List<QueryValue> args = new ArrayList<>(parameters.size());
        for (final Expression parameter : parameters) {
          final QueryValue arg = toQueryValue(table, parameter);
          args.add(arg);
        }
        return methodHandler.apply(args);
      }

    } else if (expression instanceof TypeLiteral) {
      final TypeLiteral typeLiteral = (TypeLiteral)expression;
      return null; // TODO
    } else if (expression instanceof Unary) {
      final Unary unary = (Unary)expression;
      final Expression operand = unary.getOperand();
      final UnaryOperatorKind operator = unary.getOperator();
      final QueryValue value = toQueryValue(table, operand);
      return UNARY_HANDLERS.get(operator).apply(value);
    } else {
      return null;
    }
  }
}
