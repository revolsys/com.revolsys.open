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

import org.apache.olingo.commons.api.edm.EdmAnnotatable;
import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

public abstract class AbstractEdmAnnotatable implements EdmAnnotatable {

  private final CsdlAnnotatable annotatable;

  private List<EdmAnnotation> annotations;

  protected final Edm edm;

  public AbstractEdmAnnotatable(final Edm edm, final CsdlAnnotatable annotatable) {
    this.edm = edm;
    this.annotatable = annotatable;
  }

  @Override
  public EdmAnnotation getAnnotation(final EdmTerm term, final String qualifier) {
    EdmAnnotation result = null;
    for (final EdmAnnotation annotation : getAnnotations()) {
      if (term.getFullQualifiedName().equals(annotation.getTerm().getFullQualifiedName())
        && qualifierEqual(qualifier, annotation.getQualifier())) {
        result = annotation;
        break;
      }
    }
    return result;
  }

  @Override
  public List<EdmAnnotation> getAnnotations() {
    if (this.annotations == null) {
      final List<EdmAnnotation> annotationsLocal = new ArrayList<>();
      if (this.annotatable != null) {
        for (final CsdlAnnotation annotation : this.annotatable.getAnnotations()) {
          annotationsLocal.add(new EdmAnnotationImpl(this.edm, annotation));
        }

        this.annotations = Collections.unmodifiableList(annotationsLocal);
      }
    }
    return this.annotations;
  }

  private boolean qualifierEqual(final String qualifier, final String annotationQualifier) {
    return qualifier == null && annotationQualifier == null
      || qualifier != null && qualifier.equals(annotationQualifier);
  }

  protected void setAnnotations(final List<EdmAnnotation> annotations) {
    this.annotations = annotations;
  }
}
