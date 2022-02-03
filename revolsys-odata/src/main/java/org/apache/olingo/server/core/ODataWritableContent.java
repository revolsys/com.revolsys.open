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
package org.apache.olingo.server.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;

import org.apache.olingo.commons.api.data.EntityMediaObject;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.ODataContent;
import org.apache.olingo.server.api.ODataContentWriteErrorCallback;
import org.apache.olingo.server.api.ODataContentWriteErrorContext;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerStreamResult;
import org.apache.olingo.server.core.serializer.FixedFormatSerializerImpl;
import org.apache.olingo.server.core.serializer.SerializerStreamResultImpl;
import org.apache.olingo.server.core.serializer.json.ODataJsonSerializer;
import org.apache.olingo.server.core.serializer.xml.ODataXmlSerializer;

import com.revolsys.odata.model.ODataEntityIterator;

/**
 * Stream supporting implementation of the ODataContent
 * and contains the response content for the OData request.
 * <p/>
 * If an error occur during a <code>write</code> method <b>NO</b> exception
 * will be thrown but if registered the
 * org.apache.olingo.server.api.ODataContentWriteErrorCallback is called.
 */
public class ODataWritableContent implements ODataContent {
  public static class ODataWritableContentBuilder {
    private ODataSerializer serializer;

    private ODataEntityIterator entities;

    private ServiceMetadata metadata;

    private EdmEntityType entityType;

    private EntityCollectionSerializerOptions options;

    private FixedFormatSerializerImpl fixedFormatSerializer;

    private EntityMediaObject mediaEntity;

    public ODataWritableContentBuilder(final EntityMediaObject mediaEntity,
      final FixedFormatSerializerImpl fixedFormatSerializer) {
      this.mediaEntity = mediaEntity;
      this.fixedFormatSerializer = fixedFormatSerializer;
    }

    public ODataWritableContentBuilder(final ODataEntityIterator entities,
      final EdmEntityType entityType, final ODataSerializer serializer,
      final ServiceMetadata metadata, final EntityCollectionSerializerOptions options) {
      this.entities = entities;
      this.entityType = entityType;
      this.serializer = serializer;
      this.metadata = metadata;
      this.options = options;
    }

    public SerializerStreamResult build() {
      return SerializerStreamResultImpl.with().content(buildContent()).build();
    }

    public ODataContent buildContent() {
      if (this.serializer instanceof ODataJsonSerializer) {
        final StreamContent input = new StreamContentForJson(this.entities, this.entityType,
          (ODataJsonSerializer)this.serializer, this.metadata, this.options);
        return new ODataWritableContent(input);
      } else if (this.serializer instanceof ODataXmlSerializer) {
        final StreamContentForXml input = new StreamContentForXml(this.entities, this.entityType,
          (ODataXmlSerializer)this.serializer, this.metadata, this.options);
        return new ODataWritableContent(input);
      } else if (this.fixedFormatSerializer instanceof FixedFormatSerializerImpl) {
        final StreamContent input = new StreamContentForMedia(this.mediaEntity,
          this.fixedFormatSerializer);
        return new ODataWritableContent(input);
      }
      throw new ODataRuntimeException("No suitable serializer found");
    }
  }

  private static abstract class StreamContent {
    protected ODataEntityIterator iterator;

    protected ServiceMetadata metadata;

    protected EdmEntityType entityType;

    protected EntityCollectionSerializerOptions options;

    protected EntityMediaObject mediaEntity;

    public StreamContent(final EntityMediaObject mediaEntity) {
      this.mediaEntity = mediaEntity;
    }

    public StreamContent(final ODataEntityIterator iterator, final EdmEntityType entityType,
      final ServiceMetadata metadata, final EntityCollectionSerializerOptions options) {
      this.iterator = iterator;
      this.entityType = entityType;
      this.metadata = metadata;
      this.options = options;
    }

    public void write(final OutputStream out) {
      try {
        if (this.mediaEntity == null) {
          writeEntities(this.iterator, out);
        } else {
          writeBinary(this.mediaEntity, out);
        }
      } catch (final SerializerException e) {
        final ODataContentWriteErrorCallback errorCallback = this.options
          .getODataContentWriteErrorCallback();
        if (errorCallback != null) {
          final WriteErrorContext errorContext = new WriteErrorContext(e);
          errorCallback.handleError(errorContext, Channels.newChannel(out));
        }
      }
    }

