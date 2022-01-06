package com.revolsys.http;

import java.util.function.Function;

import org.apache.http.client.methods.RequestBuilder;

import com.revolsys.net.http.ApacheHttp;
import com.revolsys.net.oauth.BearerToken;
import com.revolsys.record.io.format.json.JsonObject;

public class AzureManagedIdentityRequestBuilderFactory extends BearerTokenRequestBuilderFactory {
  public static final String ENDPOINT_URL = System.getenv("IDENTITY_ENDPOINT");

  public static final String IDENTITY_HEADER = System.getenv("IDENTITY_HEADER");

  private static boolean AVAILABLE = ENDPOINT_URL != null && IDENTITY_HEADER != null;

  public static boolean isAvailable() {
    return AVAILABLE;
  }

  public static final Function<BearerToken, BearerToken> tokenRefesh(final String resource) {
    return token -> {
      if (isAvailable()) {
        final RequestBuilder requestBuilder = RequestBuilder//
          .get(ENDPOINT_URL)
          .addHeader("X-IDENTITY-HEADER", IDENTITY_HEADER)
          .addParameter("resource", resource)
          .addParameter("api-version", "2019-08-01");
        final JsonObject response = ApacheHttp.getJson(requestBuilder);
        return new AzureManagedIdentityBearerToken(response, resource);
      } else {
        return null;
      }
    };
  }

  public AzureManagedIdentityRequestBuilderFactory(final String resource) {
    super(tokenRefesh(resource));
  }

}
