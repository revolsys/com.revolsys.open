package com.revolsys.http;

import java.io.InputStream;
import java.net.URI;

public interface ApacheHttpRequestBuilderFactoryProxy {

  default ApacheHttpRequestBuilder delete(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().delete(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder delete(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().delete(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder get(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().get(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder get(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().get(uri);
    return initRequestBuilder(requestBuilder);
  }

  default InputStream getInputStream(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = get(uri);
    return requestBuilder.newInputStream();
  }

  default InputStream getInputStream(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = get(uri);
    return requestBuilder.newInputStream();
  }

  ApacheHttpRequestBuilderFactory getRequestBuilderFactory();

  default ApacheHttpRequestBuilder head(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().head(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder head(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().head(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder initRequestBuilder(
    final ApacheHttpRequestBuilder requestBuilder) {
    return requestBuilder;
  }

  default ApacheHttpRequestBuilder post(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().post(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder post(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().post(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder put(final String uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().put(uri);
    return initRequestBuilder(requestBuilder);
  }

  default ApacheHttpRequestBuilder put(final URI uri) {
    final ApacheHttpRequestBuilder requestBuilder = getRequestBuilderFactory().put(uri);
    return initRequestBuilder(requestBuilder);
  }

}
