package com.revolsys.ui.web.config;

public interface PageStore {
  /**
   * Get a page by the path name (eg subPage, /absolute/page or relativePage).
   *
   * @param path The path name
   * @return The page if found or null otherwise
   */
  Page getPage(String path);
}
