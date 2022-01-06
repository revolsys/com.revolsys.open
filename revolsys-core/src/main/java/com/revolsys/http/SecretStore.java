package com.revolsys.http;

import com.revolsys.io.map.ObjectFactoryConfig;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;

public interface SecretStore {

  static String getSecretValue(final ObjectFactoryConfig factoryConfig, final String secretName) {
    final SecretStore secretStore = factoryConfig.getValue("secretStore");
    if (secretStore != null) {
      return secretStore.getSecretValue(secretName);
    }
    return null;
  }

  static String getSecretValue(final ObjectFactoryConfig factoryConfig, final String secretName,
    final String propertyName) {
    final SecretStore secretStore = factoryConfig.getValue("secretStore");
    if (secretStore != null) {
      final String value = secretStore.getSecretValue(secretName);
      if (value != null && value.charAt(0) == '{') {
        final JsonObject json = JsonParser.read(value);
        return json.getString(propertyName);
      }
    }
    return null;
  }

  default ObjectFactoryConfig addTo(final ObjectFactoryConfig factoryConfig) {
    factoryConfig.addValue("secretStore", this);
    return factoryConfig;
  }

  String getSecretValue(String secretId);
}
