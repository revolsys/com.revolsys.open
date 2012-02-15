package com.revolsys.config;

import java.io.File;

public class ApplicationSupport {

  public static File getUserApplicationSupportDirectory() {
    String path;
    if (isWindows()) {
      path = System.getenv("APPDATA");
    } else if (isMac()) {
      path = System.getProperty("user.home") + "/Library/Application Support";
    } else {
      path = System.getProperty("user.home") + "/.config";
    }
    final File directory = new File(path);
    directory.mkdirs();
    return directory;
  }

  public static File getUserApplicationSupportDirectory(
    final String applicationName) {
    final File directory = new File(getUserApplicationSupportDirectory(),
      applicationName);
    directory.mkdirs();
    return directory;
  }

  private static boolean isMac() {
    final String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("mac") >= 0);
  }

  private static boolean isUnix() {
    final String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
  }

  private static boolean isWindows() {
    final String os = System.getProperty("os.name").toLowerCase();
    return (os.indexOf("win") >= 0);
  }
}
