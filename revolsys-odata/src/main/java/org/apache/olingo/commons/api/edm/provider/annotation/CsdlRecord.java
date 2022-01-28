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

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotatable;
import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

/**
 * The edm:Record expression enables a new entity type or complex type instance to be constructed.
 * A record expression contains zero or more edm:PropertyValue (See {@link CsdlRecord} )elements.
 */
public class CsdlRecord extends CsdlDynamicExpression implements CsdlAnnotatable {

  private String type;

  private List<CsdlPropertyValue> propertyValues = new ArrayList<>();

  private List<CsdlAnnotation> annotations = new ArrayList<>();

  private boolean checkAnnotations(final List<CsdlAnnotation> csdlRecordAnnot) {
    if (csdlRecordAnnot == null) {
      return false;
    }
    if (this.getAnnotations().size() == csdlRecordAnnot.size()) {
      for (int i = 0; i < this.getAnnotations().size(); i++) {
        if (!this.getAnnotations().get(i).equals(csdlRecordAnnot.get(i))) {
          return false;
        }
      }
    } else {
      return false;
    }
    return true;
  }

  private boolean checkPropertyValues(final List<CsdlPropertyValue> csdlRecordpropertyValues) {
    if (csdlRecordpropertyValues == null) {
      return false;
    }
    if (this.getPropertyValues().size() == csdlRecordpropertyValues.size()) {
      for (int i = 0; i < this.getPropertyValues().size(); i++) {
        if (!this.getPropertyValues().get(i).equals(csdlRecordpropertyValues.get(i))) {
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
    if (obj == null || !(obj instanceof CsdlRecord)) {
      return false;
    }
    final CsdlRecord csdlRecord = (CsdlRecord)obj;
    return (this.getType() == null ? csdlRecord.getType() == null
      : this.getType().equals(csdlRecord.getType()))
      && (this.getAnnotations() == null ? csdlRecord.getAnnotations() == null
        : checkAnnotations(csdlRecord.getAnnotations()))
      && (this.getPropertyValues() == null ? csdlRecord.getPropertyValues() == null
        : checkPropertyValues(csdlRecord.getPropertyValues()));
  }

  @Override
  public List<CsdlAnnotation> getAnnotations() {
    return this.annotations;
  }

  /**
   * List of edm:PropertyValues (See {@link CsdlPropertyValue}
   * @return List of edm:PropertyValues (See
   */
  public List<CsdlPropertyValue> getPropertyValues() {
    return this.propertyValues;
  }

  /**
   * Returns the entity type or complex type to be constructed.
   * @return Entity type or complex type
   */
  public String getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (this.type == null ? 0 : this.type.hashCode());
    result = prime * result + (this.propertyValues == null ? 0 : this.propertyValues.hashCode());
    result = prime * result + (this.annotations == null ? 0 : this.annotations.hashCode());
    return result;
  }

  public CsdlRecord setAnnotations(final List<CsdlAnnotation> annotations) {
    this.annotations = annotations;
    return this;
  }

  public CsdlRecord setPropertyValues(final List<CsdlPropertyValue> propertyValues) {
    this.propertyValues = propertyValues;
    return this;
  }

  public CsdlRecord setType(final String type) {
    this.type = type;
    return this;
  }
}
