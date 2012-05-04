package com.revolsys.io;

import javax.xml.namespace.QName;

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

  public static String getPath(final String parent, final String name) {
    if (parent.startsWith("/")) {
      if (parent.endsWith("/")) {
        return parent + name;
      } else {
        return parent + "/" + name;
      }
    } else {
      if (parent.endsWith("/")) {
        return "/" + parent + name;
      } else {
        return "/" + parent + "/" + name;
      }
    }
  }

  private PathUtil() {
  }
}
