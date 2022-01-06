package com.revolsys.http;

import org.apache.http.client.methods.RequestBuilder;

public class AzureTableSasRequestBuilder extends ApacheHttpRequestBuilder {

  public AzureTableSasRequestBuilder(final AzureTableSasRequestBuilderFactory factory,
    final RequestBuilder requestBuilder) {
    super(factory, requestBuilder);
  }

  @Override
  public AzureTableSasRequestBuilderFactory getFactory() {
    return (AzureTableSasRequestBuilderFactory)super.getFactory();
  }

  @Override
  protected void preBuild(final RequestBuilder builder) {
    getFactory().applyToken(builder);
  }

}