    protected abstract void writeBinary(EntityMediaObject mediaEntity, OutputStream outputStream)
      throws SerializerException;

    protected abstract void writeEntities(ODataEntityIterator entity, OutputStream outputStream)
      throws SerializerException;
  }

  private static class StreamContentForJson extends StreamContent {
    private final ODataJsonSerializer jsonSerializer;

    public StreamContentForJson(final ODataEntityIterator iterator, final EdmEntityType entityType,
      final ODataJsonSerializer jsonSerializer, final ServiceMetadata metadata,
      final EntityCollectionSerializerOptions options) {
      super(iterator, entityType, metadata, options);

      this.jsonSerializer = jsonSerializer;
    }

    @Override
    protected void writeBinary(final EntityMediaObject mediaEntity, final OutputStream outputStream)
      throws SerializerException {
      throw new ODataRuntimeException("Not Implemented in Entity Handling");
    }

    @Override
    protected void writeEntities(final ODataEntityIterator entity, final OutputStream outputStream)
      throws SerializerException {
      try {
        this.jsonSerializer.writeRecords(this.metadata, this.entityType, entity, this.options,
          outputStream);
        outputStream.flush();
      } catch (final IOException e) {
        throw new ODataRuntimeException("Failed entity serialization", e);
      }
    }
  }

  private static class StreamContentForMedia extends StreamContent {
    private final FixedFormatSerializerImpl fixedFormatSerializer;

    public StreamContentForMedia(final EntityMediaObject mediaEntity,
      final FixedFormatSerializerImpl fixedFormatSerializer) {
      super(mediaEntity);

      this.fixedFormatSerializer = fixedFormatSerializer;
    }

    @Override
    protected void writeBinary(final EntityMediaObject mediaEntity, final OutputStream outputStream)
      throws SerializerException {
      this.fixedFormatSerializer.binaryIntoStreamed(mediaEntity, outputStream);
    }

    @Override
    protected void writeEntities(final ODataEntityIterator entity, final OutputStream outputStream)
      throws SerializerException {
      throw new ODataRuntimeException("Not Implemented in Entity Handling");
    }
  }

  private static class StreamContentForXml extends StreamContent {
    private final ODataXmlSerializer xmlSerializer;

    public StreamContentForXml(final ODataEntityIterator iterator, final EdmEntityType entityType,
      final ODataXmlSerializer xmlSerializer, final ServiceMetadata metadata,
      final EntityCollectionSerializerOptions options) {
      super(iterator, entityType, metadata, options);

      this.xmlSerializer = xmlSerializer;
    }

    @Override
    protected void writeBinary(final EntityMediaObject mediaEntity, final OutputStream outputStream)
      throws SerializerException {
      throw new ODataRuntimeException("Not Implemented in XML Handling");
    }

    @Override
    protected void writeEntities(final ODataEntityIterator entity, final OutputStream outputStream)
      throws SerializerException {
      try {
        this.xmlSerializer.entityCollectionIntoStream(this.metadata, this.entityType, entity,
          this.options, outputStream);
        outputStream.flush();
      } catch (final IOException e) {
        throw new ODataRuntimeException("Failed entity serialization", e);
      }
    }
  }

  public static class WriteErrorContext implements ODataContentWriteErrorContext {
    private final ODataLibraryException exception;

    public WriteErrorContext(final ODataLibraryException exception) {
      this.exception = exception;
    }

    @Override
    public Exception getException() {
      return this.exception;
    }

    @Override
    public ODataLibraryException getODataLibraryException() {
      return this.exception;
    }
  }

  public static ODataWritableContentBuilder with(final EntityMediaObject mediaEntity,
    final FixedFormatSerializerImpl fixedFormatSerializer) {
    return new ODataWritableContentBuilder(mediaEntity, fixedFormatSerializer);
  }

  public static ODataWritableContentBuilder with(final ODataEntityIterator iterator,
    final EdmEntityType entityType, final ODataSerializer serializer,
    final ServiceMetadata metadata, final EntityCollectionSerializerOptions options) {
    return new ODataWritableContentBuilder(iterator, entityType, serializer, metadata, options);
  }

  private final StreamContent streamContent;

  private ODataWritableContent(final StreamContent streamContent) {
    this.streamContent = streamContent;
  }

  @Override
  public void write(final OutputStream stream) {
    this.streamContent.write(stream);
  }
}
