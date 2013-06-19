package com.revolsys.util;

import java.io.File;

import org.springframework.util.StringUtils;

import com.revolsys.io.FileUtil;

public class OperatingSystemUtil {

  public static final String OS_ARCH = System.getProperty("os.arch");

  public final static String OS_NAME = System.getProperty("os.name");

  public final static boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

  public final static boolean IS_SOLARIS = OS_NAME.equals("SunOS");

  public final static boolean IS_LINUX = OS_NAME.equals("Linux");

  public final static boolean IS_MAC = OS_NAME.contains("OS X")
    || OS_NAME.equals("Darwin");

  public static String getArch() {
    final String osArch = OS_ARCH.toLowerCase();
    if (osArch.equals("i386")) {
      return "x86";
    } else if (osArch.startsWith("amd64") || osArch.startsWith("x86_64")) {
      return "x86_64";
    } else if (osArch.equals("ppc")) {
      return "ppc";
    } else if (osArch.startsWith("ppc")) {
      return "ppc_64";
    } else if (osArch.startsWith("sparc")) {
      return "sparc";
    } else {
      return OS_ARCH;
    }
  }

  public static File getUserApplicationDataDirectory() {
    if (isWindows()) {
      final String appData = System.getenv("APPDATA");
      if (StringUtils.hasText(appData)) {
        final File directory = FileUtil.getFile(appData);
        if (directory.exists()) {
          return directory;
        }
      }
    } else if (isMac()) {
      final File directory = getUserDirectory("/Library/Application Support");
      if (directory.exists()) {
        return directory;
      }
    }
    final File directory = getUserDirectory(".config");
    if (!directory.exists()) {
      directory.mkdirs();
    }
    return directory;
  }

  public static File getUserApplicationDataDirectory(final String path) {
    final File directory = getUserApplicationDataDirectory();
    final File appDirectory = FileUtil.getFile(directory, path);
    if (!appDirectory.exists()) {
      appDirectory.mkdirs();
    }
    return appDirectory;
  }

  public static File getUserDirectory() {
    final String home = System.getProperty("user.home");
    return FileUtil.getFile(home);
  }

  public static File getUserDirectory(final String path) {
    final File userDirectory = getUserDirectory();
    return FileUtil.getFile(userDirectory, path);
  }

  public static boolean isMac() {
    return IS_MAC;
  }

  public static boolean isUnix() {
    return IS_SOLARIS || IS_LINUX;
  }

  public static boolean isWindows() {
    return IS_WINDOWS;
  }

}
