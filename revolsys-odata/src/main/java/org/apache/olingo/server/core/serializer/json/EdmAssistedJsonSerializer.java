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
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.IConstants;
import org.apache.olingo.commons.api.constants.Constantsv00;
import org.apache.olingo.commons.api.data.AbstractEntityCollection;
import org.apache.olingo.commons.api.data.AbstractODataObject;
import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.Annotation;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Linked;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.Valuable;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.core.edm.EdmTypeInfo;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EdmAssistedSerializer;
import org.apache.olingo.server.api.serializer.EdmAssistedSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerException.MessageKeys;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.core.serializer.SerializerResultImpl;
import org.apache.olingo.server.core.serializer.utils.CircleStreamBuffer;
import org.apache.olingo.server.core.serializer.utils.ContentTypeHelper;

import com.revolsys.record.io.format.json.JsonWriter;

public class EdmAssistedJsonSerializer implements EdmAssistedSerializer {

  private static final String IO_EXCEPTION_TEXT = "An I/O exception occurred.";

  protected final boolean isIEEE754Compatible;

  protected final boolean isODataMetadataNone;

  protected final boolean isODataMetadataFull;

  private final IConstants constants;

  public EdmAssistedJsonSerializer(final ContentType contentType) {
    this.isIEEE754Compatible = ContentTypeHelper.isODataIEEE754Compatible(contentType);
    this.isODataMetadataNone = ContentTypeHelper.isODataMetadataNone(contentType);
    this.isODataMetadataFull = ContentTypeHelper.isODataMetadataFull(contentType);
    this.constants = new Constantsv00();
  }

  public EdmAssistedJsonSerializer(final ContentType contentType, final IConstants constants) {
    this.isIEEE754Compatible = ContentTypeHelper.isODataIEEE754Compatible(contentType);
    this.isODataMetadataNone = ContentTypeHelper.isODataMetadataNone(contentType);
    this.isODataMetadataFull = ContentTypeHelper.isODataMetadataFull(contentType);
    this.constants = constants;
  }

  private void collection(final JsonWriter json, final EdmType itemType, final String typeName,
    final EdmProperty edmProperty, final ValueType valueType, final List<?> value)
    throws IOException, SerializerException {

    json.startList();

    for (final Object item : value) {
      switch (valueType) {
        case COLLECTION_PRIMITIVE:
          primitiveValue(json, (EdmPrimitiveType)itemType, typeName, edmProperty, item);
        break;

        case COLLECTION_GEOSPATIAL:
        case COLLECTION_ENUM:
          throw new SerializerException("Geo and enum types are not supported.",
            MessageKeys.NOT_IMPLEMENTED);

        case COLLECTION_COMPLEX:
          complexValue(json, (EdmComplexType)itemType, typeName, (ComplexValue)item);
        break;

        default:
      }
    }

    json.endList();
  }

  private void complexValue(final JsonWriter json, final EdmComplexType valueType,
    final String typeName, final ComplexValue value) throws IOException, SerializerException {
    json.startObject();

    if (typeName != null && this.isODataMetadataFull) {
      json.labelValue(this.constants.getType(), typeName);
    }

    for (final Property property : value.getValue()) {
      final String name = property.getName();
      final EdmProperty edmProperty = valueType == null
        || valueType.getStructuralProperty(name) == null ? null
          : valueType.getStructuralProperty(name);
      valuable(json, property, name, edmProperty == null ? null : edmProperty.getType(),
        edmProperty);
    }
    links(value, null, json);

    json.endObject();
  }

  private String constructTypeExpression(final String typeName) {
    final EdmTypeInfo typeInfo = new EdmTypeInfo.Builder().setTypeExpression(typeName).build();
    final StringBuilder stringBuilder = new StringBuilder();

    if (typeInfo.isCollection()) {
      stringBuilder.append("#Collection(");
    } else {
      stringBuilder.append('#');
    }

    stringBuilder.append(typeInfo.isPrimitiveType() ? typeInfo.getFullQualifiedName().getName()
      : typeInfo.getFullQualifiedName().getFullQualifiedNameAsString());

    if (typeInfo.isCollection()) {
      stringBuilder.append(')');
    }

    return stringBuilder.toString();
  }

  protected void doSerialize(final EdmEntityType entityType,
    final AbstractEntityCollection entityCollection, final String contextURLString,
    final String metadataETag, final JsonWriter json) throws IOException, SerializerException {

    json.startObject();

    metadata(contextURLString, metadataETag, null, null, entityCollection.getId(), false, json);

    if (entityCollection.getCount() != null) {
      if (this.isIEEE754Compatible) {
        json.labelValue(this.constants.getCount(), Integer.toString(entityCollection.getCount()));
      } else {
        json.labelValue(this.constants.getCount(), entityCollection.getCount());
      }
    }
    if (entityCollection.getDeltaLink() != null) {
      json.labelValue(this.constants.getDeltaLink(),
        entityCollection.getDeltaLink().toASCIIString());
    }

    for (final Annotation annotation : entityCollection.getAnnotations()) {
      valuable(json, annotation, '@' + annotation.getTerm(), null, null);
    }

    json.label(Constants.VALUE);
    json.startList();
    for (final Entity entity : entityCollection) {
      doSerialize(entityType, entity, null, null, json);
    }
    json.endList();

    if (entityCollection.getNext() != null) {
      json.labelValue(this.constants.getNextLink(), entityCollection.getNext().toASCIIString());
    }

    json.endObject();
  }

