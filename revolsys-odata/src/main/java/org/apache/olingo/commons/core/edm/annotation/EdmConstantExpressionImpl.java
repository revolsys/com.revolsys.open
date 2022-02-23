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
package org.apache.olingo.commons.core.edm.annotation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.annotation.EdmConstantExpression;
import org.apache.olingo.commons.api.edm.geo.Geospatial;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression;
import org.apache.olingo.commons.core.edm.Edm;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;

public class EdmConstantExpressionImpl extends AbstractEdmExpression
  implements EdmConstantExpression {

  private EdmPrimitiveType type;

  private final CsdlConstantExpression csdlExp;

  private boolean built = false;

  private Object primitive;

  private String enumTypeName;

  private List<String> enumMembers;

  private Geospatial geospatial;

  public EdmConstantExpressionImpl(final Edm edm, final CsdlConstantExpression constExprConstruct) {
    super(edm, constExprConstruct.getType().toString());
    this.csdlExp = constExprConstruct;
  }

  @Override
  public List<String> asEnumMembers() {
    if (!this.built) {
      build();
    }
    return this.enumMembers;
  }

  @Override
  public Geospatial asGeospatial() {
    if (!this.built) {
      build();
    }
    return this.geospatial;
  }

  @Override
  public Object asPrimitive() {
    if (!this.built) {
      build();
    }
    return this.primitive;
  }

  private void build() {
    if (this.csdlExp.getType() == CsdlConstantExpression.ConstantExpressionType.EnumMember) {
      if (this.csdlExp.getValue() == null) {
        throw new EdmException("Expression value must not be null");
      }
      final List<String> localEnumValues = new ArrayList<>();
      for (final String split : this.csdlExp.getValue().split(" ")) {
        final String[] enumSplit = split.split("/");
        if (enumSplit.length != 2) {
          throw new EdmException("Enum expression value must consist of enumTypeName/EnumMember.");
        }
        this.enumTypeName = enumSplit[0];
        localEnumValues.add(enumSplit[1]);
      }
      this.enumMembers = Collections.unmodifiableList(localEnumValues);
    } else {
      EdmPrimitiveTypeKind kind;
      switch (this.csdlExp.getType()) {
        case Binary:
          kind = EdmPrimitiveTypeKind.Binary;
        break;
        case Bool:
          kind = EdmPrimitiveTypeKind.Boolean;
        break;
        case Date:
          kind = EdmPrimitiveTypeKind.Date;
        break;
        case DateTimeOffset:
          kind = EdmPrimitiveTypeKind.DateTimeOffset;
        break;
        case Decimal:
          kind = EdmPrimitiveTypeKind.Decimal;
        break;
        case Duration:
          kind = EdmPrimitiveTypeKind.Duration;
        break;
        case Float:
          kind = EdmPrimitiveTypeKind.Single;
        break;
        case Guid:
          kind = EdmPrimitiveTypeKind.Guid;
        break;
        case Int:
          kind = EdmPrimitiveTypeKind.Int32;
        break;
        case TimeOfDay:
          kind = EdmPrimitiveTypeKind.TimeOfDay;
        break;
        case String:
        default:
          kind = EdmPrimitiveTypeKind.String;
      }
      this.type = EdmPrimitiveTypeFactory.getInstance(kind);
      try {
        this.primitive = this.type.valueOfString(this.csdlExp.getValue(), null, null, null, null,
          null, this.type.getDefaultType());
      } catch (final EdmPrimitiveTypeException e) {
        throw new IllegalArgumentException(e);
      }
    }
    this.built = true;
  }

  @Override
  public String getEnumTypeName() {
    if (!this.built) {
      build();
    }
    return this.enumTypeName;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    switch (this.csdlExp.getType()) {
      case Binary:
        return EdmExpressionType.Binary;
      case Bool:
        return EdmExpressionType.Bool;
      case Date:
        return EdmExpressionType.Date;
      case DateTimeOffset:
        return EdmExpressionType.DateTimeOffset;
      case Decimal:
        return EdmExpressionType.Decimal;
      case Duration:
        return EdmExpressionType.Duration;
      case EnumMember:
        return EdmExpressionType.EnumMember;
      case Float:
        return EdmExpressionType.Float;
      case Guid:
        return EdmExpressionType.Guid;
      case Int:
        return EdmExpressionType.Int;
      case String:
        return EdmExpressionType.String;
      case TimeOfDay:
        return EdmExpressionType.TimeOfDay;
      default:
        throw new EdmException(
          "Invalid Expressiontype for constant expression: " + this.csdlExp.getType());
    }
  }

  @Override
  public String getValueAsString() {
    return this.csdlExp.getValue();
  }
}
