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
package org.apache.olingo.commons.api.data;

import java.net.URI;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.core.Encoder;

/**
 * High-level representation of a context URL, built from the string value returned by a service; provides access to the
 * various components of the context URL, defined in the <a
 * href="http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793655">
 * protocol specification</a>.
 */
public final class ContextURL {

  /**
   * Builder for a ContextURL instance.
   */
  public static final class Builder {

    private final ContextURL contextUrl = new ContextURL();

    /**
     * Define the result as a collection.
     * @return Builder
     */
    public Builder asCollection() {
      this.contextUrl.isCollection = true;
      return this;
    }

    /**
     * Create the ContextURL instance based on set values.
     * @return the according ContextURL
     */
    public ContextURL build() {
      return this.contextUrl;
    }

    /**
     * Set the derived edm entity type.
     * @param derivedType the derived edm entity type
     * @return Builder
     */
    public Builder derived(final EdmEntityType derivedType) {
      this.contextUrl.derivedEntity = derivedType.getFullQualifiedName()
        .getFullQualifiedNameAsString();
      return this;
    }

    /**
     * Set the derived entity name.
     * @param derivedEntity the derived entity name
     * @return Builder
     */
    public Builder derivedEntity(final String derivedEntity) {
      this.contextUrl.derivedEntity = derivedEntity;
      return this;
    }

    /**
     * Set the edm entity set.
     * @param entitySet the edm entity set
     * @return Builder
     */
    public Builder entitySet(final EdmEntitySet entitySet) {
      this.contextUrl.entitySetOrSingletonOrType = entitySet.getName();
      return this;
    }

    /**
     * Set the entity set / singleton / type name.
     * @param entitySetOrSingletonOrType the entity set / singleton / type name
     * @return Builder
     */
    public Builder entitySetOrSingletonOrType(final String entitySetOrSingletonOrType) {
      this.contextUrl.entitySetOrSingletonOrType = entitySetOrSingletonOrType;
      return this;
    }

    /**
     * Set the key path.
     * @param keyPath the key path
     * @return Builder
     */
    public Builder keyPath(final String keyPath) {
      this.contextUrl.keyPath = keyPath;
      return this;
    }

    /**
     * Set the navigation or property path.
     * @param navOrPropertyPath the navigation or property path
     * @return Builder
     */
    public Builder navOrPropertyPath(final String navOrPropertyPath) {
      this.contextUrl.navOrPropertyPath = navOrPropertyPath;
      return this;
    }

    /**
     * Set the OData path.
     * @param oDataPath the OData path
     * @return Builder
     */
    public Builder oDataPath(final String oDataPath) {
      this.contextUrl.odataPath = oDataPath;
      return this;
    }

    /**
     * Set the select list.
     * @param selectList the select list
     * @return Builder
     */
    public Builder selectList(final String selectList) {
      this.contextUrl.selectList = selectList;
      return this;
    }

    /**
     * Set the service root.
     * @param serviceRoot the service root
     * @return Builder
     */
    public Builder serviceRoot(final URI serviceRoot) {
      this.contextUrl.serviceRoot = serviceRoot;
      return this;
    }

    /**
     * Set the suffix.
     * @param suffix the suffix
     * @return Builder
     */
    public Builder suffix(final Suffix suffix) {
      this.contextUrl.suffix = suffix;
      return this;
    }

    /**
     * Set the edm entity type.
     * @param type the edm entity type
     * @return Builder
     */
    public Builder type(final EdmType type) {
      this.contextUrl.entitySetOrSingletonOrType = type.getFullQualifiedName().toString();
      return this;
    }
  }

  /**
   * Suffix of the OData Context URL
   */
  public enum Suffix {
    /**
     * Suffix for Entities
     */
    ENTITY("$entity"),
    /**
     * Suffix for References
     */
    REFERENCE("$ref"),
    /**
     * Suffix for deltas (changes)
     */
    DELTA("$delta"),
    /**
     * Suffix for deleted entities in deltas
     */
    DELTA_DELETED_ENTITY("$deletedEntity"),
    /**
     * New links in deltas
     */
    DELTA_LINK("$link"),
    /**
     * Deleted links in deltas
     */
    DELTA_DELETED_LINK("$deletedLink");

    private final String representation;

    Suffix(final String representation) {
      this.representation = representation;
    }

    /**
     * Returns OData representation of the suffix
     *
     * @return Representation of the suffix
     */
    public String getRepresentation() {
      return this.representation;
    }
  }

  /**
   * Start building a ContextURL instance.
   * @return builder for building a ContextURL instance
   */
  public static Builder with() {
    return new Builder();
  }

  private URI serviceRoot;

  private String entitySetOrSingletonOrType;

  private boolean isCollection = false;

  private String derivedEntity;

  private String selectList;

  private String navOrPropertyPath;

  private String keyPath;

  private Suffix suffix;

  private String odataPath;

  private ContextURL() {
  }

  /**
   * Get the derived entity.
   * @return derived entity
   */
  public String getDerivedEntity() {
    return this.derivedEntity;
  }

  /**
   * Get the set entity set / singleton / type.
   * @return the entity set / singleton / type
   */
  public String getEntitySetOrSingletonOrType() {
    return this.entitySetOrSingletonOrType;
  }

