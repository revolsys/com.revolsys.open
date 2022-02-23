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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.TargetType;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlTerm;

public class EdmTermImpl extends AbstractEdmNamed implements EdmTerm {

  private final CsdlTerm term;

  private final FullQualifiedName fqn;

  private EdmType termType;

  private EdmTerm baseTerm;

  private List<TargetType> appliesTo;

  public EdmTermImpl(final Edm edm, final String namespace, final CsdlTerm term) {
    super(edm, term.getName(), term);
    this.term = term;
    this.fqn = new FullQualifiedName(namespace, term.getName());
  }

  @Override
  public List<TargetType> getAppliesTo() {
    if (this.appliesTo == null) {
      final ArrayList<TargetType> localAppliesTo = new ArrayList<>();
      for (final String apply : this.term.getAppliesTo()) {
        try {
          localAppliesTo.add(TargetType.valueOf(apply));
        } catch (final IllegalArgumentException e) {
          throw new EdmException("Invalid AppliesTo value: " + apply, e);
        }
      }
      this.appliesTo = Collections.unmodifiableList(localAppliesTo);
    }
    return this.appliesTo;
  }

  @Override
  public EdmTerm getBaseTerm() {
    if (this.baseTerm == null && this.term.getBaseTerm() != null) {
      this.baseTerm = this.edm.getTerm(new FullQualifiedName(this.term.getBaseTerm()));
    }
    return this.baseTerm;
  }

  @Override
  public String getDefaultValue() {
    return this.term.getDefaultValue();
  }

  @Override
  public FullQualifiedName getFullQualifiedName() {
    return this.fqn;
  }

  @Override
  public Integer getMaxLength() {
    return this.term.getMaxLength();
  }

  @Override
  public Integer getPrecision() {
    return this.term.getPrecision();
  }

  @Override
  public Integer getScale() {
    return this.term.getScale();
  }

  @Override
  public SRID getSrid() {
    return this.term.getSrid();
  }

  @Override
  public EdmType getType() {
    if (this.termType == null) {
      if (this.term.getType() == null) {
        throw new EdmException("Terms must hava a full qualified type.");
      }
      this.termType = new EdmTypeInfo.Builder().setEdm(this.edm)
        .setTypeExpression(this.term.getType())
        .build()
        .getType();
      if (this.termType == null) {
        throw new EdmException("Cannot find type with name: " + this.term.getType());
      }
    }
    return this.termType;
  }

  @Override
  public boolean isNullable() {
    return this.term.isNullable();
  }
}
