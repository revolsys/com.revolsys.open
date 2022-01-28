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
package org.apache.olingo.server.api.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.edm.EdmProperty;

/** Options for the OData serializer. */
public final class PrimitiveSerializerOptions {

  /** Builder of OData serializer options. */
  public static final class Builder {

    private final PrimitiveSerializerOptions options;

    private Builder() {
      this.options = new PrimitiveSerializerOptions();
    }

    /** Builds the OData serializer options. */
    public PrimitiveSerializerOptions build() {
      return this.options;
    }

    /** Sets the {@link ContextURL}. */
    public Builder contextURL(final ContextURL contextURL) {
      this.options.contextURL = contextURL;
      return this;
    }

    /** Sets all facets from an EDM property. */
    public Builder facetsFrom(final EdmProperty property) {
      this.options.isNullable = property.isNullable();
      this.options.maxLength = property.getMaxLength();
      this.options.precision = property.getPrecision();
      this.options.scale = property.getScale();
      this.options.isUnicode = property.isUnicode();
      return this;
    }

    /** Sets the maxLength facet. */
    public Builder maxLength(final Integer maxLength) {
      this.options.maxLength = maxLength;
      return this;
    }

    /** Sets the nullable facet. */
    public Builder nullable(final Boolean isNullable) {
      this.options.isNullable = isNullable;
      return this;
    }

    /** Sets the precision facet. */
    public Builder precision(final Integer precision) {
      this.options.precision = precision;
      return this;
    }

    /** Sets the scale facet. */
    public Builder scale(final Integer scale) {
      this.options.scale = scale;
      return this;
    }

    /** Sets the unicode facet. */
    public Builder unicode(final Boolean isUnicode) {
      this.options.isUnicode = isUnicode;
      return this;
    }

    /** set the replacement string for xml 1.0 unicode controlled characters that are not allowed */
    public Builder xml10InvalidCharReplacement(final String replacement) {
      this.options.xml10InvalidCharReplacement = replacement;
      return this;
    }
  }

  /** Initializes the options builder. */
  public static Builder with() {
    return new Builder();
  }

  private ContextURL contextURL;

  private Boolean isNullable;

  private Integer maxLength;

  private Integer precision;

  private Integer scale;

  private Boolean isUnicode;

  private String xml10InvalidCharReplacement;

  private PrimitiveSerializerOptions() {
  }

  /** Gets the {@link ContextURL}. */
  public ContextURL getContextURL() {
    return this.contextURL;
  }

  /** Gets the maxLength facet. */
  public Integer getMaxLength() {
    return this.maxLength;
  }

  /** Gets the precision facet. */
  public Integer getPrecision() {
    return this.precision;
  }

  /** Gets the scale facet. */
  public Integer getScale() {
    return this.scale;
  }

  /** Gets the nullable facet. */
  public Boolean isNullable() {
    return this.isNullable;
  }

  /** Gets the unicode facet. */
  public Boolean isUnicode() {
    return this.isUnicode;
  }

  /** Gets the replacement string for unicode characters, that is not allowed in XML 1.0 */
  public String xml10InvalidCharReplacement() {
    return this.xml10InvalidCharReplacement;
  }
}
