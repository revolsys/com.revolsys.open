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

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmMapping;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;

public class EdmParameterImpl extends AbstractEdmNamed implements EdmParameter {

  private final CsdlParameter parameter;

  private EdmType typeImpl;

  public EdmParameterImpl(final Edm edm, final CsdlParameter parameter) {
    super(edm, parameter.getName(), parameter);
    this.parameter = parameter;
  }

  @Override
  public EdmMapping getMapping() {
    return this.parameter.getMapping();
  }

  @Override
  public Integer getMaxLength() {
    return this.parameter.getMaxLength();
  }

  @Override
  public Integer getPrecision() {
    return this.parameter.getPrecision();
  }

  @Override
  public Integer getScale() {
    return this.parameter.getScale();
  }

  @Override
  public SRID getSrid() {
    return this.parameter.getSrid();
  }

  @Override
  public EdmType getType() {
    if (this.typeImpl == null) {
      if (this.parameter.getType() == null) {
        throw new EdmException(
          "Parameter " + this.parameter.getName() + " must hava a full qualified type.");
      }
      this.typeImpl = new EdmTypeInfo.Builder().setEdm(this.edm)
        .setTypeExpression(this.parameter.getType())
        .build()
        .getType();
      if (this.typeImpl == null) {
        throw new EdmException("Cannot find type with name: " + this.parameter.getTypeFQN());
      }
    }

    return this.typeImpl;
  }

  @Override
  public boolean isCollection() {
    return this.parameter.isCollection();
  }

  @Override
  public boolean isNullable() {
    return this.parameter.isNullable();
  }
}
