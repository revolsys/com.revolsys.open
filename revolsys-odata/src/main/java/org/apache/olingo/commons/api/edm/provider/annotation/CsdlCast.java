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
 * Represents an edm:Cast expression.
 * Casts the value obtained from its single child expression to the specified type
 */
public class CsdlCast extends CsdlDynamicExpression implements CsdlAnnotatable {

  private String type;

  private Integer maxLength;

  private Integer precision;

  private Integer scale;

  private SRID srid;

  private CsdlExpression value;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> csdlCastAnnotations) {
    if (csdlCastAnnotations == null) {
      return false;
    }
    if (this.getAnnotations().size() == csdlCastAnnotations.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(csdlCastAnnotations.get(i))) {
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
    if (obj == null || !(obj instanceof CsdlCast)) {
      return false;
    }
    final CsdlCast csdlCast = (CsdlCast)obj;
    return (this.getValue() == null ? csdlCast.getValue() == null
      : this.getValue().equals(csdlCast.getValue()))
      && (this.getType() == null ? csdlCast.getType() == null
        : this.getType().equals(csdlCast.getType()))
      && (this.getMaxLength() == null ? csdlCast.getMaxLength() == null
        : this.getMaxLength().equals(csdlCast.getMaxLength()))
      && (this.getPrecision() == null ? csdlCast.getPrecision() == null
        : this.getPrecision().equals(csdlCast.getPrecision()))
      && (this.getScale() == null ? csdlCast.getScale() == null
        : this.getScale().equals(csdlCast.getScale()))
      && (this.getSrid() == null ? csdlCast.getSrid() == null
        : String.valueOf(this.getSrid()).equals(String.valueOf(csdlCast.getSrid())))
      && (this.getAnnotations() == null ? csdlCast.getAnnotations() == null
        : checkAnnotations(csdlCast.getAnnotations()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Returns the facet attribute MaxLength
   * @return Returns the facet attribute MaxLength
   */
  public Integer getMaxLength() {
    return this.maxLength;
  }

  /**
   * Returns the facet attribute Precision
   * @return Returns the facet attribute Precision
   */
  public Integer getPrecision() {
    return this.precision;
  }

  /**
   * Returns the facet attribute Scale
   * @return Returns the facet attribute Scale
   */
  public Integer getScale() {
    return this.scale;
  }

  /**
   * Returns the facet attribute SRID
   * @return Returns the facet attribute SRID
   */
  public SRID getSrid() {
    return this.srid;
  }

  /**
   * Value cast to
   * @return value cast to
   */
  public String getType() {
    return this.type;
  }

  /**
   * Cast value of the expression
   * @return Cast value
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

  public CsdlCast setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlCast setMaxLength(final Integer maxLength) {
    this.maxLength = maxLength;
    return this;
  }

  public CsdlCast setPrecision(final Integer precision) {
    this.precision = precision;
    return this;
  }

  public CsdlCast setScale(final Integer scale) {
    this.scale = scale;
    return this;
  }

  public CsdlCast setSrid(final SRID srid) {
    this.srid = srid;
    return this;
  }

  public CsdlCast setType(final String type) {
    this.type = type;
    return this;
  }

  public CsdlCast setValue(final CsdlExpression value) {
    this.value = value;
    return this;
  }
}
