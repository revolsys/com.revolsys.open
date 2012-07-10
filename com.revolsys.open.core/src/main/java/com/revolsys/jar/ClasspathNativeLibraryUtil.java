package com.revolsys.jar;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.io.FileUtil;

public class ClasspathNativeLibraryUtil {

  private static final String OS_ARCH = System.getProperty("os.arch");

  private static final Map<String, Boolean> LIBRARY_LOADED_MAP = new HashMap<String, Boolean>();

  private final static String OS_NAME = System.getProperty("os.name");

  public final static boolean IS_WINDOWS = OS_NAME.startsWith("Windows");

  public final static boolean IS_SOLARIS = OS_NAME.equals("SunOS");

  public final static boolean IS_LINUX = OS_NAME.equals("Linux");

  public final static boolean IS_DARWIN = OS_NAME.equals("Mac OS X")
    || OS_NAME.equals("Darwin");

  public static final Logger LOG = LoggerFactory.getLogger(ClasspathNativeLibraryUtil.class);

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

  public static String getLibraryExtension() {
    if (IS_WINDOWS) {
      return "dll";
    } else if (IS_DARWIN) {
      return "dylib";
    } else {
      return "so";
    }
  }

  public static String getLibraryPrefix() {
    if (IS_WINDOWS) {
      return "";
    } else {
      return "lib";
    }
  }

  private static String getOperatingSystemName() {
    if (IS_WINDOWS) {
      return "winnt";
    } else if (IS_DARWIN) {
      return "macosx";
    } else if (IS_LINUX) {
      return "linux";
    } else if (IS_SOLARIS) {
      return "solaris";
    } else {
      return OS_NAME;
    }
  }

  public static void loadLibrary(final String name) {
    synchronized (LIBRARY_LOADED_MAP) {
      final Boolean loaded = LIBRARY_LOADED_MAP.get(name);
      if (loaded == null) {
        final String prefix = getLibraryPrefix();
        final String ext = getLibraryExtension();
        final String arch = getArch();
        final String operatingSystemName = getOperatingSystemName();
        loadLibrary(prefix, name, arch, operatingSystemName, ext);
      } else if (!loaded) {
        throw new RuntimeException("Unable to load shared library " + name);
      }
    }
  }

  private static void loadLibrary(final String prefix, final String name,
    final String arch, final String operatingSystemName, final String ext) {
    final String fileName = prefix + name + "-" + arch + "-"
      + operatingSystemName + "." + ext;
    final String libraryName = "/native/" + fileName;
    final URL url = ClasspathNativeLibraryUtil.class.getResource(libraryName);
    if (url == null) {
      try {
        System.loadLibrary(name);
      } catch (final Throwable e) {
        if (arch.equals("x86_64")) {
          try {
            loadLibrary(prefix, libraryName, "x86", operatingSystemName, ext);
          } catch (RuntimeException t) {
            LOG.error("Unable to load shared library " + libraryName, t);
            LIBRARY_LOADED_MAP.put(name, Boolean.FALSE);
            throw new RuntimeException("Unable to load shared library "
              + fileName, t);

          }
        } else {
          LOG.error("Unable to load shared library " + libraryName, e);
          LIBRARY_LOADED_MAP.put(name, Boolean.FALSE);
          throw new RuntimeException("Unable to load shared library "
            + fileName, e);
        }
      }
    } else {
      try {
        final File directory = FileUtil.createTempDirectory("jni", "name");
        final File file = new File(directory, fileName);
        file.deleteOnExit();
        FileUtil.copy(url.openStream(), file);
        System.load(file.getCanonicalPath());
        LIBRARY_LOADED_MAP.put(name, Boolean.FALSE);
      } catch (final Throwable e) {
        LOG.error(
          "Unable to load shared library from classpath " + libraryName, e);
        LIBRARY_LOADED_MAP.put(name, Boolean.FALSE);
        throw new RuntimeException("Unable to load shared library " + fileName,
          e);
      }
    }
  }

}
