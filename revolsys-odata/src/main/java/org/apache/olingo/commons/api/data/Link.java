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

import java.util.ArrayList;
import java.util.List;

/**
 * Data representation for a link.
 */
public class Link extends Annotatable {

  private String title;

  private String rel;

  private String href;

  private String type;

  private String mediaETag;

  private Entity entity;

  private EntityCollection entitySet;

  private String bindingLink;

  private List<String> bindingLinks = new ArrayList<>();

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Link other = (Link)o;
    return getAnnotations().equals(other.getAnnotations())
      && (this.title == null ? other.title == null : this.title.equals(other.title))
      && (this.rel == null ? other.rel == null : this.rel.equals(other.rel))
      && (this.href == null ? other.href == null : this.href.equals(other.href))
      && (this.type == null ? other.type == null : this.type.equals(other.type))
      && (this.mediaETag == null ? other.mediaETag == null : this.mediaETag.equals(other.mediaETag))
      && (this.entity == null ? other.entity == null : this.entity.equals(other.entity))
      && (this.entitySet == null ? other.entitySet == null : this.entitySet.equals(other.entitySet))
      && (this.bindingLink == null ? other.bindingLink == null
        : this.bindingLink.equals(other.bindingLink))
      && this.bindingLinks.equals(other.bindingLinks);
  }

  /**
   * If this is a "toOne" relationship this method delivers the binding link or <tt>null</tt> if not set.
   * @return String the binding link.
   */
  public String getBindingLink() {
    return this.bindingLink;
  }

  /**
   * If this is a "toMany" relationship this method delivers the binding links or <tt>emptyList</tt> if not set.
   * @return a list of binding links.
   */
  public List<String> getBindingLinks() {
    return this.bindingLinks;
  }

  /**
   * Gets href.
   *
   * @return href.
   */
  public String getHref() {
    return this.href;
  }

  /**
   * Gets in-line entity.
   *
   * @return in-line entity.
   */
  public Entity getInlineEntity() {
    return this.entity;
  }

  /**
   * Gets in-line entity set.
   *
   * @return in-line entity set.
   */
  public EntityCollection getInlineEntitySet() {
    return this.entitySet;
  }

  /**
   * Gets Media ETag.
   *
   * @return media ETag
   */
  public String getMediaETag() {
    return this.mediaETag;
  }

  /**
   * Gets rel info.
   *
   * @return rel info.
   */
  public String getRel() {
    return this.rel;
  }

  /**
   * Gets title.
   *
   * @return title.
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * Gets type.
   *
   * @return type.
   */
  public String getType() {
    return this.type;
  }

  @Override
  public int hashCode() {
    int result = getAnnotations().hashCode();
    result = 31 * result + (this.title == null ? 0 : this.title.hashCode());
    result = 31 * result + (this.rel == null ? 0 : this.rel.hashCode());
    result = 31 * result + (this.href == null ? 0 : this.href.hashCode());
    result = 31 * result + (this.type == null ? 0 : this.type.hashCode());
    result = 31 * result + (this.mediaETag == null ? 0 : this.mediaETag.hashCode());
    result = 31 * result + (this.entity == null ? 0 : this.entity.hashCode());
    result = 31 * result + (this.entitySet == null ? 0 : this.entitySet.hashCode());
    result = 31 * result + (this.bindingLink == null ? 0 : this.bindingLink.hashCode());
    result = 31 * result + this.bindingLinks.hashCode();
    return result;
  }

  /**
   * Sets the binding link.
   * @param bindingLink name of binding link
   */
  public void setBindingLink(final String bindingLink) {
    this.bindingLink = bindingLink;
  }

  /**
   * Sets the binding links. List MUST NOT be <tt>null</tt>.
   * @param bindingLinks list of binding link names
   */
  public void setBindingLinks(final List<String> bindingLinks) {
    this.bindingLinks = bindingLinks;
  }

  /**
   * Sets href.
   *
   * @param href href.
   */
  public void setHref(final String href) {
    this.href = href;
  }

  /**
   * Sets in-line entity.
   *
   * @param entity entity.
   */
  public void setInlineEntity(final Entity entity) {
    this.entity = entity;
  }

  /**
   * Sets in-line entity set.
   *
   * @param entitySet entity set.
   */
  public void setInlineEntitySet(final EntityCollection entitySet) {
    this.entitySet = entitySet;
  }

  /**
   * Sets Media ETag.
   *
   * @param mediaETag media ETag
   */
  public void setMediaETag(final String mediaETag) {
    this.mediaETag = mediaETag;
  }

  /**
   * Sets rel info.
   *
   * @param rel rel info.
   */
  public void setRel(final String rel) {
    this.rel = rel;
  }

  /**
   * Sets title.
   *
   * @param title title.
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * Sets type.
   *
   * @param type type.
   */
  public void setType(final String type) {
    this.type = type;
  }
}
