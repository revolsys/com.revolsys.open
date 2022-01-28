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
package org.apache.olingo.commons.api.edm;

import java.io.Serializable;

/**
 * A full qualified name of any element in the EDM consists of a name and a namespace.
 */
public final class FullQualifiedName implements Serializable {

  private static final long serialVersionUID = -4063629050858999076L;

  private final String namespace;

  private final String name;

  private final String fqn;

  /**
   * Create the FQN with given namespace and name (which is split of last <code>.</code> of the parameter).
   * @param namespaceAndName namespace and name of FQN
   */
  public FullQualifiedName(final String namespaceAndName) {
    final int dotIdx = namespaceAndName.lastIndexOf('.');
    if (dotIdx == -1 || dotIdx == 0 || dotIdx == namespaceAndName.length() - 1) {
      throw new IllegalArgumentException(
        "Malformed " + FullQualifiedName.class.getSimpleName() + ": " + namespaceAndName);
    }

    this.fqn = namespaceAndName;
    this.namespace = this.fqn.substring(0, dotIdx);
    this.name = this.fqn.substring(dotIdx + 1);
  }

  /**
   * Create the FQN with given namespace and name
   * @param namespace namespace of FQN
   * @param name name of FQN
   */
  public FullQualifiedName(final String namespace, final String name) {
    this.namespace = namespace;
    this.name = name;
    this.fqn = namespace + "." + name;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final FullQualifiedName that = (FullQualifiedName)o;
    return (this.namespace == null ? that.namespace == null : this.namespace.equals(that.namespace))
      && (this.name == null ? that.name == null : this.name.equals(that.name));
  }

  /**
   * @return namespace.name
   */
  public String getFullQualifiedNameAsString() {
    return this.fqn;
  }

  /**
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return namespace
   */
  public String getNamespace() {
    return this.namespace;
  }

  @Override
  public int hashCode() {
    return this.fqn == null ? 0 : this.fqn.hashCode();
  }

  @Override
  public String toString() {
    return this.fqn;
  }
}
