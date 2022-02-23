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
package org.apache.olingo.commons.core.edm;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmEnumType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.EdmTypeDefinition;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;

public class EdmTypeInfo {

  public static class Builder {

    private String typeExpression;

    private Edm edm;

    private boolean includeAnnotations;

    public EdmTypeInfo build() {
      return new EdmTypeInfo(this.edm, this.typeExpression, this.includeAnnotations);
    }

    public Builder setEdm(final Edm edm) {
      this.edm = edm;
      return this;
    }

    public Builder setIncludeAnnotations(final boolean includeAnnotations) {
      this.includeAnnotations = includeAnnotations;
      return this;
    }

    public Builder setTypeExpression(final String typeExpression) {
      this.typeExpression = typeExpression;
      return this;
    }
  }

  public static EdmPrimitiveTypeKind determineTypeKind(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return EdmPrimitiveTypeKind.Boolean;
    } else if (value instanceof String) {
      return EdmPrimitiveTypeKind.String;
    } else if (value instanceof UUID) {
      return EdmPrimitiveTypeKind.Guid;
    } else if (value instanceof Long || value instanceof BigInteger) {
      return EdmPrimitiveTypeKind.Int64;
    } else if (value instanceof Integer) {
      return EdmPrimitiveTypeKind.Int32;
    } else if (value instanceof Short) {
      return EdmPrimitiveTypeKind.Int16;
    } else if (value instanceof Byte) {
      return EdmPrimitiveTypeKind.SByte;
    } else if (value instanceof BigDecimal) {
      return EdmPrimitiveTypeKind.Decimal;
    } else if (value instanceof Double) {
      return EdmPrimitiveTypeKind.Double;
    } else if (value instanceof Float) {
      return EdmPrimitiveTypeKind.Single;
    } else if (value instanceof Calendar || value instanceof Date
      || value instanceof java.sql.Timestamp || value instanceof java.time.Instant
      || value instanceof java.time.ZonedDateTime) {
      return EdmPrimitiveTypeKind.DateTimeOffset;
    } else if (value instanceof java.sql.Date || value instanceof java.time.LocalDate) {
      return EdmPrimitiveTypeKind.Date;
    } else if (value instanceof java.sql.Time || value instanceof java.time.LocalTime) {
      return EdmPrimitiveTypeKind.TimeOfDay;
    } else if (value instanceof byte[] || value instanceof Byte[]) {
      return EdmPrimitiveTypeKind.Binary;
    }
    return null;
  }

  private final boolean collection;

  private final FullQualifiedName fullQualifiedName;

  private final EdmPrimitiveTypeKind primitiveType;

  private EdmTypeDefinition typeDefinition;

  private EdmEnumType enumType;

  private EdmComplexType complexType;

  private EdmEntityType entityType;

  private EdmTypeInfo(final Edm edm, final String typeExpression,
    final boolean includeAnnotations) {
    String baseType;
    final int collStartIdx = typeExpression.indexOf("Collection(");
    final int collEndIdx = typeExpression.lastIndexOf(')');
    if (collStartIdx == -1) {
      baseType = typeExpression;
      this.collection = false;
    } else {
      if (collEndIdx == -1) {
        throw new IllegalArgumentException("Malformed type: " + typeExpression);
      }

      this.collection = true;
      baseType = typeExpression.substring(collStartIdx + 11, collEndIdx);
    }

    if (baseType.startsWith("#")) {
      baseType = baseType.substring(1);
    }

    String typeName;
    String namespace;

    final int lastDotIdx = baseType.lastIndexOf('.');
    if (lastDotIdx == -1) {
      namespace = EdmPrimitiveType.EDM_NAMESPACE;
      typeName = baseType;
    } else {
      namespace = baseType.substring(0, lastDotIdx);
      typeName = baseType.substring(lastDotIdx + 1);
    }

    if (typeName == null || typeName.isEmpty()) {
      throw new IllegalArgumentException("Null or empty type name in " + typeExpression);
    }

    this.fullQualifiedName = new FullQualifiedName(namespace, typeName);

    this.primitiveType = EdmPrimitiveTypeKind.getByName(typeName);

    if (this.primitiveType == null && edm != null) {
      this.typeDefinition = edm.getTypeDefinition(this.fullQualifiedName);
      if (this.typeDefinition == null) {
        this.enumType = edm.getEnumType(this.fullQualifiedName);
        if (this.enumType == null) {
          if (includeAnnotations) {
            this.complexType = edm.getComplexTypeWithAnnotations(this.fullQualifiedName, true);
          } else {
            this.complexType = edm.getComplexType(this.fullQualifiedName);
          }
          if (this.complexType == null) {
            this.entityType = edm.getEntityType(this.fullQualifiedName);
          }
        }
      }
    }
  }

  public String external() {
    return serialize(true);
  }

  public EdmComplexType getComplexType() {
    return this.complexType;
  }

  public EdmEntityType getEntityType() {
    return this.entityType;
  }

  public EdmEnumType getEnumType() {
    return this.enumType;
  }

  public FullQualifiedName getFullQualifiedName() {
    return this.fullQualifiedName;
  }

  public EdmPrimitiveTypeKind getPrimitiveTypeKind() {
    return this.primitiveType;
  }

  public EdmType getType() {
    return isPrimitiveType() ? EdmPrimitiveTypeFactory.getInstance(getPrimitiveTypeKind())
      : isTypeDefinition() ? getTypeDefinition()
        : isEnumType() ? getEnumType()
          : isComplexType() ? getComplexType() : isEntityType() ? getEntityType() : null;
  }

  public EdmTypeDefinition getTypeDefinition() {
    return this.typeDefinition;
  }

  public String internal() {
    return serialize(false);
  }

  public boolean isCollection() {
    return this.collection;
  }

  public boolean isComplexType() {
    return this.complexType != null;
  }

  public boolean isEntityType() {
    return this.entityType != null;
  }

  public boolean isEnumType() {
    return this.enumType != null;
  }

  public boolean isPrimitiveType() {
    return this.primitiveType != null;
  }

  public boolean isTypeDefinition() {
    return this.typeDefinition != null;
  }

  private String serialize(final boolean external) {
    final StringBuilder serialize = new StringBuilder();

    if (external && (!isPrimitiveType() || isCollection())) {
      serialize.append('#');
    }

    if (isCollection()) {
      serialize.append("Collection(");
    }

    serialize.append(external && isPrimitiveType() ? getFullQualifiedName().getName()
      : getFullQualifiedName().getFullQualifiedNameAsString());

    if (isCollection()) {
      serialize.append(')');
    }

    return serialize.toString();
  }
}
