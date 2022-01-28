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
import java.util.Map;
import java.util.Map.Entry;

import org.apache.olingo.commons.api.Constants;
import org.apache.olingo.commons.api.ex.ODataError;
import org.apache.olingo.commons.api.ex.ODataErrorDetail;
import org.apache.olingo.server.api.serializer.SerializerException;

import com.revolsys.record.io.format.json.JsonWriter;

public class ODataErrorSerializer {

  public void writeErrorDocument(final JsonWriter json, final ODataError error)
    throws IOException, SerializerException {
    if (error == null) {
      throw new SerializerException("ODataError object MUST NOT be null!",
        SerializerException.MessageKeys.NULL_INPUT);
    }
    json.startObject();
    json.label(Constants.JSON_ERROR);

    json.startObject();
    writeODataError(json, error.getCode(), error.getMessage(), error.getTarget());
    writeODataAdditionalProperties(json, error.getAdditionalProperties());

    if (error.getDetails() != null) {
      json.label(Constants.ERROR_DETAILS);
      json.startObject();
      for (final ODataErrorDetail detail : error.getDetails()) {
        json.startObject();
        writeODataError(json, detail.getCode(), detail.getMessage(), detail.getTarget());
        writeODataAdditionalProperties(json, detail.getAdditionalProperties());
        json.endObject();
      }
      json.endList();
    }

    json.endObject();
    json.endObject();
  }

  @SuppressWarnings("unchecked")
  private void writeODataAdditionalProperties(final JsonWriter json,
    final Map<String, Object> additionalProperties) throws IOException {
    if (additionalProperties != null) {
      for (final Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
        final Object value = additionalProperty.getValue();
        if (value instanceof List) {
          final List<Map<String, Object>> list = (List<Map<String, Object>>)value;
          json.label(additionalProperty.getKey());
          json.startObject();
          for (final Map<String, Object> entry : list) {
            json.startObject();
            writeODataAdditionalProperties(json, entry);
            json.endObject();
          }
          json.endList();
        } else if (value instanceof Map) {
          writeODataAdditionalProperties(json, (Map<String, Object>)value);
        } else {
          json.labelValue(additionalProperty.getKey(), value);
        }
      }
    }
  }

  private void writeODataError(final JsonWriter json, final String code, final String message,
    final String target) throws IOException {
    json.label(Constants.ERROR_CODE);
    if (code == null) {
      json.writeNull();
    } else {
      json.value(code);
    }

    json.label(Constants.ERROR_MESSAGE);
    if (message == null) {
      json.writeNull();
    } else {
      json.value(message);
    }

    if (target != null) {
      json.labelValue(Constants.ERROR_TARGET, target);
    }
  }
}