  protected void doSerialize(final EdmEntityType entityType, final Entity entity,
    final String contextURLString, final String metadataETag, final JsonWriter json)
    throws IOException, SerializerException {

    json.startObject();

    final String typeName = entity.getType() == null ? null
      : new EdmTypeInfo.Builder().setTypeExpression(entity.getType()).build().external();
    metadata(contextURLString, metadataETag, entity.getETag(), typeName, entity.getId(), true,
      json);

    for (final Annotation annotation : entity.getAnnotations()) {
      valuable(json, annotation, '@' + annotation.getTerm(), null, null);
    }

    for (final Property property : entity.getProperties()) {
      final String name = property.getName();
      final EdmProperty edmProperty = entityType == null
        || entityType.getStructuralProperty(name) == null ? null
          : entityType.getStructuralProperty(name);
      valuable(json, property, name, edmProperty == null ? null : edmProperty.getType(),
        edmProperty);
    }

    if (!this.isODataMetadataNone && entity.getEditLink() != null
      && entity.getEditLink().getHref() != null) {
      json.labelValue(this.constants.getEditLink(), entity.getEditLink().getHref());

      if (entity.isMediaEntity()) {
        json.labelValue(this.constants.getMediaReadLink(),
          entity.getEditLink().getHref() + "/$value");
      }
    }

    links(entity, entityType, json);

    json.endObject();
  }

  public SerializerResult entity(final ServiceMetadata metadata, final EdmEntityType entityType,
    final Entity entity, final EdmAssistedSerializerOptions options) throws SerializerException {
    return serialize(metadata, entityType, entity,
      options == null ? null : options.getContextURL());
  }

  @Override
  public SerializerResult entityCollection(final ServiceMetadata metadata,
    final EdmEntityType entityType, final AbstractEntityCollection entityCollection,
    final EdmAssistedSerializerOptions options) throws SerializerException {
    return serialize(metadata, entityType, entityCollection,
      options == null ? null : options.getContextURL());
  }

  private void links(final Linked linked, final EdmEntityType entityType, final JsonWriter json)
    throws IOException, SerializerException {

    for (final Link link : linked.getNavigationLinks()) {
      final String name = link.getTitle();
      for (final Annotation annotation : link.getAnnotations()) {
        valuable(json, annotation, name + '@' + annotation.getTerm(), null, null);
      }

      final EdmEntityType targetType = entityType == null || name == null
        || entityType.getNavigationProperty(name) == null ? null
          : entityType.getNavigationProperty(name).getType();
      if (link.getInlineEntity() != null) {
        json.label(name);
        doSerialize(targetType, link.getInlineEntity(), null, null, json);
      } else if (link.getInlineEntitySet() != null) {
        json.label(name);
        json.startList();
        for (final Entity subEntry : link.getInlineEntitySet().getEntities()) {
          doSerialize(targetType, subEntry, null, null, json);
        }
        json.endList();
      }
    }
  }

  private void metadata(final String contextURLString, final String metadataETag, final String eTag,
    final String type, final URI id, final boolean writeNullId, final JsonWriter json)
    throws IOException, SerializerException {
    if (!this.isODataMetadataNone) {
      if (contextURLString != null) {
        json.labelValue(this.constants.getContext(), contextURLString);
      }
      if (metadataETag != null) {
        json.labelValue(this.constants.getMetadataEtag(), metadataETag);
      }
      if (eTag != null) {
        json.labelValue(this.constants.getEtag(), eTag);
      }
      if (this.isODataMetadataFull) {
        if (type != null) {
          json.labelValue(this.constants.getType(), type);
        }
        if (id == null) {
          if (writeNullId) {
            json.labelValue(this.constants.getId(), null);
          }
        } else {
          json.labelValue(this.constants.getId(), id.toASCIIString());
        }
      }
    }
  }

