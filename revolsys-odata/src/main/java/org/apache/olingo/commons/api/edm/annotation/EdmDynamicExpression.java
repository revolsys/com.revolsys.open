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
package org.apache.olingo.commons.api.edm.annotation;

/**
 * Represents a dynamic expression
 */
public interface EdmDynamicExpression extends EdmExpression {

  /**
   * Casts the expression to a {@link EdmAnd} expression
   * @return EdmAnd expression
   */
  EdmAnd asAnd();

  /**
   * Casts the expression to a {@link EdmAnnotationPath} expression
   * @return EdmAnnotationPath expression
   */
  EdmAnnotationPath asAnnotationPath();

  /**
   * Casts the expression to a {@link EdmApply} expression
   * @return EdmApply expression
   */
  EdmApply asApply();

  /**
   * Casts the expression to a {@link EdmCast} expression
   * @return EdmCast expression
   */
  EdmCast asCast();

  /**
   * Casts the expression to a {@link EdmCollection} expression
   * @return EdmCollection expression
   */
  EdmCollection asCollection();

  /**
   * Casts the expression to a {@link EdmEq} expression
   * @return EdmEq expression
   */
  EdmEq asEq();

  /**
   * Casts the expression to a {@link EdmGe} expression
   * @return EdmGe expression
   */
  EdmGe asGe();

  /**
   * Casts the expression to a {@link EdmGt} expression
   * @return EdmGt expression
   */
  EdmGt asGt();

  /**
   * Casts the expression to a {@link EdmIf} expression
   * @return EdmIf expression
   */
  EdmIf asIf();

  /**
   * Casts the expression to a {@link EdmIsOf} expression
   * @return EdmIsOf expression
   */
  EdmIsOf asIsOf();

  /**
   * Casts the expression to a {@link EdmLabeledElement} expression
   * @return EdmLabeledElement expression
   */
  EdmLabeledElement asLabeledElement();

  /**
   * Casts the expression to a {@link EdmLabeledElementReference} expression
   * @return EdmLabeledElementReference expression
   */
  EdmLabeledElementReference asLabeledElementReference();

  /**
   * Casts the expression to a {@link EdmLe} expression
   * @return EdmLe expression
   */
  EdmLe asLe();

  /**
   * Casts the expression to a {@link EdmLt} expression
   * @return EdmLt expression
   */
  EdmLt asLt();

  /**
   * Casts the expression to a {@link EdmNavigationPropertyPath} expression
   * @return EdmNavigationPropertyPath expression
   */
  EdmNavigationPropertyPath asNavigationPropertyPath();

  /**
   * Casts the expression to a {@link EdmNe} expression
   * @return EdmNe expression
   */
  EdmNe asNe();

  /**
   * Casts the expression to a {@link EdmNot} expression
   * @return EdmNot expression
   */
  EdmNot asNot();

  /**
   * Casts the expression to a {@link EdmNull} expression
   * @return EdmNull expression
   */
  EdmNull asNull();

  /**
   * Casts the expression to a {@link EdmOr} expression
   * @return EdmOr expression
   */
  EdmOr asOr();

  /**
   * Casts the expression to a {@link EdmPath} expression
   * @return EdmPath expression
   */
  EdmPath asPath();

  /**
   * Casts the expression to a {@link EdmPropertyPath} expression
   * @return EdmPropertyPath expression
   */
  EdmPropertyPath asPropertyPath();

  /**
   * Casts the expression to a {@link EdmPropertyValue} expression
   * @return EdmPropertyValue expression
   */
  EdmPropertyValue asPropertyValue();

  /**
   * Casts the expression to a {@link EdmRecord} expression
   * @return EdmRecord expression
   */
  EdmRecord asRecord();

  /**
   * Casts the expression to a {@link EdmUrlRef} expression
   * @return EdmUrlRef expression
   */
  EdmUrlRef asUrlRef();

  /**
   * Returns true if the expression is a logical edm:And expression
   * @return  true if the expression is a logical edm:And expression
   */
  boolean isAnd();

  /**
   * Returns true if the expression is a edm:AnnotationPath expression
   * @return  true if the expression is a edm:AnnotationPath expression
   */
  boolean isAnnotationPath();

  /**
   * Returns true if the expression is a edm:Apply expression
   * @return  true if the expression is a edm:Apply expression
   */
  boolean isApply();

  /**
   * Returns true if the expression is a edm:Cast expression
   * @return  true if the expression is a edm:Cast expression
   */
  boolean isCast();

  /**
   * Returns true if the expression is a edm:Collection expression
   * @return  true if the expression is a edm:Collection expression
   */
  boolean isCollection();

  /**
   * Returns true if the expression is a edm:Eq expression
   * @return  true if the expression is a edm:Eq expression
   */
  boolean isEq();

  /**
   * Returns true if the expression is a edm:Ge expression
   * @return  true if the expression is a edm:Ge expression
   */
  boolean isGe();

  /**
   * Returns true if the expression is a edm:Gt expression
   * @return  true if the expression is a edm:Gt expression
   */
  boolean isGt();

  /**
   * Returns true if the expression is a edm:If expression
   * @return  true if the expression is a edm:If expression
   */
  boolean isIf();

  /**
   * Returns true if the expression is a edm:IsOf expression
   * @return  true if the expression is a edm:IsOf expression
   */
  boolean isIsOf();

  /**
   * Returns true if the expression is a edm:LabeledElement expression
   * @return  true if the expression is a edm:LabeledElement expression
   */
  boolean isLabeledElement();

  /**
   * Returns true if the expression is a edm:LabeledElementReference expression
   * @return  true if the expression is a edm:LabeledElementReference expression
   */
  boolean isLabeledElementReference();

  /**
   * Returns true if the expression is a edm:Le expression
   * @return  true if the expression is a edm:Le expression
   */
  boolean isLe();

  /**
   * Returns true if the expression is a edm:Lt expression
   * @return  true if the expression is a edm:Lt expression
   */
  boolean isLt();

  /**
   * Returns true if the expression is a edm:NavigationPropertyPath expression
   * @return  true if the expression is a edm:NavigationPropertyPath expression
   */
  boolean isNavigationPropertyPath();

  /**
   * Returns true if the expression is a edm:Ne expression
   * @return  true if the expression is a edm:Ne expression
   */
  boolean isNe();

  /**
   * Returns true if the expression is a logical edm:Not expression
   * @return  true if the expression is a logical edm:Not expression
   */
  boolean isNot();

  /**
   * Returns true if the expression is a edm:Null expression
   * @return  true if the expression is a edm:Null expression
   */
  boolean isNull();

  /**
   * Returns true if the expression is a logical edm:Or expression
   * @return  true if the expression is a logical edm:Or expression
   */
  boolean isOr();

  /**
   * Returns true if the expression is a edm:Path expression
   * @return  true if the expression is a edm:Path expression
   */
  boolean isPath();

  /**
   * Returns true if the expression is a edm:PropertyPath expression
   * @return  true if the expression is a edm:PropertyPath expression
   */
  boolean isPropertyPath();

  /**
   * Returns true if the expression is a edm:PropertyValue expression
   * @return  true if the expression is a edm:PropertyValue expression
   */
  boolean isPropertyValue();

  /**
   * Returns true if the expression is a edm:Record expression
   * @return  true if the expression is a edm:Record expression
   */
  boolean isRecord();

  /**
   * Returns true if the expression is a edm:UrlRef expression
   * @return  true if the expression is a edm:UrlRef expression
   */
  boolean isUrlRef();
}
