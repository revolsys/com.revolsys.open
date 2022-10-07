package com.revolsys.http;

import com.revolsys.net.oauth.BearerToken;
import com.revolsys.record.io.format.json.JsonObject;

public class AzureManagedIdentityBearerToken extends BearerToken {

  public AzureManagedIdentityBearerToken(final JsonObject config, final String resource) {
    super(config, resource);
    final String returnedResource = config.getString("resource");
    setScope(resource, returnedResource);
  }

}
