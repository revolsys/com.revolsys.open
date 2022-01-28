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
import java.util.Iterator;
import java.util.List;

/**
 * Data representation for a collection of single entities.
 */
public class EntityCollection extends AbstractEntityCollection {

  private final List<Entity> entities = new ArrayList<>();

  private Integer count;

  private URI next;

  private URI deltaLink;

  private final List<Operation> operations = new ArrayList<>();

  @Override
  public boolean equals(final Object o) {
    if (!super.equals(o)) {
      return false;
    }
    final EntityCollection other = (EntityCollection)o;
    return this.entities.equals(other.entities)
      && (this.count == null ? other.count == null : this.count.equals(other.count))
      && (this.next == null ? other.next == null : this.next.equals(other.next))
      && (this.deltaLink == null ? other.deltaLink == null
        : this.deltaLink.equals(other.deltaLink));
  }

  /**
   * Gets number of entries - if it was required.
   *
   * @return number of entries into the entity set.
   */
  @Override
  public Integer getCount() {
    return this.count;
  }

  /**
   * Gets delta link if exists.
   *
   * @return delta link if exists; null otherwise.
   */
  @Override
  public URI getDeltaLink() {
    return this.deltaLink;
  }

  /**
   * Gets entities.
   *
   * @return entries.
   */
  public List<Entity> getEntities() {
    return this.entities;
  }

  /**
   * Gets next link if exists.
   *
   * @return next link if exists; null otherwise.
   */
  @Override
  public URI getNext() {
    return this.next;
  }

  /**
   * Gets operations.
   *
   * @return operations.
   */
  @Override
  public List<Operation> getOperations() {
    return this.operations;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + this.entities.hashCode();
    result = 31 * result + (this.count == null ? 0 : this.count.hashCode());
    result = 31 * result + (this.next == null ? 0 : this.next.hashCode());
    result = 31 * result + (this.deltaLink == null ? 0 : this.deltaLink.hashCode());
    return result;
  }

  @Override
  public Iterator<Entity> iterator() {
    return this.entities.iterator();
  }

  /**
   * Sets number of entries.
   *
   * @param count number of entries
   */
  public void setCount(final Integer count) {
    this.count = count;
  }

  /**
   * Sets delta link.
   *
   * @param deltaLink delta link.
   */
  public void setDeltaLink(final URI deltaLink) {
    this.deltaLink = deltaLink;
  }

  /**
   * Sets next link.
   *
   * @param next next link.
   */
  public void setNext(final URI next) {
    this.next = next;
  }

  @Override
  public String toString() {
    return this.entities.toString();
  }
}
