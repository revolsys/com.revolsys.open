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

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.annotation.EdmIsOf;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.annotation.CsdlIsOf;
import org.apache.olingo.commons.core.edm.Edm;
import org.apache.olingo.commons.core.edm.EdmTypeInfo;

public class EdmIsOfImpl extends AbstractEdmAnnotatableDynamicExpression implements EdmIsOf {

  private final Edm edm;

  private final CsdlIsOf isOf;

  private EdmExpression value;

  private EdmType type;

  public EdmIsOfImpl(final Edm edm, final CsdlIsOf isOf) {
    super(edm, "IsOf", isOf);
    this.edm = edm;
    this.isOf = isOf;
  }

  @Override
  public EdmExpressionType getExpressionType() {
    return EdmExpressionType.IsOf;
  }

  @Override
  public Integer getMaxLength() {
    return this.isOf.getMaxLength();
  }

  @Override
  public Integer getPrecision() {
    return this.isOf.getPrecision();
  }

  @Override
  public Integer getScale() {
    return this.isOf.getScale();
  }

  @Override
  public SRID getSrid() {
    return this.isOf.getSrid();
  }

  @Override
  public EdmType getType() {
    if (this.type == null) {
      if (this.isOf.getType() == null) {
        throw new EdmException("Must specify a type for an IsOf expression.");
      }
      final EdmTypeInfo typeInfo = new EdmTypeInfo.Builder().setEdm(this.edm)
        .setTypeExpression(this.isOf.getType())
        .build();
      this.type = typeInfo.getType();
    }
    return this.type;
  }

  @Override
  public EdmExpression getValue() {
    if (this.value == null) {
      if (this.isOf.getValue() == null) {
        throw new EdmException("IsOf expressions require an expression value.");
      }
      this.value = getExpression(this.edm, this.isOf.getValue());
    }
    return this.value;
  }
}
