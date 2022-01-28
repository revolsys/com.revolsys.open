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
package org.apache.olingo.commons.api.edm.provider.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

/**
 * The edm:IsOf expression evaluates a child expression and returns a Boolean value indicating whether
 * the child expression returns the specified type
 */
public class CsdlIsOf extends CsdlDynamicExpression implements CsdlAnnotatable {

  private String type;

  private Integer maxLength;

  private Integer precision;

  private Integer scale;

  private SRID srid;

  private CsdlExpression value;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> csdlIsOfannot) {
    if (csdlIsOfannot == null) {
      return false;
    }
    if (this.getAnnotations().size() == csdlIsOfannot.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(csdlIsOfannot.get(i))) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null || !(obj instanceof CsdlIsOf)) {
      return false;
    }
    final CsdlIsOf csdlIsOf = (CsdlIsOf)obj;
    return (this.getType() == null ? csdlIsOf.getType() == null
      : this.getType().equals(csdlIsOf.getType()))
      && (this.getMaxLength() == null ? csdlIsOf.getMaxLength() == null
        : this.getMaxLength().equals(csdlIsOf.getMaxLength()))
      && (this.getPrecision() == null ? csdlIsOf.getPrecision() == null
        : this.getPrecision().equals(csdlIsOf.getPrecision()))
      && (this.getScale() == null ? csdlIsOf.getScale() == null
        : this.getScale().equals(csdlIsOf.getScale()))
      && (this.getSrid() == null ? csdlIsOf.getSrid() == null
        : this.getSrid().equals(csdlIsOf.getSrid()))
      && (this.getValue() == null ? csdlIsOf.getValue() == null
        : this.getValue().equals(csdlIsOf.getValue()))
      && (this.getAnnotations() == null ? csdlIsOf.getAnnotations() == null
        : checkAnnotations(csdlIsOf.getAnnotations()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Facet MaxLength
   * @return fact MaxLength
   */
  public Integer getMaxLength() {
    return this.maxLength;
  }

  /**
   * Facet Precision
   * @return fact Precision
   */
  public Integer getPrecision() {
    return this.precision;
  }

  /**
   * Facet Scale
   * @return facet Scale
   */
  public Integer getScale() {
    return this.scale;
  }

  /**
   * Facet SRID
   * @return facet SRID
   */
  public SRID getSrid() {
    return this.srid;
  }

  /**
   * The type which is checked again the child expression
   * @return EdmType type
   */
  public String getType() {
    return this.type;
  }

  /**
   * Returns the child expression
   * @return Returns the child expression
   */
  public CsdlExpression getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.type == null ? 0 : this.type.hashCode());
    result = prime * result + (this.maxLength == null ? 0 : this.maxLength.hashCode());
    result = prime * result + (this.precision == null ? 0 : this.precision.hashCode());
    result = prime * result + (this.scale == null ? 0 : this.scale.hashCode());
    result = prime * result + (this.srid == null ? 0 : this.srid.hashCode());
    result = prime * result + (this.value == null ? 0 : this.value.hashCode());
    result = prime * result + (this.annotations == null ? 0 : this.annotations.hashCode());
    return result;
  }

  public CsdlIsOf setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlIsOf setMaxLength(final Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  public CsdlIsOf setPrecision(final Integer precision) {
    this.precision = precision;
    return this;
  }

  public CsdlIsOf setScale(final Integer scale) {
    this.scale = scale;
    return this;
  }

  public CsdlIsOf setSrid(final SRID srid) {
    this.srid = srid;
    return this;
  }

  public CsdlIsOf setType(final String type) {
    this.type = type;
    return this;
  }

  public CsdlIsOf setValue(final CsdlExpression value) {
    this.value = value;
    return this;
  }
}
