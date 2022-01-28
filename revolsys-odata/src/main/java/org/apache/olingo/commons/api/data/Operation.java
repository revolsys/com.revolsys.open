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

/**
 * Data representation for an operation.
 */
public class Operation {
  public enum Type {
    ACTION, FUNCTION
  }

  private String metadataAnchor;

  private String title;

  private URI target;

  private Type type;

  /**
   * Gets metadata anchor.
   *
   * @return metadata anchor.
   */
  public String getMetadataAnchor() {
    return this.metadataAnchor;
  }

  /**
   * Gets target.
   *
   * @return target.
   */
  public URI getTarget() {
    return this.target;
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
   * Gets the Operation Type
   * @return
   */
  public Type getType() {
    return this.type;
  }

  /**
   * Sets metadata anchor.
   *
   * @param metadataAnchor metadata anchor.
   */
  public void setMetadataAnchor(final String metadataAnchor) {
    this.metadataAnchor = metadataAnchor;
  }

  /**
   * Sets target.
   *
   * @param target target.
   */
  public void setTarget(final URI target) {
    this.target = target;
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
   * Set the Operation type
   * @param type
   */
  public void setType(final Type type) {
    this.type = type;
  }
}
