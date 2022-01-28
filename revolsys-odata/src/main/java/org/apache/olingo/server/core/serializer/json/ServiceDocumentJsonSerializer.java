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
package org.apache.olingo.server.core.serializer.json;

import java.io.IOException;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmFunctionImport;
import org.apache.olingo.commons.api.edm.EdmSingleton;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;

import com.revolsys.record.io.format.json.JsonWriter;

public class ServiceDocumentJsonSerializer {
  public static final String KIND = "kind";

  public static final String FUNCTION_IMPORT = "FunctionImport";

  public static final String SINGLETON = "Singleton";

  public static final String SERVICE_DOCUMENT = "ServiceDocument";

  private final ServiceMetadata metadata;

  private final String serviceRoot;

  private final boolean isODataMetadataNone;

  public ServiceDocumentJsonSerializer(final ServiceMetadata metadata, final String serviceRoot,
    final boolean isODataMetadataNone) throws SerializerException {
    if (metadata == null || metadata.getEdm() == null) {
      throw new SerializerException("Service Metadata and EDM must not be null for a service.",
        SerializerException.MessageKeys.NULL_METADATA_OR_EDM);
    }
    this.metadata = metadata;
    this.serviceRoot = serviceRoot;
    this.isODataMetadataNone = isODataMetadataNone;
  }

  private void writeElement(final JsonWriter gen, final String kind, final String reference,
    final String name, final String title) throws IOException {
    gen.startObject();
    gen.labelValue(Constants.JSON_NAME, name);
    if (title != null) {
      gen.labelValue(Constants.JSON_TITLE, title);
    }
    gen.labelValue(Constants.JSON_URL, reference);
    if (kind != null) {
      gen.labelValue(KIND, kind);
    }
    gen.endObject();
  }

  private void writeEntitySets(final JsonWriter gen, final EdmEntityContainer container)
    throws IOException {
    for (final EdmEntitySet edmEntitySet : container.getEntitySets()) {
      if (edmEntitySet.isIncludeInServiceDocument()) {
        writeElement(gen, null, edmEntitySet.getName(), edmEntitySet.getName(),
          edmEntitySet.getTitle());
      }
    }
  }

  private void writeFunctionImports(final JsonWriter gen, final EdmEntityContainer container)
    throws IOException {
    for (final EdmFunctionImport edmFI : container.getFunctionImports()) {
      if (edmFI.isIncludeInServiceDocument()) {
        writeElement(gen, FUNCTION_IMPORT, edmFI.getName(), edmFI.getName(), edmFI.getTitle());
      }
    }
  }

  public void writeServiceDocument(final JsonWriter gen) throws IOException {
    gen.startObject();

    if (!this.isODataMetadataNone) {
      final String metadataUri = (this.serviceRoot == null ? ""
        : this.serviceRoot.endsWith("/") ? this.serviceRoot : this.serviceRoot + "/")
        + Constants.METADATA;
      gen.labelValue(Constants.JSON_CONTEXT, metadataUri);

      if (this.metadata != null && this.metadata.getServiceMetadataETagSupport() != null
        && this.metadata.getServiceMetadataETagSupport().getMetadataETag() != null) {
        gen.labelValue(Constants.JSON_METADATA_ETAG,
          this.metadata.getServiceMetadataETagSupport().getMetadataETag());
      }
    }

    gen.label(Constants.VALUE);
    gen.startList();
    if (this.metadata != null) {
      final EdmEntityContainer container = this.metadata.getEdm().getEntityContainer();
      if (container != null) {
        writeEntitySets(gen, container);
        writeFunctionImports(gen, container);
        writeSingletons(gen, container);
      }
    }
  }

  private void writeSingletons(final JsonWriter gen, final EdmEntityContainer container)
    throws IOException {
    for (final EdmSingleton edmSingleton : container.getSingletons()) {
      writeElement(gen, SINGLETON, edmSingleton.getName(), edmSingleton.getName(),
        edmSingleton.getTitle());
    }
  }
}