  protected void primitiveValue(final JsonWriter json, final EdmPrimitiveType valueType,
    final String typeName, final EdmProperty edmProperty, final Object value)
    throws IOException, SerializerException {

    EdmPrimitiveType type = valueType;
    if (type == null) {
      final EdmPrimitiveTypeKind kind = typeName == null ? EdmTypeInfo.determineTypeKind(value)
        : new EdmTypeInfo.Builder().setTypeExpression(typeName).build().getPrimitiveTypeKind();
      type = kind == null ? null : EdmPrimitiveTypeFactory.getInstance(kind);
    }

    if (value == null) {
      json.writeNull();
    } else if (type == null) {
      throw new SerializerException("The primitive type could not be determined.",
        MessageKeys.INCONSISTENT_PROPERTY_TYPE, "");
    } else if (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Boolean)) {
      json.value(value);
    } else {
      String serialized = null;
      try {
        Integer scale = null;
        if (value instanceof BigDecimal) {
          scale = Math.max(0, ((BigDecimal)value).scale());
        } else {
          scale = Constants.DEFAULT_SCALE;
        }
        serialized = type.valueToString(value,
          edmProperty == null ? null : edmProperty.isNullable(),
          edmProperty == null ? null : edmProperty.getMaxLength(),
          edmProperty == null ? null : edmProperty.getPrecision(),
          edmProperty == null ? scale : edmProperty.getScale(),
          edmProperty == null ? null : edmProperty.isUnicode());
      } catch (final EdmPrimitiveTypeException e) {
        final String name = edmProperty == null ? "" : edmProperty.getName();
        throw new SerializerException("Wrong value for property '" + name + "'!", e,
          SerializerException.MessageKeys.WRONG_PROPERTY_VALUE, name, value.toString());
      }
      if (this.isIEEE754Compatible
        && (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int64)
          || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Decimal))
        || type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Byte)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.SByte)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Single)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Double)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int16)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int32)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int64)
          && type != EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Decimal)) {
        json.value(serialized);
      } else {
        json.value(serialized);
      }
    }
  }

  protected SerializerResult serialize(final ServiceMetadata metadata,
    final EdmEntityType entityType, final AbstractODataObject obj, final ContextURL contextURL)
    throws SerializerException {
    final String metadataETag = this.isODataMetadataNone || metadata == null
      || metadata.getServiceMetadataETagSupport() == null ? null
        : metadata.getServiceMetadataETagSupport().getMetadataETag();
    final String contextURLString = this.isODataMetadataNone || contextURL == null ? null
      : contextURL.toUriString();
    OutputStream outputStream = null;
    SerializerException cachedException = null;

    final CircleStreamBuffer buffer = new CircleStreamBuffer();
    outputStream = buffer.getOutputStream();
    try (
      JsonWriter json = new JsonWriter(outputStream, true)) {
      if (obj instanceof AbstractEntityCollection) {
        doSerialize(entityType, (AbstractEntityCollection)obj, contextURLString, metadataETag,
          json);
      } else if (obj instanceof Entity) {
        doSerialize(entityType, (Entity)obj, contextURLString, metadataETag, json);
      } else {
        throw new SerializerException("Input type not supported.", MessageKeys.NOT_IMPLEMENTED);
      }
      json.flush();
      json.close();
      return SerializerResultImpl.with().content(buffer.getInputStream()).build();
    } catch (final IOException e) {
      cachedException = new SerializerException(IO_EXCEPTION_TEXT, e,
        SerializerException.MessageKeys.IO_EXCEPTION);
      throw cachedException;
    } finally {
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (final IOException e) {
          throw cachedException == null
            ? new SerializerException(IO_EXCEPTION_TEXT, e,
              SerializerException.MessageKeys.IO_EXCEPTION)
            : cachedException;
        }
      }
    }
  }

  protected void valuable(final JsonWriter json, final Valuable valuable, final String name,
    final EdmType type, final EdmProperty edmProperty) throws IOException, SerializerException {

    if (this.isODataMetadataFull && !(valuable instanceof Annotation) && !valuable.isComplex()) {

      String typeName = valuable.getType();
      if (typeName == null && type == null && valuable.isPrimitive()) {
        if (valuable.isCollection()) {
          if (!valuable.asCollection().isEmpty()) {
            final EdmPrimitiveTypeKind kind = EdmTypeInfo
              .determineTypeKind(valuable.asCollection().get(0));
            if (kind != null) {
              typeName = "Collection(" + kind.getFullQualifiedName().getFullQualifiedNameAsString()
                + ')';
            }
          }
        } else {
          final EdmPrimitiveTypeKind kind = EdmTypeInfo.determineTypeKind(valuable.asPrimitive());
          if (kind != null) {
            typeName = kind.getFullQualifiedName().getFullQualifiedNameAsString();
          }
        }
      }

      if (typeName != null) {
        json.labelValue(name + this.constants.getType(), constructTypeExpression(typeName));
      }
    }

    for (final Annotation annotation : ((Annotatable)valuable).getAnnotations()) {
      valuable(json, annotation, name + '@' + annotation.getTerm(), null, null);
    }

    json.label(name);
    value(json, valuable, type, edmProperty);
  }

  private void value(final JsonWriter json, final Valuable value, final EdmType type,
    final EdmProperty edmProperty) throws IOException, SerializerException {
    final String typeName = value.getType() == null ? null
      : new EdmTypeInfo.Builder().setTypeExpression(value.getType()).build().external();

    if (value.isNull()) {
      json.writeNull();
    } else if (value.isCollection()) {
      collection(json, type, typeName, edmProperty, value.getValueType(), value.asCollection());
    } else if (value.isPrimitive()) {
      primitiveValue(json, (EdmPrimitiveType)type, typeName, edmProperty, value.asPrimitive());
    } else if (value.isComplex()) {
      complexValue(json, (EdmComplexType)type, typeName, value.asComplex());
    } else if (value.isEnum() || value.isGeospatial()) {
      throw new SerializerException("Geo and enum types are not supported.",
        MessageKeys.NOT_IMPLEMENTED);
    }
  }
}
