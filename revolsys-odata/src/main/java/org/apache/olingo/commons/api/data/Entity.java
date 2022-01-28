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
import java.util.ArrayList;
import java.util.List;

/**
 * Data representation for a single entity.
 */
public class Entity extends Linked {

  private String eTag;

  private String type;

  private Link readLink;

  private Link editLink;

  private final List<Link> mediaEditLinks = new ArrayList<>();

  private final List<Operation> operations = new ArrayList<>();

  private final List<Property> properties = new ArrayList<>();

  private URI mediaContentSource;

  private String mediaContentType;

  private String mediaETag;

  /**
   * Add property to this Entity.
   *
   * @param property property which is added
   * @return this Entity for fluid/flow adding
   */
  public Entity addProperty(final Property property) {
    this.properties.add(property);
    return this;
  }

  @Override
  public boolean equals(final Object o) {
    return super.equals(o)
      && (this.eTag == null ? ((Entity)o).eTag == null : this.eTag.equals(((Entity)o).eTag))
      && (this.type == null ? ((Entity)o).type == null : this.type.equals(((Entity)o).type))
      && (this.readLink == null ? ((Entity)o).readLink == null
        : this.readLink.equals(((Entity)o).readLink))
      && (this.editLink == null ? ((Entity)o).editLink == null
        : this.editLink.equals(((Entity)o).editLink))
      && this.mediaEditLinks.equals(((Entity)o).mediaEditLinks)
      && this.operations.equals(((Entity)o).operations)
      && this.properties.equals(((Entity)o).properties)
      && (this.mediaContentSource == null ? ((Entity)o).mediaContentSource == null
        : this.mediaContentSource.equals(((Entity)o).mediaContentSource))
      && (this.mediaContentType == null ? ((Entity)o).mediaContentType == null
        : this.mediaContentType.equals(((Entity)o).mediaContentType))
      && (this.mediaETag == null ? ((Entity)o).mediaETag == null
        : this.mediaETag.equals(((Entity)o).mediaETag));
  }

  /**
   * Gets entity edit link.
   *
   * @return edit link.
   */
  public Link getEditLink() {
    return this.editLink;
  }

  /**
   * Gets ETag.
   *
   * @return ETag.
   */
  public String getETag() {
    return this.eTag;
  }

  /**
   * Gets media content resource.
   *
   * @return media content resource.
   */
  public URI getMediaContentSource() {
    return this.mediaContentSource;
  }

  /**
   * Gets media content type.
   *
   * @return media content type.
   */
  public String getMediaContentType() {
    return this.mediaContentType;
  }

  /**
   * Gets media entity links.
   *
   * @return links.
   */
  public List<Link> getMediaEditLinks() {
    return this.mediaEditLinks;
  }

  /**
   * ETag of the binary stream represented by this media entity or named stream property.
   *
   * @return media ETag value
   */
  public String getMediaETag() {
    return this.mediaETag;
  }

  /**
   * Gets operations.
   *
   * @return operations.
   */
  public List<Operation> getOperations() {
    return this.operations;
  }

  /**
   * Gets properties.
   *
   * @return properties.
   */
  public List<Property> getProperties() {
    return this.properties;
  }

  /**
   * Gets property with given name.
   *
   * @param name property name
   * @return property with given name if found, null otherwise
   */
  public Property getProperty(final String name) {
    Property result = null;

    for (final Property property : this.properties) {
      if (name.equals(property.getName())) {
        result = property;
        break;
      }
    }

    return result;
  }

  /**
   * Gets entity self link.
   *
   * @return self link.
   */
  public Link getSelfLink() {
    return this.readLink;
  }

  /**
   * Gets entity type.
   *
   * @return entity type.
   */
  public String getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (this.eTag == null ? 0 : this.eTag.hashCode());
    result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
    result = 31 * result + (this.readLink == null ? 0 : this.readLink.hashCode());
    result = 31 * result + (this.editLink == null ? 0 : this.editLink.hashCode());
    result = 31 * result + this.mediaEditLinks.hashCode();
    result = 31 * result + this.operations.hashCode();
    result = 31 * result + this.properties.hashCode();
    result = 31 * result
      + (this.mediaContentSource == null ? 0 : this.mediaContentSource.hashCode());
    result = 31 * result + (this.mediaContentType == null ? 0 : this.mediaContentType.hashCode());
    result = 31 * result + (this.mediaETag == null ? 0 : this.mediaETag.hashCode());
    return result;
  }

  /**
   * Checks if the current entity is a media entity.
   *
   * @return 'TRUE' if is a media entity; 'FALSE' otherwise.
   */
  public boolean isMediaEntity() {
    return this.mediaContentSource != null;
  }

  /**
   * Sets entity edit link.
   *
   * @param editLink edit link.
   */
  public void setEditLink(final Link editLink) {
    this.editLink = editLink;
  }

  /**
   * Sets ETag
   * @param eTag ETag
   */
  public void setETag(final String eTag) {
    this.eTag = eTag;
  }

  /**
   * Set media content source.
   *
   * @param mediaContentSource media content source.
   */
  public void setMediaContentSource(final URI mediaContentSource) {
    this.mediaContentSource = mediaContentSource;
  }

  /**
   * Set media content type.
   *
   * @param mediaContentType media content type.
   */
  public void setMediaContentType(final String mediaContentType) {
    this.mediaContentType = mediaContentType;
  }

  /**
   * Set media ETag.
   *
   * @param eTag media ETag value
   */
  public void setMediaETag(final String eTag) {
    this.mediaETag = eTag;
  }

  /**
   * Sets entity self link.
   *
   * @param selfLink self link.
   */
  public void setSelfLink(final Link selfLink) {
    this.readLink = selfLink;
  }

  /**
   * Sets entity type.
   *
   * @param type entity type.
   */
  public void setType(final String type) {
    this.type = type;
  }

  @Override
  public String toString() {
    return this.properties.toString();
  }
}
