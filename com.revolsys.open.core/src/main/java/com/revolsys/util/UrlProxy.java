package com.revolsys.util;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import com.revolsys.io.file.Paths;

public interface UrlProxy {

  URL getUrl();

  default URL getUrl(final Path path) {
    String childPath = Paths.getFileName(path);
    if (Files.isDirectory(path)) {
      childPath += "/";
    }
    return getUrl(childPath);
  }

  default URL getUrl(final String child) {
    final URL parentUrl = getUrl();
    return UrlUtil.getUrl(parentUrl, child);
  }
}
