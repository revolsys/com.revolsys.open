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
 * A Delta instance contains all added and deleted links and all deleted entities.
 */
public class Delta extends EntityCollection {

  private final List<DeletedEntity> deletedEntities = new ArrayList<>();

  private final List<DeltaLink> addedLinks = new ArrayList<>();

  private final List<DeltaLink> deletedLinks = new ArrayList<>();

  @Override
  public boolean equals(final Object o) {
    return super.equals(o) && this.deletedEntities.equals(((Delta)o).deletedEntities)
      && this.addedLinks.equals(((Delta)o).addedLinks)
      && this.deletedLinks.equals(((Delta)o).deletedLinks);
  }

  /**
   * Get list of added links (must not be NULL).
   * @return list of added links (must not be NULL)
   */
  public List<DeltaLink> getAddedLinks() {
    return this.addedLinks;
  }

  /**
   * Get list of deleted entities (must not be NULL).
   * @return list of deleted entities (must not be NULL)
   */
  public List<DeletedEntity> getDeletedEntities() {
    return this.deletedEntities;
  }

  /**
   * Get list of deleted links (must not be NULL).
   * @return list of deleted links (must not be NULL)
   */
  public List<DeltaLink> getDeletedLinks() {
    return this.deletedLinks;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + this.deletedEntities.hashCode();
    result = 31 * result + this.addedLinks.hashCode();
    result = 31 * result + this.deletedLinks.hashCode();
    return result;
  }
}
