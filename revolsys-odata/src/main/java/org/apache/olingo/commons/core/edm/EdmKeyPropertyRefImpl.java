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

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmStructuredType;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;

public class EdmKeyPropertyRefImpl implements EdmKeyPropertyRef {

  private final CsdlPropertyRef ref;

  private final EdmEntityType edmEntityType;

  private EdmProperty property;

  public EdmKeyPropertyRefImpl(final EdmEntityType edmEntityType, final CsdlPropertyRef ref) {
    this.edmEntityType = edmEntityType;
    this.ref = ref;
  }

  @Override
  public String getAlias() {
    return this.ref.getAlias();
  }

  @Override
  public String getName() {
    return this.ref.getName();
  }

  @Override
  public EdmProperty getProperty() {
    if (this.property == null) {
      if (getAlias() == null) {
        this.property = this.edmEntityType.getStructuralProperty(getName());
        if (this.property == null) {
          throw new EdmException(
            "Invalid key property ref specified. Can´t find property with name: " + getName());
        }
      } else {
        if (getName() == null || getName().isEmpty()) {
          throw new EdmException("Alias but no path specified for propertyRef");
        }
        final String[] splitPath = getName().split("/");
        EdmStructuredType structType = this.edmEntityType;
        for (int i = 0; i < splitPath.length - 1; i++) {
          final EdmProperty _property = structType.getStructuralProperty(splitPath[i]);
          if (_property == null) {
            throw new EdmException(
              "Invalid property ref specified. Can´t find property with name: " + splitPath[i]
                + " at type: " + structType.getNamespace() + "." + structType.getName());
          }
          structType = (EdmStructuredType)_property.getType();
        }
        this.property = structType.getStructuralProperty(splitPath[splitPath.length - 1]);
        if (this.property == null) {
          throw new EdmException("Invalid property ref specified. Can´t find property with name: "
            + splitPath[splitPath.length - 1] + " at type: " + structType.getNamespace() + "."
            + structType.getName());
        }
      }
    }

    return this.property;
  }
}
