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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

public class EdmEntityTypeImpl extends AbstractEdmStructuredType implements EdmEntityType {

  private final CsdlEntityType entityType;

  private boolean baseTypeChecked = false;

  private final boolean hasStream;

  protected EdmEntityType entityBaseType;

  private final List<String> keyPredicateNames = Collections
    .synchronizedList(new ArrayList<String>());

  private final Map<String, EdmKeyPropertyRef> keyPropertyRefs = Collections
    .synchronizedMap(new LinkedHashMap<String, EdmKeyPropertyRef>());

  private List<EdmKeyPropertyRef> keyPropertyRefsList;

  public EdmEntityTypeImpl(final Edm edm, final FullQualifiedName name,
    final CsdlEntityType entityType) {
    super(edm, name, EdmTypeKind.ENTITY, entityType);
    this.entityType = entityType;
    this.hasStream = entityType.hasStream();
  }

  @Override
  protected EdmStructuredType buildBaseType(final FullQualifiedName baseTypeName) {
    EdmEntityType baseType = null;
    if (baseTypeName != null) {
      baseType = this.edm.getEntityType(baseTypeName);
      if (baseType == null) {
        throw new EdmException(
          "Cannot find base type with name: " + baseTypeName + " for entity type: " + getName());
      }
    }
    return baseType;
  }

  @Override
  protected void checkBaseType() {
    if (!this.baseTypeChecked) {
      if (this.baseTypeName != null) {
        this.baseType = buildBaseType(this.baseTypeName);
        this.entityBaseType = (EdmEntityType)this.baseType;
      }
      if (this.baseType == null || this.baseType.isAbstract()
        && ((EdmEntityType)this.baseType).getKeyPropertyRefs().isEmpty()) {
        final List<CsdlPropertyRef> key = this.entityType.getKey();
        if (key != null) {
          final List<EdmKeyPropertyRef> edmKey = new ArrayList<>();
          for (final CsdlPropertyRef ref : key) {
            edmKey.add(new EdmKeyPropertyRefImpl(this, ref));
          }
          setEdmKeyPropertyRef(edmKey);
        }
      }
      this.baseTypeChecked = true;
    }
  }

  @Override
  public EdmEntityType getBaseType() {
    checkBaseType();
    return this.entityBaseType;
  }

  @Override
  public List<String> getKeyPredicateNames() {
    checkBaseType();
    if (this.keyPredicateNames.isEmpty() && this.baseType != null) {
      return this.entityBaseType.getKeyPredicateNames();
    }
    return Collections.unmodifiableList(this.keyPredicateNames);
  }

  @Override
  public EdmKeyPropertyRef getKeyPropertyRef(final String keyPredicateName) {
    checkBaseType();
    final EdmKeyPropertyRef edmKeyPropertyRef = this.keyPropertyRefs.get(keyPredicateName);
    if (edmKeyPropertyRef == null && this.entityBaseType != null) {
      return this.entityBaseType.getKeyPropertyRef(keyPredicateName);
    }
    return edmKeyPropertyRef;
  }

  @Override
  public List<EdmKeyPropertyRef> getKeyPropertyRefs() {
    checkBaseType();
    if (this.keyPropertyRefsList == null) {
      this.keyPropertyRefsList = new ArrayList<>(this.keyPropertyRefs.values());
    }
    if (this.keyPropertyRefsList.isEmpty() && this.entityBaseType != null) {
      return this.entityBaseType.getKeyPropertyRefs();
    }
    return Collections.unmodifiableList(this.keyPropertyRefsList);
  }

  @Override
  public boolean hasStream() {
    checkBaseType();

    if (this.hasStream || this.entityBaseType != null && this.entityBaseType.hasStream()) {
      return true;
    }
    return false;
  }

  protected void setEdmKeyPropertyRef(final List<EdmKeyPropertyRef> edmKey) {
    for (final EdmKeyPropertyRef ref : edmKey) {
      if (ref.getAlias() == null) {
        this.keyPredicateNames.add(ref.getName());
        this.keyPropertyRefs.put(ref.getName(), ref);
      } else {
        this.keyPredicateNames.add(ref.getAlias());
        this.keyPropertyRefs.put(ref.getAlias(), ref);
      }
    }
  }
}
