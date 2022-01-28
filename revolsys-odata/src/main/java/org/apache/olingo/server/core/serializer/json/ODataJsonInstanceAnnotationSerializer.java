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
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.IConstants;
import org.apache.olingo.commons.api.data.Annotation;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.Link;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.Valuable;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.core.edm.primitivetype.EdmPrimitiveTypeFactory;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.core.serializer.utils.ContentTypeHelper;

import com.revolsys.record.io.format.json.JsonWriter;

public class ODataJsonInstanceAnnotationSerializer {

  private final boolean isODataMetadataNone;

  private final boolean isODataMetadataFull;

  private final IConstants constants;

  private final boolean isIEEE754Compatible;

  public ODataJsonInstanceAnnotationSerializer(final ContentType contentType,
    final IConstants constants) {
    this.isIEEE754Compatible = ContentTypeHelper.isODataIEEE754Compatible(contentType);
    this.isODataMetadataNone = ContentTypeHelper.isODataMetadataNone(contentType);
    this.isODataMetadataFull = ContentTypeHelper.isODataMetadataFull(contentType);
    this.constants = constants;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  private void writeInstanceAnnotation(final JsonWriter json, final Valuable annotation,
    final String name) throws IOException, SerializerException, DecoderException {
    try {
      switch (annotation.getValueType()) {
        case PRIMITIVE:
          if (this.isODataMetadataFull && name.length() > 0) {
            json.labelValue(name + this.constants.getType(), "#" + annotation.getType());
          }
          if (name.length() > 0) {
            json.label(name);
          }
          writeInstanceAnnotOnPrimitiveProperty(json, annotation, annotation.getValue());
        break;
        case COLLECTION_PRIMITIVE:
          if (this.isODataMetadataFull && name.length() > 0) {
            json.labelValue(name + this.constants.getType(),
              "#Collection(" + annotation.getType() + ")");
          }
          if (name.length() > 0) {
            json.label(name);
          }
          json.startList();
          final List list = annotation.asCollection();
          for (final Object value : list) {
            writeInstanceAnnotOnPrimitiveProperty(json, annotation, value);
          }
          json.endList();
        break;
        case COMPLEX:
          if (this.isODataMetadataFull && name.length() > 0) {
            json.labelValue(name + this.constants.getType(), "#" + annotation.getType());
          }
          if (name.length() > 0) {
            json.label(name);
          }
          final ComplexValue complexValue = annotation.asComplex();
          writeInstanceAnnotOnComplexProperty(json, annotation, complexValue);
        break;
        case COLLECTION_COMPLEX:
          if (this.isODataMetadataFull && name.length() > 0) {
            json.labelValue(name + this.constants.getType(),
              "#Collection(" + annotation.getType() + ")");
          }
          if (name.length() > 0) {
            json.label(name);
          }
          json.startList();
          final List<ComplexValue> complexValues = (List<ComplexValue>)annotation.asCollection();
          for (final ComplexValue complxValue : complexValues) {
            writeInstanceAnnotOnComplexProperty(json, annotation, complxValue);
          }
          json.endList();
        break;
        default:
      }
    } catch (final EdmPrimitiveTypeException e) {
      throw new SerializerException("Wrong value for instance annotation!", e,
        SerializerException.MessageKeys.WRONG_PROPERTY_VALUE, ((Annotation)annotation).getTerm(),
        annotation.getValue().toString());
    }
  }

  /**
   * Write the instance annotation of an entity
   * @param annotations List of annotations
   * @param json JsonWriter
   * @throws IOException
   * @throws SerializerException
   * @throws DecoderException
   */
  public void writeInstanceAnnotationsOnEntity(final List<Annotation> annotations,
    final JsonWriter json) throws IOException, SerializerException, DecoderException {
    for (final Annotation annotation : annotations) {
      if (this.isODataMetadataFull) {
        json.labelValue(this.constants.getType(), "#" + annotation.getType());
      }
      json.label("@" + annotation.getTerm());
      writeInstanceAnnotation(json, annotation, "");
    }
  }

  /**
   * Write instance annotation of a property
   * @param edmProperty EdmProperty
   * @param property Property
   * @param json JsonWriter
   * @throws IOException
   * @throws SerializerException
   * @throws DecoderException
   */
  public void writeInstanceAnnotationsOnProperties(final EdmProperty edmProperty,
    final Property property, final JsonWriter json)
    throws IOException, SerializerException, DecoderException {
    if (property != null) {
      for (final Annotation annotation : property.getAnnotations()) {
        json.label(edmProperty.getName() + "@" + annotation.getTerm());
        writeInstanceAnnotation(json, annotation, "");
      }
    }
  }

  private void writeInstanceAnnotOnComplexProperty(final JsonWriter json, final Valuable annotation,
    final ComplexValue complexValue) throws IOException, SerializerException, DecoderException {
    json.startObject();
    if (this.isODataMetadataFull) {
      json.labelValue(this.constants.getType(), "#" + complexValue.getTypeName());
    }
    final List<Property> properties = complexValue.getValue();
    for (final Property prop : properties) {
      writeInstanceAnnotation(json, prop, prop.getName());
    }
    json.endObject();
  }

  private void writeInstanceAnnotOnPrimitiveProperty(final JsonWriter json,
    final Valuable annotation, final Object value) throws IOException, EdmPrimitiveTypeException {
    writePrimitiveValue("",
      EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.getByName(annotation.getType())),
      value, null, null, null, null, true, json);
  }

  protected void writePrimitiveValue(final String name, final EdmPrimitiveType type,
    final Object primitiveValue, final Boolean isNullable, final Integer maxLength,
    final Integer precision, final Integer scale, final Boolean isUnicode, final JsonWriter json)
    throws EdmPrimitiveTypeException, IOException {
    final String value = type.valueToString(primitiveValue, isNullable, maxLength, precision, scale,
      isUnicode);
    if (value == null) {
      json.writeNull();
    } else if (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Boolean)) {
      json.value(Boolean.parseBoolean(value));
    } else if (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Byte)
      || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Double)
      || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int16)
      || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int32)
      || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.SByte)
      || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Single)
      || (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Decimal)
        || type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Int64))
        && !this.isIEEE754Compatible) {
      json.label(value);
    } else if (type == EdmPrimitiveTypeFactory.getInstance(EdmPrimitiveTypeKind.Stream)) {
      if (primitiveValue instanceof Link) {
        final Link stream = (Link)primitiveValue;
        if (!this.isODataMetadataNone) {
          if (stream.getMediaETag() != null) {
            json.labelValue(name + this.constants.getMediaEtag(), stream.getMediaETag());
          }
          if (stream.getType() != null) {
            json.labelValue(name + this.constants.getMediaContentType(), stream.getType());
          }
        }
        if (this.isODataMetadataFull) {
          if (stream.getRel() != null && stream.getRel().equals(Constants.NS_MEDIA_READ_LINK_REL)) {
            json.labelValue(name + this.constants.getMediaReadLink(), stream.getHref());
          }
          if (stream.getRel() == null || stream.getRel().equals(Constants.NS_MEDIA_EDIT_LINK_REL)) {
            json.labelValue(name + this.constants.getMediaEditLink(), stream.getHref());
          }
        }
      }
    } else {
      json.value(value);
    }
  }
}
