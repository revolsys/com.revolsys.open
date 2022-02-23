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

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.annotation.EdmExpression;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.core.edm.annotation.AbstractEdmExpression;

public class EdmAnnotationImpl extends AbstractEdmAnnotatable implements EdmAnnotation {

  private final CsdlAnnotation annotation;

  private EdmTerm term;

  private EdmExpression expression;

  public EdmAnnotationImpl(final Edm edm, final CsdlAnnotation annotation) {
    super(edm, annotation);
    this.annotation = annotation;
  }

  @Override
  public EdmExpression getExpression() {
    if (this.expression == null && this.annotation.getExpression() != null) {
      this.expression = AbstractEdmExpression.getExpression(this.edm,
        this.annotation.getExpression());
    }
    return this.expression;
  }

  @Override
  public String getQualifier() {
    return this.annotation.getQualifier();
  }

  @Override
  public EdmTerm getTerm() {
    if (this.term == null) {
      if (this.annotation.getTerm() == null) {
        throw new EdmException("Term must not be null for an annotation.");
      }
      this.term = this.edm.getTerm(new FullQualifiedName(this.annotation.getTerm()));
    }
    return this.term;
  }
}
