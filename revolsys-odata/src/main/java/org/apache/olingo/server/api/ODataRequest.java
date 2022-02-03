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
package org.apache.olingo.server.api;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.FormatOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.apache.olingo.server.core.uri.queryoption.FormatOptionImpl;

/**
 * Request object to carry HTTP information optimized for and required to handle OData requests only.
 */
public class ODataRequest {
  private HttpMethod method;

  private final HttpHeaders headers = new HttpHeaders();

  private InputStream body;

  private String rawQueryPath;

  private String rawRequestUri;

  private String rawODataPath;

  private String rawBaseUri;

  private String rawServiceResolutionUri;

  private String protocol;

  private UriInfo uriInfo;

  /**
   * <p>Adds a header to the request.</p>
   * <p>The header name will be handled as case-insensitive key.</p>
   * <p>If a header already exists then the list of values will just be extended.</p>
   * @param name case-insensitive header name
   * @param values list of values for the given header name
   * @see <a href="http://ietf.org/rfc/rfc7230.txt">RFC 7230, section 3.2.2</a>
   */
  public void addHeader(final String name, final List<String> values) {
    this.headers.addHeader(name, values);
  }

  /**
   * <p>Adds a header to the request.</p>
   * <p>The header name will be handled as case-insensitive key.</p>
   * <p>If a header already exists then the list of values will just be extended.</p>
   * @param name case-insensitive header name
   * @param value value for the given header name
   * @see <a href="http://ietf.org/rfc/rfc7230.txt">RFC 7230, section 3.2.2</a>
   */
  public void addHeader(final String name, final String value) {
    this.headers.addHeader(name, value);
  }

  /**
   * Gets all headers.
   * @return an unmodifiable Map of header names/values
   */
  public Map<String, List<String>> getAllHeaders() {
    return this.headers.getHeaderToValues();
  }

  /**
   * Gets the body of the request.
   * @return the request payload as {@link InputStream} or null
   */
  public InputStream getBody() {
    return this.body;
  }

  /**
   * Extract format option from either <code>uriInfo</code> (if not <code>NULL</code>)
   * or query from <code>request</code> (if not <code>NULL</code>).
   * If both options are <code>NULL</code>, <code>NULL</code> is returned.
   *
   * @param request request which is checked
   * @param uriInfo uriInfo which is checked
   * @return the evaluated format option or <code>NULL</code>.
   */
  public FormatOption getFormatOption() {
    if (this.uriInfo == null) {
      final String query = getRawQueryPath();
      if (query == null) {
        return null;
      }

      final String formatOption = SystemQueryOptionKind.FORMAT.toString();
      final int index = query.indexOf(formatOption);
      int endIndex = query.indexOf('&', index);
      if (endIndex == -1) {
        endIndex = query.length();
      }
      String format = "";
      if (index + formatOption.length() < endIndex) {
        format = query.substring(index + formatOption.length(), endIndex);
      }
      return new FormatOptionImpl().setFormat(format);
    }
    return this.uriInfo.getFormatOption();
  }

  /**
   * Gets first header value for a given name.
   * @param name the header name as a case-insensitive key
   * @return the first header value or null if not found
   */
  public String getHeader(final String name) {
    final List<String> values = getHeaders(name);
    return values == null || values.isEmpty() ? null : values.get(0);
  }

  /**
   * Gets header values for a given name.
   * @param name the header name as a case-insensitive key
   * @return the header value(s) or null if not found
   */
  public List<String> getHeaders(final String name) {
    return this.headers.getHeader(name);
  }

  /**
   * Gets the HTTP method.
   * @return the HTTP method (GET, PUT, POST ...)
   */
  public HttpMethod getMethod() {
    return this.method;
  }

  /**
   * @return the protocol version used e.g. HTTP/1.1
   */
  public String getProtocol() {
    return this.protocol;
  }

  /**
   * Gets the base URI.
   * @return undecoded base URI, e.g., "<code>http://localhost/my%20service</code>"
   */
  public String getRawBaseUri() {
    return this.rawBaseUri;
  }

  /**
   * Gets the path segments of the request URI that belong to OData.
   * @return undecoded OData path segments, e.g., "/Employees"
   */
  public String getRawODataPath() {
    return this.rawODataPath;
  }

  /**
   * Gets the query part of the request URI.
   * @return the undecoded query options, e.g., "<code>$format=json,$top=10</code>"
   * @see <a href="http://ietf.org/rfc/rfc3986.txt">RFC 3986, section 3.4</a>
   */
  public String getRawQueryPath() {
    return this.rawQueryPath;
  }

  /**
   * Gets the total request URI.
   * @return undecoded request URI, e.g., "<code>http://localhost/my%20service/sys1/Employees?$format=json</code>"
   */
  public String getRawRequestUri() {
    return this.rawRequestUri;
  }

  /**
   * Gets the URI part responsible for service resolution.
   * @return undecoded path segments that do not belong to the OData URL schema or null, e.g., "<code>sys1</code>"
   */
  public String getRawServiceResolutionUri() {
    return this.rawServiceResolutionUri;
  }

  public UriInfo getUriInfo() {
    return this.uriInfo;
  }

  /**
   * Sets the body of the request.
   * @param body the request payload as {@link InputStream}
   */
  public void setBody(final InputStream body) {
    this.body = body;
  }

  /**
   * <p>Sets a header in the request.</p>
   * <p>The header name will be handled as case-insensitive key.</p>
   * <p>If a header already exists then the header will be replaced by this new value.</p>
   * @param name case-insensitive header name
   * @param value value for the given header name
   * @see <a href="http://ietf.org/rfc/rfc7230.txt">RFC 7230, section 3.2.2</a>
   */
  public void setHeader(final String name, final String value) {
    this.headers.setHeader(name, value);
  }

  /**
   * Sets the HTTP method.
   * @param method the HTTP method (GET, PUT, POST ...)
   */
  public void setMethod(final HttpMethod method) {
    this.method = method;
  }

  /**
   * Sets the HTTP protocol used
   * @param protocol
   * @see #getProtocol()
   */
  public void setProtocol(final String protocol) {
    this.protocol = protocol;
  }

  /**
   * Sets the base URI.
   * @see #getRawBaseUri()
   */
  public void setRawBaseUri(final String rawBaseUri) {
    this.rawBaseUri = rawBaseUri;
  }

  /**
   * Sets the path segments of the request URI that belong to OData.
   * @see #getRawODataPath()
   */
  public void setRawODataPath(final String rawODataPath) {
    this.rawODataPath = rawODataPath;
  }

  /**
   * Sets the query part of the request URI.
   * @see #getRawQueryPath()
   */
  public void setRawQueryPath(final String rawQueryPath) {
    this.rawQueryPath = rawQueryPath;
  }

  /**
   * Sets the total request URI.
   * @see #getRawRequestUri()
   */
  public void setRawRequestUri(final String rawRequestUri) {
    this.rawRequestUri = rawRequestUri;
  }

  /**
   * Sets the URI part responsible for service resolution.
   * @see #getRawServiceResolutionUri()
   */
  public void setRawServiceResolutionUri(final String rawServiceResolutionUri) {
    this.rawServiceResolutionUri = rawServiceResolutionUri;
  }

  public void setUriInfo(final UriInfo uriInfo) {
    this.uriInfo = uriInfo;
  }

}
