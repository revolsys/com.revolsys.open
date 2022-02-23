/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.olingo.server.core.uri.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmReturnType;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.EdmTypeDefinition;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.core.edm.Edm;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.UriResourceLambdaVariable;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.AliasQueryOption;
import org.apache.olingo.server.api.uri.queryoption.apply.AggregateExpression;
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
import org.apache.olingo.server.core.uri.UriInfoImpl;
import org.apache.olingo.server.core.uri.UriResourceComplexPropertyImpl;
import org.apache.olingo.server.core.uri.UriResourceCountImpl;
import org.apache.olingo.server.core.uri.UriResourceEntitySetImpl;
import org.apache.olingo.server.core.uri.UriResourceFunctionImpl;
import org.apache.olingo.server.core.uri.UriResourceItImpl;
import org.apache.olingo.server.core.uri.UriResourceLambdaAllImpl;
import org.apache.olingo.server.core.uri.UriResourceLambdaAnyImpl;
import org.apache.olingo.server.core.uri.UriResourceLambdaVarImpl;
import org.apache.olingo.server.core.uri.UriResourceNavigationPropertyImpl;
import org.apache.olingo.server.core.uri.UriResourcePrimitivePropertyImpl;
import org.apache.olingo.server.core.uri.UriResourceRootImpl;
import org.apache.olingo.server.core.uri.UriResourceSingletonImpl;
import org.apache.olingo.server.core.uri.UriResourceStartingTypeFilterImpl;
import org.apache.olingo.server.core.uri.UriResourceTypedImpl;
import org.apache.olingo.server.core.uri.UriResourceWithKeysImpl;
import org.apache.olingo.server.core.uri.parser.UriTokenizer.TokenKind;
import org.apache.olingo.server.core.uri.queryoption.expression.AliasImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.BinaryImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.EnumerationImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.LiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MemberImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.MethodImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.TypeLiteralImpl;
import org.apache.olingo.server.core.uri.queryoption.expression.UnaryImpl;
import org.apache.olingo.server.core.uri.validator.UriValidationException;

public class ExpressionParser {
  private static final Map<TokenKind, BinaryOperatorKind> tokenToBinaryOperator;
  static {
    final Map<TokenKind, BinaryOperatorKind> temp = new EnumMap<>(TokenKind.class);
    temp.put(TokenKind.OrOperator, BinaryOperatorKind.OR);
    temp.put(TokenKind.AndOperator, BinaryOperatorKind.AND);

    temp.put(TokenKind.EqualsOperator, BinaryOperatorKind.EQ);
    temp.put(TokenKind.NotEqualsOperator, BinaryOperatorKind.NE);

    temp.put(TokenKind.GreaterThanOperator, BinaryOperatorKind.GT);
    temp.put(TokenKind.GreaterThanOrEqualsOperator, BinaryOperatorKind.GE);
    temp.put(TokenKind.LessThanOperator, BinaryOperatorKind.LT);
    temp.put(TokenKind.LessThanOrEqualsOperator, BinaryOperatorKind.LE);

    temp.put(TokenKind.AddOperator, BinaryOperatorKind.ADD);
    temp.put(TokenKind.SubOperator, BinaryOperatorKind.SUB);

    temp.put(TokenKind.MulOperator, BinaryOperatorKind.MUL);
    temp.put(TokenKind.DivOperator, BinaryOperatorKind.DIV);
    temp.put(TokenKind.ModOperator, BinaryOperatorKind.MOD);

    tokenToBinaryOperator = Collections.unmodifiableMap(temp);
  }

  // 'cast' and 'isof' are handled specially.
  private static final Map<TokenKind, MethodKind> tokenToMethod;
  static {
    final Map<TokenKind, MethodKind> temp = new EnumMap<>(TokenKind.class);
    temp.put(TokenKind.AggregateTrafo, MethodKind.COMPUTE_AGGREGATE);
    temp.put(TokenKind.CeilingMethod, MethodKind.CEILING);
    temp.put(TokenKind.ConcatMethod, MethodKind.CONCAT);
    temp.put(TokenKind.ContainsMethod, MethodKind.CONTAINS);
    temp.put(TokenKind.DateMethod, MethodKind.DATE);
    temp.put(TokenKind.DayMethod, MethodKind.DAY);
    temp.put(TokenKind.EndswithMethod, MethodKind.ENDSWITH);
    temp.put(TokenKind.FloorMethod, MethodKind.FLOOR);
    temp.put(TokenKind.FractionalsecondsMethod, MethodKind.FRACTIONALSECONDS);
    temp.put(TokenKind.GeoDistanceMethod, MethodKind.GEODISTANCE);
    temp.put(TokenKind.GeoIntersectsMethod, MethodKind.GEOINTERSECTS);
    temp.put(TokenKind.GeoLengthMethod, MethodKind.GEOLENGTH);
    temp.put(TokenKind.HourMethod, MethodKind.HOUR);
    temp.put(TokenKind.IndexofMethod, MethodKind.INDEXOF);
    temp.put(TokenKind.LengthMethod, MethodKind.LENGTH);
    temp.put(TokenKind.MaxdatetimeMethod, MethodKind.MAXDATETIME);
    temp.put(TokenKind.MindatetimeMethod, MethodKind.MINDATETIME);
    temp.put(TokenKind.MinuteMethod, MethodKind.MINUTE);
    temp.put(TokenKind.MonthMethod, MethodKind.MONTH);
    temp.put(TokenKind.NowMethod, MethodKind.NOW);
    temp.put(TokenKind.RoundMethod, MethodKind.ROUND);
    temp.put(TokenKind.SecondMethod, MethodKind.SECOND);
    temp.put(TokenKind.StartswithMethod, MethodKind.STARTSWITH);
    temp.put(TokenKind.SubstringMethod, MethodKind.SUBSTRING);
    temp.put(TokenKind.TimeMethod, MethodKind.TIME);
    temp.put(TokenKind.TolowerMethod, MethodKind.TOLOWER);
    temp.put(TokenKind.TotaloffsetminutesMethod, MethodKind.TOTALOFFSETMINUTES);
    temp.put(TokenKind.TotalsecondsMethod, MethodKind.TOTALSECONDS);
    temp.put(TokenKind.ToupperMethod, MethodKind.TOUPPER);
    temp.put(TokenKind.TrimMethod, MethodKind.TRIM);
    temp.put(TokenKind.YearMethod, MethodKind.YEAR);
    temp.put(TokenKind.SubstringofMethod, MethodKind.SUBSTRINGOF);

    tokenToMethod = Collections.unmodifiableMap(temp);
  }

  protected static EdmType getType(final Expression expression) throws UriParserException {
    EdmType type;
    if (expression instanceof Literal) {
      type = ((Literal)expression).getType();
    } else if (expression instanceof TypeLiteral) {
      type = ((TypeLiteral)expression).getType();
    } else if (expression instanceof Enumeration) {
      type = ((Enumeration)expression).getType();
    } else if (expression instanceof Member) {
      type = ((Member)expression).getType();
    } else if (expression instanceof Unary) {
      type = ((UnaryImpl)expression).getType();
    } else if (expression instanceof Binary) {
      type = ((BinaryImpl)expression).getType();
    } else if (expression instanceof Method) {
      type = ((MethodImpl)expression).getType();
    } else if (expression instanceof Alias) {
      final AliasQueryOption alias = ((AliasImpl)expression).getAlias();
      type = alias == null || alias.getValue() == null ? null : getType(alias.getValue());
    } else if (expression instanceof LambdaRef) {
      throw new UriParserSemanticException("Type determination not implemented.",
        UriParserSemanticException.MessageKeys.NOT_IMPLEMENTED, expression.toString());
    } else {
      throw new UriParserSemanticException("Unknown expression type.",
        UriParserSemanticException.MessageKeys.NOT_IMPLEMENTED, expression.toString());
    }
    if (type != null && type.getKind() == EdmTypeKind.DEFINITION) {
      type = ((EdmTypeDefinition)type).getUnderlyingType();
    }
    return type;
  }

