package com.revolsys.http;

import org.apache.http.client.methods.RequestBuilder;

public class BearerTokenRequestBuilder extends ApacheHttpRequestBuilder {

  public BearerTokenRequestBuilder(final BearerTokenRequestBuilderFactory factory,
    final RequestBuilder requestBuilder) {
    super(factory, requestBuilder);
  }

  @Override
  public BearerTokenRequestBuilderFactory getFactory() {
    return (BearerTokenRequestBuilderFactory)super.getFactory();
  }

  @Override
  protected void preBuild(final RequestBuilder builder) {
    final String authorization = getFactory().getAuthorizationHeader();
    builder.addHeader("Authorization", authorization);
  }

}
