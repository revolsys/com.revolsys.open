package com.revolsys.http;

import org.apache.http.client.methods.RequestBuilder;

public class ConfigurableRequestBuilder extends ApacheHttpRequestBuilder {

  public ConfigurableRequestBuilder(final ConfigurableRequestBuilderFactory factory,
    final RequestBuilder requestBuilder) {
    super(factory, requestBuilder);
  }

  @Override
  public ConfigurableRequestBuilderFactory getFactory() {
    return (ConfigurableRequestBuilderFactory)super.getFactory();
  }

  @Override
  protected void preBuild(final RequestBuilder builder) {
    getFactory().preBuild(builder);
  }

}
