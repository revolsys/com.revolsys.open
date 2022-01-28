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
package org.apache.olingo.server.core.deserializer.batch;

import java.util.LinkedList;
import java.util.List;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.deserializer.batch.BatchDeserializerException;

public class BatchBodyPart implements BatchPart {
  private final String boundary;

  private final boolean isStrict;

  private final List<Line> remainingMessage = new LinkedList<>();

  private Header headers;

  private boolean isChangeSet;

  private List<BatchQueryOperation> requests;

  public BatchBodyPart(final List<Line> message, final String boundary, final boolean isStrict) {
    this.boundary = boundary;
    this.isStrict = isStrict;
    this.remainingMessage.addAll(message);
  }

  private List<BatchQueryOperation> consumeChangeSet(final List<Line> remainingMessage)
    throws BatchDeserializerException {
    final List<List<Line>> changeRequests = splitChangeSet(remainingMessage);
    final List<BatchQueryOperation> requestList = new LinkedList<>();

    for (final List<Line> changeRequest : changeRequests) {
      requestList.add(new BatchChangeSetPart(changeRequest, this.isStrict).parse());
    }

    return requestList;
  }

  private List<BatchQueryOperation> consumeQueryOperation(final List<Line> remainingMessage)
    throws BatchDeserializerException {
    final List<BatchQueryOperation> requestList = new LinkedList<>();
    requestList.add(new BatchQueryOperation(remainingMessage, this.isStrict).parse());

    return requestList;
  }

  private List<BatchQueryOperation> consumeRequest(final List<Line> remainingMessage)
    throws BatchDeserializerException {
    return this.isChangeSet ? consumeChangeSet(remainingMessage)
      : consumeQueryOperation(remainingMessage);
  }

  @Override
  public Header getHeaders() {
    return this.headers;
  }

  public List<BatchQueryOperation> getRequests() {
    return this.requests;
  }

  public boolean isChangeSet() {
    return this.isChangeSet;
  }

  private boolean isChangeSet(final Header headers) throws BatchDeserializerException {
    final List<String> contentTypes = headers.getHeaders(HttpHeader.CONTENT_TYPE);

    if (contentTypes.isEmpty()) {
      throw new BatchDeserializerException("Missing content type",
        BatchDeserializerException.MessageKeys.MISSING_CONTENT_TYPE,
        Integer.toString(headers.getLineNumber()));
    }

    boolean changeSet = false;
    for (final String contentType : contentTypes) {
      if (isContentTypeMultiPartMixed(contentType)) {
        changeSet = true;
      }
    }
    return changeSet;
  }

  private boolean isContentTypeMultiPartMixed(final String contentType) {
    try {
      BatchParserCommon.parseContentType(contentType, ContentType.MULTIPART_MIXED, 0);
      return true;
    } catch (final BatchDeserializerException e) {
      return false;
    }
  }

  @Override
  public boolean isStrict() {
    return this.isStrict;
  }

  public BatchBodyPart parse() throws BatchDeserializerException {
    this.headers = BatchParserCommon.consumeHeaders(this.remainingMessage);
    BatchParserCommon.consumeBlankLine(this.remainingMessage, this.isStrict);
    this.isChangeSet = isChangeSet(this.headers);
    this.requests = consumeRequest(this.remainingMessage);

    return this;
  }

  private List<List<Line>> splitChangeSet(final List<Line> remainingMessage)
    throws BatchDeserializerException {

    final HeaderField contentTypeField = this.headers.getHeaderField(HttpHeader.CONTENT_TYPE);
    final String changeSetBoundary = BatchParserCommon.getBoundary(contentTypeField.getValue(),
      contentTypeField.getLineNumber());
    validateChangeSetBoundary(changeSetBoundary, this.headers);

    return BatchParserCommon.splitMessageByBoundary(remainingMessage, changeSetBoundary);
  }

  private void validateChangeSetBoundary(final String changeSetBoundary, final Header header)
    throws BatchDeserializerException {
    if (changeSetBoundary.equals(this.boundary)) {
      throw new BatchDeserializerException(
        "Change set boundary is equals to batch request boundary",
        BatchDeserializerException.MessageKeys.INVALID_BOUNDARY,
        Integer.toString(header.getHeaderField(HttpHeader.CONTENT_TYPE).getLineNumber()));
    }
  }
}