  private final Edm edm;

  private UriTokenizer tokenizer;

  private final Deque<UriResourceLambdaVariable> lambdaVariables = new ArrayDeque<>();

  private EdmType referringType;

  private Collection<String> crossjoinEntitySetNames;

  private Map<String, AliasQueryOption> aliases;

  public ExpressionParser(final Edm edm) {
    this.edm = edm;
  }

  private void checkEqualityTypes(final Expression left, final Expression right)
    throws UriParserException {
    checkNoCollection(left);
    checkNoCollection(right);

    final EdmType leftType = getType(left);
    final EdmType rightType = getType(right);
    if (leftType == null || rightType == null || leftType.equals(rightType)) {
      return;
    }

    // Numeric promotion for Edm.Byte and Edm.SByte
    if (isType(leftType, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte)
      && isType(rightType, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte)) {
      return;
    }

    if (leftType.getKind() != EdmTypeKind.PRIMITIVE || rightType.getKind() != EdmTypeKind.PRIMITIVE
      || !(((EdmPrimitiveType)leftType).isCompatible((EdmPrimitiveType)rightType)
        || ((EdmPrimitiveType)rightType).isCompatible((EdmPrimitiveType)leftType))) {
      throw new UriParserSemanticException("Incompatible types.",
        UriParserSemanticException.MessageKeys.TYPES_NOT_COMPATIBLE,
        leftType.getFullQualifiedName().getFullQualifiedNameAsString(),
        rightType.getFullQualifiedName().getFullQualifiedNameAsString());
    }
  }

  /**
   * @param expressionList
   * @param leftExprType
   * @throws UriParserException
   * @throws UriParserSemanticException
   */
  private void checkInExpressionTypes(final List<Expression> expressionList,
    final EdmType leftExprType) throws UriParserException, UriParserSemanticException {
    for (final Expression expr : expressionList) {
      final EdmType inExprType = getType(expr);

      if (!((EdmPrimitiveType)leftExprType).isCompatible((EdmPrimitiveType)inExprType)) {
        throw new UriParserSemanticException("Incompatible types.",
          UriParserSemanticException.MessageKeys.TYPES_NOT_COMPATIBLE,
          inExprType == null ? ""
            : inExprType.getFullQualifiedName().getFullQualifiedNameAsString(),
          leftExprType.getFullQualifiedName().getFullQualifiedNameAsString());
      }
    }
  }

  protected void checkIntegerType(final Expression expression) throws UriParserException {
    checkNoCollection(expression);
    checkType(expression, EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Int32,
      EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte);
  }

  private void checkNoCollection(final Expression expression) throws UriParserException {
    if (expression instanceof Member && ((Member)expression).isCollection()) {
      throw new UriParserSemanticException("Collection not allowed.",
        UriParserSemanticException.MessageKeys.COLLECTION_NOT_ALLOWED);
    }
  }

  protected void checkNumericType(final Expression expression) throws UriParserException {
    checkNoCollection(expression);
    checkType(expression, EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Int32,
      EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte,
      EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single, EdmPrimitiveTypeKind.Double);
  }

  private void checkRelationTypes(final Expression left, final Expression right)
    throws UriParserException {
    checkNoCollection(left);
    checkNoCollection(right);
    final EdmType leftType = getType(left);
    final EdmType rightType = getType(right);
    checkType(left, EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Int32,
      EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte,
      EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single, EdmPrimitiveTypeKind.Double,
      EdmPrimitiveTypeKind.Boolean, EdmPrimitiveTypeKind.Guid, EdmPrimitiveTypeKind.String,
      EdmPrimitiveTypeKind.Date, EdmPrimitiveTypeKind.TimeOfDay,
      EdmPrimitiveTypeKind.DateTimeOffset, EdmPrimitiveTypeKind.Duration);
    checkType(right, EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Int32,
      EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte,
      EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single, EdmPrimitiveTypeKind.Double,
      EdmPrimitiveTypeKind.Boolean, EdmPrimitiveTypeKind.Guid, EdmPrimitiveTypeKind.String,
      EdmPrimitiveTypeKind.Date, EdmPrimitiveTypeKind.TimeOfDay,
      EdmPrimitiveTypeKind.DateTimeOffset, EdmPrimitiveTypeKind.Duration);
    if (leftType == null || rightType == null) {
      return;
    }
    if (!(((EdmPrimitiveType)leftType).isCompatible((EdmPrimitiveType)rightType)
      || ((EdmPrimitiveType)rightType).isCompatible((EdmPrimitiveType)leftType))) {
      throw new UriParserSemanticException("Incompatible types.",
        UriParserSemanticException.MessageKeys.TYPES_NOT_COMPATIBLE,
        leftType.getFullQualifiedName().getFullQualifiedNameAsString(),
        rightType.getFullQualifiedName().getFullQualifiedNameAsString());
    }
  }

  private void checkStructuredTypeFilter(final EdmType type, final EdmType filterType)
    throws UriParserException {
    if (!(filterType instanceof EdmStructuredType
      && ((EdmStructuredType)filterType).compatibleTo(type))) {
      throw new UriParserSemanticException("Incompatible type filter.",
        UriParserSemanticException.MessageKeys.INCOMPATIBLE_TYPE_FILTER,
        filterType.getFullQualifiedName().getFullQualifiedNameAsString());
    }
  }

  private void checkType(final Expression expression, final EdmPrimitiveTypeKind... kinds)
    throws UriParserException {
    final EdmType type = getType(expression);
    if (!isType(type, kinds)) {
      throw new UriParserSemanticException("Incompatible types.",
        UriParserSemanticException.MessageKeys.TYPES_NOT_COMPATIBLE,
        type == null ? "" : type.getFullQualifiedName().getFullQualifiedNameAsString(),
        Arrays.deepToString(kinds));
    }
  }

  private Enumeration createEnumExpression(final String primitiveValueLiteral)
    throws UriParserException {
    final EdmEnumType enumType = getEnumType(primitiveValueLiteral);
    // The Enumeration interface could be extended to handle the value as a
    // whole, in line with the primitive type.
    try {
      return new EnumerationImpl(enumType,
        Arrays.asList(enumType.fromUriLiteral(primitiveValueLiteral).split(",")));
    } catch (final EdmPrimitiveTypeException e) {
      // This part should not be reached, so a general error message key can be
      // re-used.
      throw new UriParserSemanticException(
        "Wrong enumeration value '" + primitiveValueLiteral + "'.", e,
        UriParserSemanticException.MessageKeys.UNKNOWN_PART, primitiveValueLiteral);
    }
  }

