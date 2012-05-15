package com.revolsys.io;

import org.springframework.util.StringUtils;

public final class PathUtil {

  public static String getName(String path) {
    path = path.replaceAll("/+", "/");
    path = path.replaceAll("/$", "");

    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    final int index = path.lastIndexOf('/');
    return path.substring(index + 1);
  }

  public static String getPath(String path) {
    path = path.replaceAll("/+", "/");
    path = path.replaceAll("/$", "");

    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    final int index = path.lastIndexOf('/') - 1;
    if (index < 0) {
      return "/";
    } else {
      return path.substring(0, index + 1);
    }
  }

  public static String toPath(final String... parts) {
    if (parts.length == 0) {
      return "/";
    } else {
      StringBuffer path = new StringBuffer();
      for (String part : parts) {
        part = part.replaceAll("^/*", "");
        part = part.replaceAll("/*", "");
        if (StringUtils.hasText(part)) {
          path.append('/');
          path.append(part);
        }
      }
      return path.toString();
    }
  }

  private PathUtil() {
  }
}
