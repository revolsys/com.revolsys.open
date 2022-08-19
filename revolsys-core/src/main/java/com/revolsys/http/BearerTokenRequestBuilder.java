package com.revolsys.http;

import org.apache.http.client.methods.HttpUriRequest;

public class BearerTokenRequestBuilder extends ApacheHttpRequestBuilder {

  public BearerTokenRequestBuilder(final BearerTokenRequestBuilderFactory<?> factory) {
    super(factory);
  }

  @Override
  public HttpUriRequest build() {
    final String authorization = getFactory().getAuthorizationHeader();
    addHeader("Authorization", authorization);
    return super.build();
  }

  @Override
  public BearerTokenRequestBuilderFactory<?> getFactory() {
    return (BearerTokenRequestBuilderFactory<?>)super.getFactory();
  }

}
