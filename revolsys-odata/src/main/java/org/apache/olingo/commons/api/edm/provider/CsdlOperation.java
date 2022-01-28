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
package org.apache.olingo.commons.api.edm.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Csdl operation.
 */
public abstract class CsdlOperation implements CsdlAbstractEdmItem, CsdlNamed, CsdlAnnotatable {

  /**
   * The Name.
   */
  protected String name;

  /**
   * The Is bound.
   */
  protected boolean isBound = false;

  /**
   * The Entity set path.
   */
  protected String entitySetPath;

  /**
   * The Parameters.
   */
  protected List<CsdlParameter> parameters = new ArrayList<>();

  /**
   * The Return type.
   */
  protected CsdlReturnType returnType;

  /**
   * The Annotations.
   */
  protected List<CsdlAnnotation> annotations = new ArrayList<>();

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * Gets entity set path.
   *
   * @return the entity set path
   */
  public String getEntitySetPath() {
    return this.entitySetPath;
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Gets parameter.
   *
   * @param name the name
   * @return the parameter
   */
  public CsdlParameter getParameter(final String name) {
    return getOneByName(name, getParameters());
  }

  /**
   * Gets parameters.
   *
   * @return the parameters
   */
  public List<CsdlParameter> getParameters() {
    return this.parameters;
  }

  /**
   * Gets return type.
   *
   * @return the return type
   */
  public CsdlReturnType getReturnType() {
    return this.returnType;
  }

  /**
   * Is bound.
   *
   * @return the boolean
   */
  public boolean isBound() {
    return this.isBound;
  }

  /**
   * Sets a list of annotations
   * @param annotations list of annotations
   * @return this instance
   */
  public CsdlOperation setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  /**
   * Sets as bound operation.
   *
   * @param isBound the is bound
   * @return the bound
   */
  public CsdlOperation setBound(final boolean isBound) {
    this.isBound = isBound;
    return this;
  }

  /**
   * Sets entity set path.
   *
   * @param entitySetPath the entity set path
   * @return the entity set path
   */
  public CsdlOperation setEntitySetPath(final String entitySetPath) {
    this.entitySetPath = entitySetPath;
    return this;
  }

  /**
   * Sets name.
   *
   * @param name the name
   * @return the name
   */
  public CsdlOperation setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets parameters.
   *
   * @param parameters the parameters
   * @return the parameters
   */
  public CsdlOperation setParameters(final List<CsdlParameter> parameters) {
    this.parameters = parameters;
    return this;
  }

  /**
   * Sets return type.
   *
   * @param returnType the return type
   * @return the return type
   */
  public CsdlOperation setReturnType(final CsdlReturnType returnType) {
    this.returnType = returnType;
    return this;
  }
}
