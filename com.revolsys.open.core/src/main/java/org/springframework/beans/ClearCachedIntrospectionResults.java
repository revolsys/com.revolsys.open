package org.springframework.beans;

public class ClearCachedIntrospectionResults {

  public static void clearCache() {
    synchronized (CachedIntrospectionResults.classCache) {
      CachedIntrospectionResults.classCache.clear();
    }
  }
}