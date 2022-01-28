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
package org.apache.olingo.server.core.uri.queryoption.apply;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmMapping;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.geo.SRID;

/** A dynamic EDM property containing an aggregation. */
public class DynamicProperty implements EdmProperty {

  private final String name;

  private final EdmType propertyType;

  private Integer precision;

  private Integer scale;

  private String scaleAsString;

  /** Creates a dynamic property with a mandatory name and an optional type. */
  public DynamicProperty(final String name, final EdmType type) {
    this.name = name;
    this.propertyType = type;
  }

  @Override
  public EdmAnnotation getAnnotation(final EdmTerm term, final String qualifier) {
    return null;
  }

  @Override
  public List<EdmAnnotation> getAnnotations() {
    return Collections.emptyList();
  }

  @Override
  public String getDefaultValue() {
    return null;
  }

  @Override
  public EdmMapping getMapping() {
    return null;
  }

  @Override
  public Integer getMaxLength() {
    return null;
  }

  @Override
  public String getMimeType() {
    return null;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Integer getPrecision() {
    return this.precision;
  }

  @Override
  public Integer getScale() {
    return this.scale;
  }

  @Override
  public String getScaleAsString() {
    return this.scaleAsString;
  }

  @Override
  public SRID getSrid() {
    return null;
  }

  @Override
  public EdmType getType() {
    return this.propertyType;
  }

  @Override
  public EdmType getTypeWithAnnotations() {
    return this.propertyType;
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public boolean isNullable() {
    return false;
  }

  @Override
  public boolean isPrimitive() {
    return this.propertyType != null && this.propertyType.getKind() == EdmTypeKind.PRIMITIVE;
  }

  @Override
  public boolean isUnicode() {
    return true;
  }

  public DynamicProperty setPrecision(final Integer precision) {
    this.precision = precision;
    return this;
  }

  public DynamicProperty setScale(final Integer scale) {
    this.scale = scale;
    return this;
  }

  public DynamicProperty setScaleAsString(final String scaleAsString) {
    this.scaleAsString = scaleAsString;
    return this;
  }
}