  /**
   * Get the set key path.
   * @return the set key path
   */
  public String getKeyPath() {
    return this.keyPath;
  }

  /**
   * Get the set navigation or property path.
   * @return the set navigation or property path
   */
  public String getNavOrPropertyPath() {
    return this.navOrPropertyPath;
  }

  /**
   * Get the OData path.
   * @return the OData path
   */
  public String getODataPath() {
    return this.odataPath;
  }

  /**
   * Get the select list.
   * @return the select list
   */
  public String getSelectList() {
    return this.selectList;
  }

  /**
   * Get the service root.
   * @return the service root
   */
  public URI getServiceRoot() {
    return this.serviceRoot;
  }

  /**
   * Get the set suffix.
   * @return the set suffix
   */
  public Suffix getSuffix() {
    return this.suffix;
  }

  /**
   * Is context result a collection.
   * @return <code>true</code> for a collection, otherwise <code>false</code>
   */
  public boolean isCollection() {
    return this.isCollection;
  }

  /**
   * Is context result a delta result.
   * @return <code>true</code> for a delta result, otherwise <code>false</code>
   */
  public boolean isDelta() {
    return this.suffix == Suffix.DELTA;
  }

  /**
   * Is context result a delta deleted entity.
   * @return <code>true</code> for a delta deleted entity, otherwise <code>false</code>
   */
  public boolean isDeltaDeletedEntity() {
    return this.suffix == Suffix.DELTA_DELETED_ENTITY;
  }

  /**
   * Is context result a delta deleted link.
   * @return <code>true</code> for a delta deleted link, otherwise <code>false</code>
   */
  public boolean isDeltaDeletedLink() {
    return this.suffix == Suffix.DELTA_DELETED_LINK;
  }

  /**
   * Is context result a delta link.
   * @return <code>true</code> for a delta link, otherwise <code>false</code>
   */
  public boolean isDeltaLink() {
    return this.suffix == Suffix.DELTA_LINK;
  }

  /**
   * Is context result a entity.
   * @return <code>true</code> for a reference, otherwise <code>false</code>
   */
  public boolean isEntity() {
    return this.suffix == Suffix.ENTITY;
  }

  /**
   * Is context result a reference.
   * @return <code>true</code> for a reference, otherwise <code>false</code>
   */
  public boolean isReference() {
    return this.suffix == Suffix.REFERENCE;
  }

  @Override
  public String toString() {
    return toUriString();
  }

  public URI toURI() {
    final StringBuilder result = new StringBuilder();
    if (this.serviceRoot != null) {
      result.append(this.serviceRoot);
    } else if (getODataPath() != null) {
      final String oDataPath = getODataPath();
      final char[] chars = oDataPath.toCharArray();
      for (int i = 1; i < chars.length - 1; i++) {
        if (chars[i] == '/' && chars[i - 1] != '/') {
          result.append("../");
        }
      }
    }

    result.append(Constants.METADATA);
    final String entitySetOrSingletonOrType = getEntitySetOrSingletonOrType();
    if (entitySetOrSingletonOrType != null) {
      result.append('#');
      if (isCollection()) {
        result.append("Collection(").append(Encoder.encode(entitySetOrSingletonOrType)).append(")");
      } else {
        result.append(Encoder.encode(entitySetOrSingletonOrType));
      }
    }
    final String derivedEntity = getDerivedEntity();
    if (derivedEntity != null) {
      if (entitySetOrSingletonOrType == null) {
        throw new IllegalArgumentException(
          "ContextURL: Derived Type without anything to derive from!");
      }
      result.append('/').append(Encoder.encode(derivedEntity));
    }
    final String keyPath = getKeyPath();
    if (keyPath != null) {
      result.append('(').append(keyPath).append(')');
    }
    final String navOrPropertyPath = getNavOrPropertyPath();
    if (navOrPropertyPath != null) {
      if (this.serviceRoot == null || !this.serviceRoot.isAbsolute()) {
        final String[] paths = navOrPropertyPath.split("/");
        for (final String path : paths) {
          result.insert(0, "../");
        }
      }
      result.append('/').append(navOrPropertyPath);
    }
    final String selectList = getSelectList();
    if (selectList != null) {
      result.append('(').append(selectList).append(')');
    }
    if (isReference()) {
      if (this.serviceRoot == null || !this.serviceRoot.isAbsolute()) {
        result.insert(0, "../");
      }
      if (entitySetOrSingletonOrType != null) {
        throw new IllegalArgumentException("ContextURL: $ref with Entity Set");
      }
      if (isCollection()) {
        result.append('#')
          .append("Collection(")
          .append(ContextURL.Suffix.REFERENCE.getRepresentation())
          .append(")");
      } else {
        result.append('#').append(ContextURL.Suffix.REFERENCE.getRepresentation());
      }
    } else {
      final Suffix suffix = getSuffix();
      if (suffix != null) {
        if (entitySetOrSingletonOrType == null) {
          throw new IllegalArgumentException("ContextURL: Suffix without preceding Entity Set!");
        }
        result.append('/').append(suffix.getRepresentation());
      }
    }
    return URI.create(result.toString());
  }

  public String toUriString() {
    return toURI().toASCIIString();
  }
}
