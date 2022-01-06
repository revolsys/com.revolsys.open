package com.revolsys.http;

import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpRequest;
import org.apache.http.client.methods.RequestBuilder;

public class ApacheHttpRequestBuilderFactory {

  public ApacheHttpRequestBuilder copy(final HttpRequest request) {
    final RequestBuilder requestBuilder = RequestBuilder.copy(request);
    return newRequestBuilder(requestBuilder);
  }

  public ApacheHttpRequestBuilder create(final HttpMethod method, final String uri) {
    final RequestBuilder requestBuilder = RequestBuilder.create(method.name()).setUri(uri);
    return newRequestBuilder(requestBuilder);
  }

  public ApacheHttpRequestBuilder create(final HttpMethod method, final URI uri) {
    final RequestBuilder requestBuilder = RequestBuilder.create(method.name()).setUri(uri);
    return newRequestBuilder(requestBuilder);
  }

  public ApacheHttpRequestBuilder delete(final String uri) {
    return create(HttpMethod.DELETE, uri);
  }

  public ApacheHttpRequestBuilder delete(final URI uri) {
    return create(HttpMethod.DELETE, uri);
  }

  public ApacheHttpRequestBuilder get(final String uri) {
    return create(HttpMethod.GET, uri);
  }

  public ApacheHttpRequestBuilder get(final URI uri) {
    return create(HttpMethod.GET, uri);
  }

  public InputStream getInputStream(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = get(uri);
    return requestBuilder.newInputStream();
  }

  public InputStream getInputStream(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = get(uri);
    return requestBuilder.newInputStream();
  }

  public ApacheHttpRequestBuilder head(final String uri) {
    return create(HttpMethod.HEAD, uri);
  }

  public ApacheHttpRequestBuilder head(final URI uri) {
    return create(HttpMethod.HEAD, uri);
  }

  protected ApacheHttpRequestBuilder newRequestBuilder(final RequestBuilder requestBuilder) {
    return new ApacheHttpRequestBuilder(this, requestBuilder);
  }

  public ApacheHttpRequestBuilder post(final String uri) {
    return create(HttpMethod.POST, uri);
  }

  public ApacheHttpRequestBuilder post(final URI uri) {
    return create(HttpMethod.POST, uri);
  }

  public ApacheHttpRequestBuilder put(final String uri) {
    return create(HttpMethod.PUT, uri);
  }

  public ApacheHttpRequestBuilder put(final URI uri) {
    return create(HttpMethod.PUT, uri);
  }

}
