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

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmOnDelete;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.provider.CsdlNavigationProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;
import org.apache.olingo.commons.api.edm.provider.CsdlReferentialConstraint;

public class EdmNavigationPropertyImpl extends AbstractEdmNamed implements EdmNavigationProperty {

  private final CsdlNavigationProperty navigationProperty;

  private List<EdmReferentialConstraint> referentialConstraints;

  private EdmEntityType typeImpl;

  private EdmNavigationProperty partnerNavigationProperty;

  public EdmNavigationPropertyImpl(final Edm edm, final CsdlNavigationProperty navigationProperty) {
    super(edm, navigationProperty.getName(), navigationProperty);
    this.navigationProperty = navigationProperty;
  }

  @Override
  public boolean containsTarget() {
    return this.navigationProperty.isContainsTarget();
  }

  @Override
  public EdmOnDelete getOnDelete() {
    final CsdlOnDelete csdlOnDelete = this.navigationProperty.getOnDelete();
    return csdlOnDelete != null ? new EdmOnDeleteImpl(this.edm, csdlOnDelete) : null;
  }

  @Override
  public EdmNavigationProperty getPartner() {
    if (this.partnerNavigationProperty == null) {
      final String partner = this.navigationProperty.getPartner();
      if (partner != null) {
        EdmStructuredType type = getType();
        EdmNavigationProperty property = null;
        final String[] split = partner.split("/");
        for (final String element : split) {
          property = type.getNavigationProperty(element);
          if (property == null) {
            throw new EdmException("Cannot find navigation property with name: " + element
              + " at type " + type.getName());
          }
          type = property.getType();
        }
        this.partnerNavigationProperty = property;
      }
    }
    return this.partnerNavigationProperty;
  }

  @Override
  public String getReferencingPropertyName(final String referencedPropertyName) {
    final List<CsdlReferentialConstraint> refConstraints = this.navigationProperty
      .getReferentialConstraints();
    if (refConstraints != null) {
      for (final CsdlReferentialConstraint constraint : refConstraints) {
        if (constraint.getReferencedProperty().equals(referencedPropertyName)) {
          return constraint.getProperty();
        }
      }
    }
    return null;
  }

  @Override
  public List<EdmReferentialConstraint> getReferentialConstraints() {
    if (this.referentialConstraints == null) {
      final List<CsdlReferentialConstraint> providerConstraints = this.navigationProperty
        .getReferentialConstraints();
      final List<EdmReferentialConstraint> referentialConstraintsLocal = new ArrayList<>();
      if (providerConstraints != null) {
        for (final CsdlReferentialConstraint constraint : providerConstraints) {
          referentialConstraintsLocal.add(new EdmReferentialConstraintImpl(this.edm, constraint));
        }
      }

      this.referentialConstraints = Collections.unmodifiableList(referentialConstraintsLocal);
    }
    return this.referentialConstraints;
  }

  @Override
  public EdmEntityType getType() {
    if (this.typeImpl == null) {
      this.typeImpl = this.edm.getEntityType(this.navigationProperty.getTypeFQN());
      if (this.typeImpl == null) {
        throw new EdmException(
          "Cannot find type with name: " + this.navigationProperty.getTypeFQN());
      }
    }
    return this.typeImpl;
  }

  @Override
  public boolean isCollection() {
    return this.navigationProperty.isCollection();
  }

  @Override
  public boolean isNullable() {
    return this.navigationProperty.isNullable();
  }
}