  private EdmPrimitiveTypeKind determineIntegerType(final String intValueAsString)
    throws UriParserSyntaxException {
    EdmPrimitiveTypeKind typeKind = null;
    try {
      final long value = Long.parseLong(intValueAsString);
      if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
        typeKind = EdmPrimitiveTypeKind.SByte;
      } else if (value >= 0 && value <= 255) {
        typeKind = EdmPrimitiveTypeKind.Byte;
      } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
        typeKind = EdmPrimitiveTypeKind.Int16;
      } else if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
        typeKind = EdmPrimitiveTypeKind.Int32;
      } else {
        typeKind = EdmPrimitiveTypeKind.Int64;
      }
    } catch (final NumberFormatException e) {
      // The number cannot be formatted wrongly because the tokenizer already
      // checked the format
      // but it is too large for Long and therefore too large for Edm.Int64.
      typeKind = EdmPrimitiveTypeKind.Decimal;
    }
    return typeKind;
  }

  private EdmType getAddSubTypeAndCheckLeftAndRight(final Expression left, final Expression right,
    final boolean isSub) throws UriParserException {
    checkNoCollection(left);
    checkNoCollection(right);
    final EdmType leftType = getType(left);
    final EdmType rightType = getType(right);
    if (isType(leftType, EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Int32,
      EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte,
      EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single, EdmPrimitiveTypeKind.Double)
      && isType(rightType, EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Int32,
        EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte,
        EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single, EdmPrimitiveTypeKind.Double)) {
      // The result type must be able to handle the overflow,
      // so we return always a wider type than the types of the operands.
      if (isType(leftType, EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single,
        EdmPrimitiveTypeKind.Double)
        || isType(rightType, EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single,
          EdmPrimitiveTypeKind.Double)) {
        return EdmPrimitiveTypeKind.Double.getInstance();
      } else if (isType(leftType, EdmPrimitiveTypeKind.Int64)
        || isType(rightType, EdmPrimitiveTypeKind.Int64)) {
        return EdmPrimitiveTypeKind.Decimal.getInstance();
      } else if (isType(leftType, EdmPrimitiveTypeKind.Int32)
        || isType(rightType, EdmPrimitiveTypeKind.Int32)) {
        return EdmPrimitiveTypeKind.Int64.getInstance();
      } else if (isType(leftType, EdmPrimitiveTypeKind.Int16)
        || isType(rightType, EdmPrimitiveTypeKind.Int16)) {
        return EdmPrimitiveTypeKind.Int32.getInstance();
      } else {
        return EdmPrimitiveTypeKind.Int16.getInstance();
      }
    }
    if ((isType(leftType, EdmPrimitiveTypeKind.DateTimeOffset)
      || isType(leftType, EdmPrimitiveTypeKind.Date)
      || isType(leftType, EdmPrimitiveTypeKind.Duration))
      && isType(rightType, EdmPrimitiveTypeKind.Duration)) {
      return leftType;
    }
    if (isSub && (isType(leftType, EdmPrimitiveTypeKind.DateTimeOffset)
      && isType(rightType, EdmPrimitiveTypeKind.DateTimeOffset)
      || isType(leftType, EdmPrimitiveTypeKind.Date)
        && isType(rightType, EdmPrimitiveTypeKind.Date))) {
      return EdmPrimitiveTypeKind.Duration.getInstance();
    }
    throw new UriParserSemanticException("Incompatible types.",
      UriParserSemanticException.MessageKeys.TYPES_NOT_COMPATIBLE,
      leftType.getFullQualifiedName().getFullQualifiedNameAsString(),
      rightType.getFullQualifiedName().getFullQualifiedNameAsString());
  }

  private EdmEnumType getEnumType(final String primitiveValueLiteral) throws UriParserException {
    final String enumTypeName = primitiveValueLiteral.substring(0,
      primitiveValueLiteral.indexOf('\''));
    final EdmEnumType type = this.edm.getEnumType(new FullQualifiedName(enumTypeName));
    if (type == null) {
      throw new UriParserSemanticException("Unknown Enum type '" + enumTypeName + "'.",
        UriParserSemanticException.MessageKeys.UNKNOWN_TYPE, enumTypeName);
    }
    return type;
  }

  private EdmType getPrimitiveType(final FullQualifiedName fullQualifiedName) {
    if (EdmPrimitiveType.EDM_NAMESPACE.equals(fullQualifiedName.getNamespace())) {
      final EdmPrimitiveTypeKind primitiveTypeKind = EdmPrimitiveTypeKind
        .valueOf(fullQualifiedName.getName());
      return primitiveTypeKind == null ? null : primitiveTypeKind.getInstance();
    } else {
      return null;
    }
  }

  private boolean isEnumType(final Expression expression) throws UriParserException {
    final EdmType expressionType = getType(expression);
    return expressionType == null || expressionType.getKind() == EdmTypeKind.ENUM
      || isType(expressionType, EdmPrimitiveTypeKind.Int16, EdmPrimitiveTypeKind.Int32,
        EdmPrimitiveTypeKind.Int64, EdmPrimitiveTypeKind.Byte, EdmPrimitiveTypeKind.SByte);
  }

  private boolean isType(final EdmType type, final EdmPrimitiveTypeKind... kinds)
    throws UriParserException {
    if (type == null) {
      return true;
    }
    for (final EdmPrimitiveTypeKind kind : kinds) {
      if (type.equals(kind.getInstance())) {
        return true;
      }
    }
    return false;
  }

  public Expression parse(final UriTokenizer tokenizer, final EdmType referringType,
    final Collection<String> crossjoinEntitySetNames, final Map<String, AliasQueryOption> aliases)
    throws UriParserException, UriValidationException {
    // Initialize tokenizer.
    this.tokenizer = tokenizer;
    this.referringType = referringType;
    this.crossjoinEntitySetNames = crossjoinEntitySetNames;
    this.aliases = aliases;

    final Expression expression = parseExpression();
    checkNoCollection(expression);
    return expression;
  }

  private Expression parseAnd() throws UriParserException, UriValidationException {
    Expression left = parseExprEquality();
    while (this.tokenizer.next(TokenKind.AndOperator)) {
      checkType(left, EdmPrimitiveTypeKind.Boolean);
      checkNoCollection(left);
      final Expression right = parseExprEquality();
      checkType(right, EdmPrimitiveTypeKind.Boolean);
      checkNoCollection(right);
      left = new BinaryImpl(left, BinaryOperatorKind.AND, right,
        EdmPrimitiveTypeKind.Boolean.getInstance());
    }
    return left;
  }

  private void parseBoundFunction(final FullQualifiedName fullQualifiedName,
    final UriInfoImpl uriInfo, final UriResourcePartTyped lastResource)
    throws UriParserException, UriValidationException {
    final EdmType type = lastResource.getType();
    final List<UriParameter> parameters = ParserHelper.parseFunctionParameters(this.tokenizer,
      this.edm, this.referringType, true, this.aliases);
    final List<String> parameterNames = ParserHelper.getParameterNames(parameters);
    final EdmFunction boundFunction = this.edm.getBoundFunction(fullQualifiedName,
      type.getFullQualifiedName(), lastResource.isCollection(), parameterNames);
    if (boundFunction == null) {
      throw new UriParserSemanticException("Bound function '" + fullQualifiedName + "' not found.",
        UriParserSemanticException.MessageKeys.FUNCTION_NOT_FOUND,
        fullQualifiedName.getFullQualifiedNameAsString());
    }
    ParserHelper.validateFunctionParameters(boundFunction, parameters, this.edm, this.referringType,
      this.aliases);
    parseFunctionRest(uriInfo, boundFunction, parameters);
  }

  private void parseCollectionNavigationExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {
    boolean hasSlash = false;
    if (this.tokenizer.next(TokenKind.SLASH)) {
      hasSlash = true;
      if (this.tokenizer.next(TokenKind.QualifiedName)) {
        final FullQualifiedName qualifiedName = new FullQualifiedName(this.tokenizer.getText());
        final EdmEntityType edmEntityType = this.edm.getEntityType(qualifiedName);
        if (edmEntityType == null) {
          parseBoundFunction(qualifiedName, uriInfo, lastResource);
        } else {
          setTypeFilter(lastResource, edmEntityType);
        }
        hasSlash = false;
      }
    }

    if (!hasSlash && this.tokenizer.next(TokenKind.OPEN)) {
      if (lastResource instanceof UriResourceNavigation) {
        ((UriResourceNavigationPropertyImpl)lastResource)
          .setKeyPredicates(ParserHelper.parseNavigationKeyPredicate(this.tokenizer,
            ((UriResourceNavigationPropertyImpl)lastResource).getProperty(), this.edm,
            this.referringType, this.aliases));
      } else if (lastResource instanceof UriResourceFunction
        && ((UriResourceFunction)lastResource).getType() instanceof EdmEntityType) {
        ((UriResourceFunctionImpl)lastResource).setKeyPredicates(ParserHelper.parseKeyPredicate(
          this.tokenizer, (EdmEntityType)((UriResourceFunction)lastResource).getType(), null,
          this.edm, this.referringType, this.aliases));
      } else {
        throw new UriParserSemanticException("Unknown or wrong resource type.",
          UriParserSemanticException.MessageKeys.NOT_IMPLEMENTED, lastResource.toString());
      }
      parseSingleNavigationExpr(uriInfo, lastResource);
    }

    if (hasSlash || this.tokenizer.next(TokenKind.SLASH)) {
      parseCollectionPathExpr(uriInfo, lastResource);
    }
  }

  private void parseCollectionPathExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {
    // The initial slash (see grammar) must have been checked and consumed by
    // the caller.
    if (this.tokenizer.next(TokenKind.COUNT)) {
      uriInfo.addResourcePart(new UriResourceCountImpl());
    } else if (this.tokenizer.next(TokenKind.ANY)) {
      uriInfo.addResourcePart(parseLambdaRest(TokenKind.ANY, lastResource));
    } else if (this.tokenizer.next(TokenKind.ALL)) {
      uriInfo.addResourcePart(parseLambdaRest(TokenKind.ALL, lastResource));
    } else if (this.tokenizer.next(TokenKind.QualifiedName)) {
      parseBoundFunction(new FullQualifiedName(this.tokenizer.getText()), uriInfo, lastResource);
    } else {
      throw new UriParserSyntaxException("Unexpected token.",
        UriParserSyntaxException.MessageKeys.SYNTAX);
    }
  }

  private void parseComplexPathExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {

    if (this.tokenizer.next(TokenKind.SLASH)) {
      if (this.tokenizer.next(TokenKind.QualifiedName)) {
        final FullQualifiedName fullQualifiedName = new FullQualifiedName(this.tokenizer.getText());
        final EdmComplexType edmComplexType = this.edm.getComplexType(fullQualifiedName);

        if (edmComplexType != null) {
          setTypeFilter(lastResource, edmComplexType);
          if (this.tokenizer.next(TokenKind.SLASH)) {
            parseComplexPathRestExpr(uriInfo, lastResource);
          }
        } else {
          // Must be a bound function.
          parseBoundFunction(fullQualifiedName, uriInfo, lastResource);
        }
      } else {
        parseComplexPathRestExpr(uriInfo, lastResource);
      }
    }
  }

  private void parseComplexPathRestExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {
    if (this.tokenizer.next(TokenKind.QualifiedName)) {
      final FullQualifiedName fullQualifiedName = new FullQualifiedName(this.tokenizer.getText());
      // Must be a bound function.
      parseBoundFunction(fullQualifiedName, uriInfo, lastResource);
    } else if (this.tokenizer.next(TokenKind.ODataIdentifier)) {
      parsePropertyPathExpr(uriInfo, lastResource);
    } else {
      throw new UriParserSyntaxException("Unexpected token.",
        UriParserSyntaxException.MessageKeys.SYNTAX);
    }
  }

  private void parseDollarIt(final UriInfoImpl uriInfo, final EdmType referringType)
    throws UriParserException, UriValidationException {
    final UriResourceItImpl itResource = new UriResourceItImpl(referringType, false);
    uriInfo.addResourcePart(itResource);
    if (this.tokenizer.next(TokenKind.SLASH)) {
      final TokenKind tokenKind = ParserHelper.next(this.tokenizer, TokenKind.QualifiedName,
        TokenKind.ODataIdentifier);
      parseMemberExpression(tokenKind, uriInfo, itResource, true);
    }
  }

  private void parseDollarRoot(final UriInfoImpl uriInfo)
    throws UriParserException, UriValidationException {
    final UriResourceRootImpl rootResource = new UriResourceRootImpl(this.referringType, true);
    uriInfo.addResourcePart(rootResource);
    ParserHelper.requireNext(this.tokenizer, TokenKind.SLASH);
    ParserHelper.requireNext(this.tokenizer, TokenKind.ODataIdentifier);
    final String name = this.tokenizer.getText();
    UriResourcePartTyped resource = null;
    final EdmEntitySet entitySet = this.edm.getEntityContainer().getEntitySet(name);
    if (entitySet == null) {
      final EdmSingleton singleton = this.edm.getEntityContainer().getSingleton(name);
      if (singleton == null) {
        throw new UriParserSemanticException("EntitySet or singleton expected.",
          UriParserSemanticException.MessageKeys.UNKNOWN_PART, name);
      } else {
        resource = new UriResourceSingletonImpl(singleton);
      }
    } else {
      ParserHelper.requireNext(this.tokenizer, TokenKind.OPEN);
      final List<UriParameter> keyPredicates = ParserHelper.parseKeyPredicate(this.tokenizer,
        entitySet.getEntityType(), null, this.edm, this.referringType, this.aliases);
      resource = new UriResourceEntitySetImpl(entitySet).setKeyPredicates(keyPredicates);
    }
    uriInfo.addResourcePart(resource);
    parseSingleNavigationExpr(uriInfo, resource);
  }

  private Expression parseExprAdd() throws UriParserException, UriValidationException {
    Expression left = parseExprMul();
    TokenKind operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.AddOperator,
      TokenKind.SubOperator);
    // Null for everything other than ADD or SUB
    while (operatorTokenKind != null) {
      final Expression right = parseExprMul();
      final EdmType resultType = getAddSubTypeAndCheckLeftAndRight(left, right,
        operatorTokenKind == TokenKind.SubOperator);
      left = new BinaryImpl(left, tokenToBinaryOperator.get(operatorTokenKind), right, resultType);
      operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.AddOperator,
        TokenKind.SubOperator);
    }
    return left;
  }

  private Expression parseExprEquality() throws UriParserException, UriValidationException {
    Expression left = parseExprRel();
    TokenKind operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.EqualsOperator,
      TokenKind.NotEqualsOperator);
    // Null for everything other than EQ or NE
    while (operatorTokenKind != null) {
      final Expression right = parseExprEquality();
      checkEqualityTypes(left, right);
      left = new BinaryImpl(left, tokenToBinaryOperator.get(operatorTokenKind), right,
        EdmPrimitiveTypeKind.Boolean.getInstance());
      operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.EqualsOperator,
        TokenKind.NotEqualsOperator);
    }
    return left;
  }

  private Expression parseExpression() throws UriParserException, UriValidationException {
    Expression left = parseAnd();
    while (this.tokenizer.next(TokenKind.OrOperator)) {
      checkType(left, EdmPrimitiveTypeKind.Boolean);
      checkNoCollection(left);
      final Expression right = parseAnd();
      checkType(right, EdmPrimitiveTypeKind.Boolean);
      checkNoCollection(right);
      left = new BinaryImpl(left, BinaryOperatorKind.OR, right,
        EdmPrimitiveTypeKind.Boolean.getInstance());
    }
    return left;
  }

  private Expression parseExprMul() throws UriParserException, UriValidationException {
    Expression left = parseExprUnary();
    TokenKind operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.MulOperator,
      TokenKind.DivOperator, TokenKind.ModOperator);
    // Null for everything other than MUL or DIV or MOD
    while (operatorTokenKind != null) {
      checkNumericType(left);
      final Expression right = parseExprUnary();
      checkNumericType(right);
      left = new BinaryImpl(left, tokenToBinaryOperator.get(operatorTokenKind), right,
        EdmPrimitiveTypeKind.Double.getInstance());
      operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.MulOperator,
        TokenKind.DivOperator, TokenKind.ModOperator);
    }
    return left;
  }

  private Expression parseExprPrimary() throws UriParserException, UriValidationException {
    final Expression left = parseExprValue();
    if (isEnumType(left) && this.tokenizer.next(TokenKind.HasOperator)) {
      ParserHelper.requireNext(this.tokenizer, TokenKind.EnumValue);
      final Expression right = createEnumExpression(this.tokenizer.getText());
      return new BinaryImpl(left, BinaryOperatorKind.HAS, right,
        EdmPrimitiveTypeKind.Boolean.getInstance());
    } else if (this.tokenizer.next(TokenKind.InOperator)) {
      final EdmType leftExprType = getType(left);
      final EdmPrimitiveTypeKind kinds = EdmPrimitiveTypeKind
        .valueOfFQN(leftExprType.getFullQualifiedName());
      if (this.tokenizer.next(TokenKind.OPEN)) {
        ParserHelper.bws(this.tokenizer);
        final List<Expression> expressionList = parseInExpr();
        checkInExpressionTypes(expressionList, leftExprType);
        return new BinaryImpl(left, BinaryOperatorKind.IN, expressionList,
          EdmPrimitiveTypeKind.Boolean.getInstance());
      } else {
        ParserHelper.bws(this.tokenizer);
        final Expression right = parseExpression();
        checkType(right, kinds);
        return new BinaryImpl(left, BinaryOperatorKind.IN, right,
          EdmPrimitiveTypeKind.Boolean.getInstance());
      }
    }
    return left;
  }

  private Expression parseExprRel() throws UriParserException, UriValidationException {
    if (this.tokenizer.next(TokenKind.IsofMethod)) {
      // The isof method is a terminal. So no further operators are allowed.
      return parseIsOfOrCastMethod(MethodKind.ISOF);
    } else {
      Expression left = parseExprAdd();
      TokenKind operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.GreaterThanOperator,
        TokenKind.GreaterThanOrEqualsOperator, TokenKind.LessThanOperator,
        TokenKind.LessThanOrEqualsOperator);
      // Null for everything other than GT or GE or LT or LE
      while (operatorTokenKind != null) {
        final Expression right = parseExprAdd();
        checkRelationTypes(left, right);
        left = new BinaryImpl(left, tokenToBinaryOperator.get(operatorTokenKind), right,
          EdmPrimitiveTypeKind.Boolean.getInstance());
        operatorTokenKind = ParserHelper.next(this.tokenizer, TokenKind.GreaterThanOperator,
          TokenKind.GreaterThanOrEqualsOperator, TokenKind.LessThanOperator,
          TokenKind.LessThanOrEqualsOperator);
      }
      return left;
    }
  }

  private Expression parseExprUnary() throws UriParserException, UriValidationException {
    if (this.tokenizer.next(TokenKind.MinusOperator)) {
      final Expression expression = parseExprPrimary();
      if (!isType(getType(expression), EdmPrimitiveTypeKind.Duration)) {
        checkNumericType(expression);
      }
      return new UnaryImpl(UnaryOperatorKind.MINUS, expression, getType(expression));
    } else if (this.tokenizer.next(TokenKind.NotOperator)) {
      final Expression expression = parseExprValue();
      checkType(expression, EdmPrimitiveTypeKind.Boolean);
      checkNoCollection(expression);
      return new UnaryImpl(UnaryOperatorKind.NOT, expression, getType(expression));
    } else if (this.tokenizer.next(TokenKind.CastMethod)) {
      return parseIsOfOrCastMethod(MethodKind.CAST);
    } else {
      return parseExprPrimary();
    }
  }

  private Expression parseExprValue() throws UriParserException, UriValidationException {
    if (this.tokenizer.next(TokenKind.OPEN)) {
      ParserHelper.bws(this.tokenizer);
      final Expression expression = parseExpression();
      ParserHelper.bws(this.tokenizer);
      ParserHelper.requireNext(this.tokenizer, TokenKind.CLOSE);
      return expression;
    }

    if (this.tokenizer.next(TokenKind.ParameterAliasName)) {
      final String name = this.tokenizer.getText();
      if (this.aliases.containsKey(name)) {
        return new AliasImpl(name, ParserHelper.parseAliasValue(name, null, true, true, this.edm,
          this.referringType, this.aliases));
      } else {
        return new AliasImpl(name, null);
      }
    }

    if (this.tokenizer.next(TokenKind.jsonArrayOrObject)) {
      // There is no obvious way how the type could be determined.
      return new LiteralImpl(this.tokenizer.getText(), null);
    }

    if (this.tokenizer.next(TokenKind.ROOT)) {
      return parseFirstMemberExpr(TokenKind.ROOT);
    }

    if (this.tokenizer.next(TokenKind.IT)) {
      return parseFirstMemberExpr(TokenKind.IT);
    }

    final TokenKind nextPrimitive = ParserHelper.nextPrimitiveValue(this.tokenizer);
    if (nextPrimitive != null) {
      return parsePrimitive(nextPrimitive);
    }

    final TokenKind nextMethod = ParserHelper.next(this.tokenizer,
      tokenToMethod.keySet().toArray(new TokenKind[tokenToMethod.size()]));
    if (nextMethod != null) {
      return parseMethod(nextMethod);
    }

    if (this.tokenizer.next(TokenKind.QualifiedName)) {
      return parseFirstMemberExpr(TokenKind.QualifiedName);
    }

    if (this.tokenizer.next(TokenKind.ODataIdentifier)) {
      return parseFirstMemberExpr(TokenKind.ODataIdentifier);
    }

    throw new UriParserSyntaxException("Unexpected token.",
      UriParserSyntaxException.MessageKeys.SYNTAX);
  }

  private Expression parseFirstMemberExpr(final TokenKind lastTokenKind)
    throws UriParserException, UriValidationException {

    final UriInfoImpl uriInfo = new UriInfoImpl();
    EdmType startTypeFilter = null;

    if (lastTokenKind == TokenKind.ROOT) {
      parseDollarRoot(uriInfo);
    } else if (lastTokenKind == TokenKind.IT) {
      parseDollarIt(uriInfo, this.referringType);
    } else if (lastTokenKind == TokenKind.QualifiedName) {
      // Special handling for leading type casts and type literals
      final FullQualifiedName fullQualifiedName = new FullQualifiedName(this.tokenizer.getText());
      EdmType filterType = this.edm.getEntityType(fullQualifiedName);
      if (filterType == null) {
        filterType = this.edm.getComplexType(fullQualifiedName);
      }

      if (filterType == null) {
        filterType = getPrimitiveType(fullQualifiedName);
      }

      if (filterType == null) {
        filterType = this.edm.getEnumType(fullQualifiedName);
      }

      if (filterType == null) {
        filterType = this.edm.getTypeDefinition(fullQualifiedName);
      }

      if (filterType != null) {
        if (this.tokenizer.next(TokenKind.SLASH)) {
          // Leading type cast
          checkStructuredTypeFilter(this.referringType, filterType);
          startTypeFilter = filterType;

          final TokenKind tokenKind = ParserHelper.next(this.tokenizer, TokenKind.QualifiedName,
            TokenKind.ODataIdentifier);
          parseMemberExpression(tokenKind, uriInfo,
            new UriResourceStartingTypeFilterImpl(filterType, false), false);
        } else {
          // Type literal
          return new TypeLiteralImpl(filterType);
        }
      } else {
        // Must be bound or unbound function.
        parseFunction(fullQualifiedName, uriInfo, this.referringType, true);
      }
    } else if (lastTokenKind == TokenKind.ODataIdentifier) {
      parseFirstMemberODataIdentifier(uriInfo);
    }

    return new MemberImpl(uriInfo, startTypeFilter);
  }

  private void parseFirstMemberODataIdentifier(final UriInfoImpl uriInfo)
    throws UriParserException, UriValidationException {
    final String name = this.tokenizer.getText();

    // For a crossjoin, the identifier must be an entity-set name.
    if (this.crossjoinEntitySetNames != null && !this.crossjoinEntitySetNames.isEmpty()) {
      if (this.crossjoinEntitySetNames.contains(name)) {
        final UriResourceEntitySetImpl resource = new UriResourceEntitySetImpl(
          this.edm.getEntityContainer().getEntitySet(name));
        uriInfo.addResourcePart(resource);
        if (this.tokenizer.next(TokenKind.SLASH)) {
          final TokenKind tokenKind = ParserHelper.next(this.tokenizer, TokenKind.QualifiedName,
            TokenKind.ODataIdentifier);
          parseMemberExpression(tokenKind, uriInfo, resource, true);
        }
        return;
      } else {
        throw new UriParserSemanticException("Unknown crossjoin entity set.",
          UriParserSemanticException.MessageKeys.UNKNOWN_PART, name);
      }
    }

    // Check if the OData identifier is a lambda variable, otherwise it must be
    // a property.
    UriResourceLambdaVariable lambdaVariable = null;
    for (final UriResourceLambdaVariable variable : this.lambdaVariables) {
      if (variable.getVariableName().equals(name)) {
        lambdaVariable = variable;
        break;
      }
    }
    if (lambdaVariable != null) {
      // Copy lambda variable into new resource, just in case ...
      final UriResourceLambdaVariable lambdaResource = new UriResourceLambdaVarImpl(
        lambdaVariable.getVariableName(), lambdaVariable.getType());
      uriInfo.addResourcePart(lambdaResource);
      if (this.tokenizer.next(TokenKind.SLASH)) {
        final TokenKind tokenKind = ParserHelper.next(this.tokenizer, TokenKind.QualifiedName,
          TokenKind.ODataIdentifier);
        parseMemberExpression(tokenKind, uriInfo, lambdaResource, true);
      }
    } else {
      // Must be a property.
      parseMemberExpression(TokenKind.ODataIdentifier, uriInfo, null, true);
    }
  }

  private void parseFunction(final FullQualifiedName fullQualifiedName, final UriInfoImpl uriInfo,
    final EdmType lastType, final boolean lastIsCollection)
    throws UriParserException, UriValidationException {

    final List<UriParameter> parameters = ParserHelper.parseFunctionParameters(this.tokenizer,
      this.edm, this.referringType, true, this.aliases);
    final List<String> parameterNames = ParserHelper.getParameterNames(parameters);
    final EdmFunction boundFunction = this.edm.getBoundFunction(fullQualifiedName,
      lastType.getFullQualifiedName(), lastIsCollection, parameterNames);

    if (boundFunction != null) {
      ParserHelper.validateFunctionParameters(boundFunction, parameters, this.edm,
        this.referringType, this.aliases);
      parseFunctionRest(uriInfo, boundFunction, parameters);
      return;
    }

    final EdmFunction unboundFunction = this.edm.getUnboundFunction(fullQualifiedName,
      parameterNames);
    if (unboundFunction != null) {
      ParserHelper.validateFunctionParameters(unboundFunction, parameters, this.edm,
        this.referringType, this.aliases);
      parseFunctionRest(uriInfo, unboundFunction, parameters);
      return;
    }

    throw new UriParserSemanticException("No function '" + fullQualifiedName + "' found.",
      UriParserSemanticException.MessageKeys.FUNCTION_NOT_FOUND,
      fullQualifiedName.getFullQualifiedNameAsString());
  }

  private void parseFunctionRest(final UriInfoImpl uriInfo, final EdmFunction function,
    final List<UriParameter> parameters) throws UriParserException, UriValidationException {
    final UriResourceFunction functionResource = new UriResourceFunctionImpl(null, function,
      parameters);
    uriInfo.addResourcePart(functionResource);

    final EdmReturnType edmReturnType = function.getReturnType();
    final EdmType edmType = edmReturnType.getType();
    final boolean isCollection = edmReturnType.isCollection();

    if (function.isComposable()) {
      if (edmType instanceof EdmEntityType) {
        if (isCollection) {
          parseCollectionNavigationExpr(uriInfo, functionResource);
        } else {
          parseSingleNavigationExpr(uriInfo, functionResource);
        }
      } else if (edmType instanceof EdmComplexType) {
        if (isCollection) {
          if (this.tokenizer.next(TokenKind.SLASH)) {
            parseCollectionPathExpr(uriInfo, functionResource);
          }
        } else {
          parseComplexPathExpr(uriInfo, functionResource);
        }
      } else if (edmType instanceof EdmPrimitiveType) {
        if (isCollection) {
          if (this.tokenizer.next(TokenKind.SLASH)) {
            parseCollectionPathExpr(uriInfo, functionResource);
          }
        } else {
          parseSinglePathExpr(uriInfo, functionResource);
        }
      }
    } else if (this.tokenizer.next(TokenKind.SLASH)) {
      throw new UriValidationException("Function is not composable.",
        UriValidationException.MessageKeys.UNALLOWED_RESOURCE_PATH, "");
    }
  }

  /**
   * @param expressionList
   * @throws UriParserException
   * @throws UriValidationException
   */
  private List<Expression> parseInExpr() throws UriParserException, UriValidationException {
    final List<Expression> expressionList = new ArrayList<>();
    while (!this.tokenizer.next(TokenKind.CLOSE)) {
      Expression expression = parseExpression();
      expressionList.add(expression);
      ParserHelper.bws(this.tokenizer);
      while (this.tokenizer.next(TokenKind.COMMA)) {
        ParserHelper.bws(this.tokenizer);
        expression = parseExpression();
        expressionList.add(expression);
        ParserHelper.bws(this.tokenizer);
      }
    }
    return expressionList;
  }

  private Expression parseIsOfOrCastMethod(final MethodKind kind)
    throws UriParserException, UriValidationException {
    // The TokenKind 'IsOfMethod' consumes also the opening parenthesis.
    // The first parameter could be an expression or a type literal.
    final List<Expression> parameters = new ArrayList<>();
    ParserHelper.bws(this.tokenizer);
    parameters.add(parseExpression());
    if (!(parameters.get(0) instanceof TypeLiteral)) {
      // The first parameter is not a type literal, so there must be a second
      // parameter.
      ParserHelper.bws(this.tokenizer);
      ParserHelper.requireNext(this.tokenizer, TokenKind.COMMA);
      ParserHelper.bws(this.tokenizer);
      parameters.add(parseExpression());
      ParserHelper.bws(this.tokenizer);
      // The second parameter must be a type literal.
      if (!(parameters.get(1) instanceof TypeLiteral)) {
        throw new UriParserSemanticException("Type literal expected.",
          UriParserSemanticException.MessageKeys.INCOMPATIBLE_TYPE_FILTER);
      }
    }

    ParserHelper.requireNext(this.tokenizer, TokenKind.CLOSE);
    return new MethodImpl(kind, parameters);
  }

  private UriResourcePartTyped parseLambdaRest(final TokenKind lastTokenKind,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {

    ParserHelper.requireNext(this.tokenizer, TokenKind.OPEN);
    if (lastTokenKind == TokenKind.ANY && this.tokenizer.next(TokenKind.CLOSE)) {
      return new UriResourceLambdaAnyImpl(null, null);
    }
    ParserHelper.requireNext(this.tokenizer, TokenKind.ODataIdentifier);
    final String lambbdaVariable = this.tokenizer.getText();
    ParserHelper.requireNext(this.tokenizer, TokenKind.COLON);
    this.lambdaVariables.addFirst(new UriResourceLambdaVarImpl(lambbdaVariable,
      lastResource == null ? this.referringType : lastResource.getType()));
    // The ABNF suggests that the "lambaPredicateExpr" must contain at least one
    // lambdaVariable,
    // so arguably this could be checked in expression parsing or later in
    // validation.
    final Expression lambdaPredicateExpr = parseExpression();
    this.lambdaVariables.removeFirst();
    ParserHelper.requireNext(this.tokenizer, TokenKind.CLOSE);
    if (lastTokenKind == TokenKind.ALL) {
      return new UriResourceLambdaAllImpl(lambbdaVariable, lambdaPredicateExpr);
    } else if (lastTokenKind == TokenKind.ANY) {
      return new UriResourceLambdaAnyImpl(lambbdaVariable, lambdaPredicateExpr);
    } else {
      throw new UriParserSyntaxException("Unexpected token.",
        UriParserSyntaxException.MessageKeys.SYNTAX);
    }
  }

  private void parseMemberExpression(final TokenKind lastTokenKind, final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource, final boolean allowTypeFilter)
    throws UriParserException, UriValidationException {

    if (lastTokenKind == TokenKind.QualifiedName) {
      // Type cast to an entity type or complex type or bound function
      final FullQualifiedName fullQualifiedName = new FullQualifiedName(this.tokenizer.getText());
      final EdmEntityType edmEntityType = this.edm.getEntityType(fullQualifiedName);

      if (edmEntityType != null) {
        if (allowTypeFilter) {
          setTypeFilter(lastResource, edmEntityType);

          if (this.tokenizer.next(TokenKind.SLASH)) {
            if (this.tokenizer.next(TokenKind.QualifiedName)) {
              parseBoundFunction(fullQualifiedName, uriInfo, lastResource);
            } else if (this.tokenizer.next(TokenKind.ODataIdentifier)) {
              parsePropertyPathExpr(uriInfo, lastResource);
            } else {
              throw new UriParserSyntaxException(
                "Expected OData Identifier or Full Qualified Name.",
                UriParserSyntaxException.MessageKeys.SYNTAX);
            }
          }
        } else {
          throw new UriParserSemanticException("Type filters are not chainable.",
            UriParserSemanticException.MessageKeys.TYPE_FILTER_NOT_CHAINABLE,
            lastResource.getType().getFullQualifiedName().getFullQualifiedNameAsString(),
            fullQualifiedName.getFullQualifiedNameAsString());
        }
      } else if (this.edm.getComplexType(fullQualifiedName) != null) {
        if (allowTypeFilter) {
          setTypeFilter(lastResource, this.edm.getComplexType(fullQualifiedName));

          if (this.tokenizer.next(TokenKind.SLASH)) {
            if (this.tokenizer.next(TokenKind.QualifiedName)) {
              parseBoundFunction(fullQualifiedName, uriInfo, lastResource);
            } else if (this.tokenizer.next(TokenKind.ODataIdentifier)) {
              parsePropertyPathExpr(uriInfo, lastResource);
            } else {
              throw new UriParserSyntaxException(
                "Expected OData Identifier or Full Qualified Name.",
                UriParserSyntaxException.MessageKeys.SYNTAX);
            }
          }
        } else {
          throw new UriParserSemanticException("Type filters are not chainable.",
            UriParserSemanticException.MessageKeys.TYPE_FILTER_NOT_CHAINABLE,
            lastResource.getType().getFullQualifiedName().getFullQualifiedNameAsString(),
            fullQualifiedName.getFullQualifiedNameAsString());
        }
      } else {
        parseBoundFunction(fullQualifiedName, uriInfo, lastResource);
      }
    } else if (lastTokenKind == TokenKind.ODataIdentifier) {
      parsePropertyPathExpr(uriInfo, lastResource);
    } else {
      throw new UriParserSyntaxException("Unexpected token.",
        UriParserSyntaxException.MessageKeys.SYNTAX);
    }
  }

  private Expression parseMethod(final TokenKind nextMethod)
    throws UriParserException, UriValidationException {
    // The method token text includes the opening parenthesis so that method
    // calls can be recognized unambiguously.
    // OData identifiers have to be considered after that.
    final MethodKind methodKind = tokenToMethod.get(nextMethod);
    return new MethodImpl(methodKind, parseMethodParameters(methodKind));
  }

  private List<Expression> parseMethodParameters(final MethodKind methodKind)
    throws UriParserException, UriValidationException {
    final List<Expression> parameters = new ArrayList<>();
    switch (methodKind) {
      // Must have no parameter.
      case NOW:
      case MAXDATETIME:
      case MINDATETIME:
        ParserHelper.bws(this.tokenizer);
      break;

      // Must have one parameter.
      case LENGTH:
      case TOLOWER:
      case TOUPPER:
      case TRIM:
        ParserHelper.bws(this.tokenizer);
        final Expression stringParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(stringParameter, EdmPrimitiveTypeKind.String);
        checkNoCollection(stringParameter);
        parameters.add(stringParameter);
      break;
      case YEAR:
      case MONTH:
      case DAY:
        ParserHelper.bws(this.tokenizer);
        final Expression dateParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(dateParameter, EdmPrimitiveTypeKind.Date, EdmPrimitiveTypeKind.DateTimeOffset);
        checkNoCollection(dateParameter);
        parameters.add(dateParameter);
      break;
      case HOUR:
      case MINUTE:
      case SECOND:
      case FRACTIONALSECONDS:
        ParserHelper.bws(this.tokenizer);
        final Expression timeParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(timeParameter, EdmPrimitiveTypeKind.TimeOfDay,
          EdmPrimitiveTypeKind.DateTimeOffset);
        checkNoCollection(timeParameter);
        parameters.add(timeParameter);
      break;
      case DATE:
      case TIME:
      case TOTALOFFSETMINUTES:
        ParserHelper.bws(this.tokenizer);
        final Expression dateTimeParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(dateTimeParameter, EdmPrimitiveTypeKind.DateTimeOffset);
        checkNoCollection(dateTimeParameter);
        parameters.add(dateTimeParameter);
      break;
      case TOTALSECONDS:
        ParserHelper.bws(this.tokenizer);
        final Expression durationParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(durationParameter, EdmPrimitiveTypeKind.Duration);
        checkNoCollection(durationParameter);
        parameters.add(durationParameter);
      break;
      case ROUND:
      case FLOOR:
      case CEILING:
        ParserHelper.bws(this.tokenizer);
        final Expression decimalParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(decimalParameter, EdmPrimitiveTypeKind.Decimal, EdmPrimitiveTypeKind.Single,
          EdmPrimitiveTypeKind.Double);
        checkNoCollection(decimalParameter);
        parameters.add(decimalParameter);
      break;
      case GEOLENGTH:
        ParserHelper.bws(this.tokenizer);
        final Expression geoParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(geoParameter, EdmPrimitiveTypeKind.GeographyLineString,
          EdmPrimitiveTypeKind.GeometryLineString);
        checkNoCollection(geoParameter);
        parameters.add(geoParameter);
      break;

      // Must have two parameters.
      case CONTAINS:
      case ENDSWITH:
      case STARTSWITH:
      case INDEXOF:
      case CONCAT:
      case SUBSTRINGOF:
        ParserHelper.bws(this.tokenizer);
        final Expression stringParameter1 = parseExpression();
        checkType(stringParameter1, EdmPrimitiveTypeKind.String);
        checkNoCollection(stringParameter1);
        parameters.add(stringParameter1);
        ParserHelper.bws(this.tokenizer);
        ParserHelper.requireNext(this.tokenizer, TokenKind.COMMA);
        ParserHelper.bws(this.tokenizer);
        final Expression stringParameter2 = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(stringParameter2, EdmPrimitiveTypeKind.String);
        checkNoCollection(stringParameter2);
        parameters.add(stringParameter2);
      break;
      case GEODISTANCE:
        ParserHelper.bws(this.tokenizer);
        final Expression geoParameter1 = parseExpression();
        checkType(geoParameter1, EdmPrimitiveTypeKind.GeographyPoint,
          EdmPrimitiveTypeKind.GeometryPoint);
        checkNoCollection(geoParameter1);
        parameters.add(geoParameter1);
        ParserHelper.bws(this.tokenizer);
        ParserHelper.requireNext(this.tokenizer, TokenKind.COMMA);
        ParserHelper.bws(this.tokenizer);
        final Expression geoParameter2 = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkType(geoParameter2, EdmPrimitiveTypeKind.GeographyPoint,
          EdmPrimitiveTypeKind.GeometryPoint);
        checkNoCollection(geoParameter2);
        parameters.add(geoParameter2);
      break;
      case GEOINTERSECTS:
        ParserHelper.bws(this.tokenizer);
        final Expression geoPointParameter = parseExpression();
        // checkType(geoPointParameter,
        // EdmPrimitiveTypeKind.GeographyPoint,
        // EdmPrimitiveTypeKind.GeometryPoint);
        checkNoCollection(geoPointParameter);
        parameters.add(geoPointParameter);
        ParserHelper.bws(this.tokenizer);
        ParserHelper.requireNext(this.tokenizer, TokenKind.COMMA);
        ParserHelper.bws(this.tokenizer);
        final Expression geoPolygonParameter = parseExpression();
        ParserHelper.bws(this.tokenizer);
        // checkType(geoPolygonParameter, EdmPrimitiveTypeKind.GeographyPolygon,
        // EdmPrimitiveTypeKind.GeometryPolygon);
        checkNoCollection(geoPolygonParameter);
        parameters.add(geoPolygonParameter);
      break;

      // Can have two or three parameters.
      case SUBSTRING:
        ParserHelper.bws(this.tokenizer);
        final Expression parameterFirst = parseExpression();
        checkType(parameterFirst, EdmPrimitiveTypeKind.String);
        checkNoCollection(parameterFirst);
        parameters.add(parameterFirst);
        ParserHelper.bws(this.tokenizer);
        ParserHelper.requireNext(this.tokenizer, TokenKind.COMMA);
        ParserHelper.bws(this.tokenizer);
        final Expression parameterSecond = parseExpression();
        ParserHelper.bws(this.tokenizer);
        checkIntegerType(parameterSecond);
        parameters.add(parameterSecond);
        ParserHelper.bws(this.tokenizer);
        if (this.tokenizer.next(TokenKind.COMMA)) {
          ParserHelper.bws(this.tokenizer);
          final Expression parameterThird = parseExpression();
          ParserHelper.bws(this.tokenizer);
          checkIntegerType(parameterThird);
          parameters.add(parameterThird);
        }
      break;

      // Can have one or two parameters. These methods are handled elsewhere.
      case CAST:
      case ISOF:
      break;

      case COMPUTE_AGGREGATE:
        final ApplyParser ap = new ApplyParser(this.edm);
        final AggregateExpression aggrExpr = ap.parseAggregateMethodCallExpr(this.tokenizer,
          (EdmStructuredType)this.referringType);
        parameters.add(aggrExpr);

    }
    ParserHelper.requireNext(this.tokenizer, TokenKind.CLOSE);

    return parameters;
  }

  private Expression parsePrimitive(final TokenKind primitiveTokenKind) throws UriParserException {
    final String primitiveValueLiteral = this.tokenizer.getText();
    if (primitiveTokenKind == TokenKind.EnumValue) {
      return createEnumExpression(primitiveValueLiteral);
    } else {
      EdmPrimitiveTypeKind primitiveTypeKind = ParserHelper.tokenToPrimitiveType
        .get(primitiveTokenKind);
      if (primitiveTypeKind == EdmPrimitiveTypeKind.Int64) {
        primitiveTypeKind = determineIntegerType(primitiveValueLiteral);
      }
      final EdmPrimitiveTypeKind kind = primitiveTypeKind;

      final EdmPrimitiveType type = primitiveTypeKind == null ?
      // Null handling
        null : kind.getInstance();
      return new LiteralImpl(primitiveValueLiteral, type);
    }
  }

  private void parsePropertyPathExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {

    final String oDataIdentifier = this.tokenizer.getText();

    final EdmType lastType = lastResource == null ? this.referringType
      : ParserHelper.getTypeInformation(lastResource);
    if (!(lastType instanceof EdmStructuredType)) {
      throw new UriParserSemanticException("Property paths must follow a structured type.",
        UriParserSemanticException.MessageKeys.ONLY_FOR_STRUCTURAL_TYPES, oDataIdentifier);
    }

    final EdmStructuredType structuredType = (EdmStructuredType)lastType;
    final EdmElement property = structuredType.getProperty(oDataIdentifier);

    if (property == null) {
      throw new UriParserSemanticException("Unknown property: " + oDataIdentifier,
        UriParserSemanticException.MessageKeys.EXPRESSION_PROPERTY_NOT_IN_TYPE,
        lastType.getFullQualifiedName().getFullQualifiedNameAsString(), oDataIdentifier);
    }

    if (property.getType() instanceof EdmComplexType) {
      final UriResourceComplexPropertyImpl complexResource = new UriResourceComplexPropertyImpl(
        (EdmProperty)property);
      uriInfo.addResourcePart(complexResource);

      if (property.isCollection()) {
        if (this.tokenizer.next(TokenKind.SLASH)) {
          parseCollectionPathExpr(uriInfo, complexResource);
        }
      } else {
        parseComplexPathExpr(uriInfo, complexResource);
      }
    } else if (property instanceof EdmNavigationProperty) {
      // Nav. property; maybe a collection
      final UriResourceNavigationPropertyImpl navigationResource = new UriResourceNavigationPropertyImpl(
        (EdmNavigationProperty)property);
      navigationResource.setKeyPredicates(ParserHelper.parseNavigationKeyPredicate(this.tokenizer,
        (EdmNavigationProperty)property, this.edm, this.referringType, this.aliases));
      uriInfo.addResourcePart(navigationResource);

      if (navigationResource.isCollection()) {
        parseCollectionNavigationExpr(uriInfo, navigationResource);
      } else {
        parseSingleNavigationExpr(uriInfo, navigationResource);
      }
    } else {
      // Primitive type or Enum type
      final UriResourcePrimitivePropertyImpl primitiveResource = new UriResourcePrimitivePropertyImpl(
        (EdmProperty)property);
      uriInfo.addResourcePart(primitiveResource);

      if (property.isCollection()) {
        if (this.tokenizer.next(TokenKind.SLASH)) {
          parseCollectionPathExpr(uriInfo, primitiveResource);
        }
      } else {
        parseSinglePathExpr(uriInfo, primitiveResource);
      }
    }
  }

  private void parseSingleNavigationExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {
    if (this.tokenizer.next(TokenKind.SLASH)) {
      final TokenKind tokenKind = ParserHelper.next(this.tokenizer, TokenKind.QualifiedName,
        TokenKind.ODataIdentifier);
      parseMemberExpression(tokenKind, uriInfo, lastResource, true);
    }
  }

  private void parseSinglePathExpr(final UriInfoImpl uriInfo,
    final UriResourcePartTyped lastResource) throws UriParserException, UriValidationException {
    if (this.tokenizer.next(TokenKind.SLASH)) {
      ParserHelper.requireNext(this.tokenizer, TokenKind.QualifiedName);
      parseBoundFunction(new FullQualifiedName(this.tokenizer.getText()), uriInfo, lastResource);
    }
  }

  private void setTypeFilter(final UriResourcePartTyped lastResource,
    final EdmStructuredType entityTypeFilter) throws UriParserException {
    checkStructuredTypeFilter(lastResource.getType(), entityTypeFilter);
    if (lastResource instanceof UriResourceTypedImpl) {
      ((UriResourceTypedImpl)lastResource).setTypeFilter(entityTypeFilter);
    } else if (lastResource instanceof UriResourceWithKeysImpl) {
      ((UriResourceWithKeysImpl)lastResource).setEntryTypeFilter(entityTypeFilter);
    }
  }

  @Override
  public String toString() {
    return this.tokenizer.toString();
  }
}
