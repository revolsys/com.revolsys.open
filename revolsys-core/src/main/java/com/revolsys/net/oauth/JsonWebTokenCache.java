package com.revolsys.net.oauth;

import java.net.URI;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;

import org.jeometry.common.collection.map.LruMap;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.record.io.format.json.JsonParser;
import com.revolsys.util.Pair;

public class JsonWebTokenCache {

  private static Map<URI, Pair<Instant, JsonObject>> CACHE = new LruMap<>(100);

  public static JsonObject getJson(final URI uri) {
    final Instant now = Instant.now();
    Pair<Instant, JsonObject> resource = CACHE.get(uri);
    if (resource == null) {
      final JsonObject json = JsonParser.read(uri);
      final Instant expiry = now.plusSeconds(15 * 60);
      resource = new Pair<>(expiry, json);
    }

    for (final Iterator<Pair<Instant, JsonObject>> iterator = CACHE.values().iterator(); iterator
      .hasNext();) {
      final Pair<Instant, JsonObject> entry = iterator.next();
      if (entry.getValue1().isAfter(now)) {
        iterator.remove();
      }
    }
    return resource.getValue2();
  }

}
