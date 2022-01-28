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

import org.apache.olingo.commons.api.edm.FullQualifiedName;

/**
 * The type Csdl binding target.
 */
public abstract class CsdlBindingTarget implements CsdlAbstractEdmItem, CsdlNamed, CsdlAnnotatable {

  /**
   * The Name.
   */
  protected String name;

  /**
   * The human readable title.
   */
  protected String title;

  /**
   * The Type.
   */
  protected FullQualifiedName type;

  /**
   * The Navigation property bindings.
   */
  protected List<CsdlNavigationPropertyBinding> navigationPropertyBindings = new ArrayList<>();

  /**
   * Mapping for server use cases
   */
  protected CsdlMapping mapping;

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  public CsdlMapping getMapping() {
    return this.mapping;
  }

  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Gets navigation property bindings.
   *
   * @return the navigation property bindings
   */
  public List<CsdlNavigationPropertyBinding> getNavigationPropertyBindings() {
    return this.navigationPropertyBindings;
  }

  public String getTitle() {
    return this.title;
  }

  /**
   * Gets type.
   *
   * @return the type
   */
  public String getType() {
    return this.type.getFullQualifiedNameAsString();
  }

  /**
   * Gets type fQN.
   *
   * @return the type fQN
   */
  public FullQualifiedName getTypeFQN() {
    return this.type;
  }

  /**
   * Sets a list of annotations
   * @param annotations list of annotations
   * @return this instance
   */
  public CsdlBindingTarget setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  /**
   * Sets the mapping object for this binding target.
   * @param mapping
   * @return this instance
   */
  public CsdlBindingTarget setMapping(final CsdlMapping mapping) {
    this.mapping = mapping;
    return this;
  }

  /**
   * Sets name.
   *
   * @param name the name
   * @return the name
   */
  public CsdlBindingTarget setName(final String name) {
    this.name = name;
    return this;
  }

  /**
   * Sets navigation property bindings.
   *
   * @param navigationPropertyBindings the navigation property bindings
   * @return the navigation property bindings
   */
  public CsdlBindingTarget setNavigationPropertyBindings(
    final List<CsdlNavigationPropertyBinding> navigationPropertyBindings) {
    this.navigationPropertyBindings = navigationPropertyBindings;
    return this;
  }

  /**
   * A human readable title for this instance
   * @param title
   * @return this instance
   */
  public CsdlBindingTarget setTitle(final String title) {
    this.title = title;
    return this;
  }

  /**
   * Sets type.
   *
   * @param type the type
   * @return the type
   */
  public CsdlBindingTarget setType(final FullQualifiedName type) {
    this.type = type;
    return this;
  }

  /**
   * Sets type.
   *
   * @param type the type
   * @return the type
   */
  public CsdlBindingTarget setType(final String type) {
    this.type = new FullQualifiedName(type);
    return this;
  }
}
