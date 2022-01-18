package com.revolsys.odata.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.record.io.RecordStoreFactory;
import com.revolsys.record.schema.RecordStore;

/**
 * odata:[url]
 */
public class OData extends AbstractIoFactory implements RecordStoreFactory {

  public static final String URL_PREFIX = "odata:";

  private static final List<Pattern> URL_PATTERNS = Arrays
    .asList(Pattern.compile(URL_PREFIX + ".+"));

  public OData() {
    super("OData");
  }

  @Override
  public boolean canOpenUrl(final String url) {
    if (isAvailable()) {
      return url.startsWith(URL_PREFIX);
    }
    return false;
  }

  @Override
  public Class<? extends RecordStore> getRecordStoreInterfaceClass(
    final Map<String, ? extends Object> connectionProperties) {
    return RecordStore.class;
  }

  @Override
  public List<Pattern> getUrlPatterns() {
    return URL_PATTERNS;
  }

  @Override
  public ODataRecordStore newRecordStore(final MapEx connectionProperties) {
    return new ODataRecordStore(this, connectionProperties);
  }

  @Override
  public Map<String, Object> parseUrl(final String url) {
    if (url != null && url.startsWith(URL_PREFIX)) {
      final Map<String, Object> parameters = new LinkedHashMap<>();
      final String fileName = url.substring(URL_PREFIX.length());
      parameters.put("recordStoreType", getName());
      parameters.put("file", fileName);
    }
    return Collections.emptyMap();
  }

  @Override
  public String toString() {
    return getName();
  }

  @Override
  public String toUrl(final Map<String, Object> urlParameters) {
    final StringBuilder url = new StringBuilder(URL_PREFIX);
    final String file = Maps.getString(urlParameters, "serviceUrl");
    url.append(file);
    return url.toString().toLowerCase();
  }
}
