package com.revolsys.io;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.util.StringUtils;

public final class PathUtil {

  public static String getName(String path) {
    if (path == null) {
      return null;
    } else {
      path = path.replaceAll("/+", "/");
      path = path.replaceAll("/$", "");

      if (!path.startsWith("/")) {
        path = "/" + path;
      }
      final int index = path.lastIndexOf('/');
      return path.substring(index + 1);
    }
  }

  public static String getPath(String path) {
    if (path == null) {
      return null;
    } else {
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
  }

  public static List<String> getPathElements(final String path) {
    if (path == null) {
      return Collections.emptyList();
    } else if (path.equals("/")) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(path.replaceAll("^/*", "").split("/+"));
    }
  }

  public static String toPath(final String... parts) {
    if (parts.length == 0) {
      return "/";
    } else {
      final StringBuffer path = new StringBuffer();
      for (String part : parts) {
        if (part != null) {
          part = part.replaceAll("^/*", "");
          part = part.replaceAll("/*", "");
          if (StringUtils.hasText(part)) {
            path.append('/');
            path.append(part);
          }
        }
      }
      return path.toString();
    }
  }

  private PathUtil() {
  }
}
