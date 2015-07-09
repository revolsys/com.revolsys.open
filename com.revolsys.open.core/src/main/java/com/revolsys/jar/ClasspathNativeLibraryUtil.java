package com.revolsys.jar;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.io.FileUtil;
import com.revolsys.util.OS;

public class ClasspathNativeLibraryUtil {

  private static final Map<String, Boolean> LIBRARY_LOADED_MAP = new HashMap<String, Boolean>();

  public static final Logger LOG = LoggerFactory.getLogger(ClasspathNativeLibraryUtil.class);

  public static String getLibraryExtension() {
    if (OS.IS_WINDOWS) {
      return "dll";
    } else if (OS.IS_MAC) {
      return "dylib";
    } else {
      return "so";
    }
  }

  public static String getLibraryPrefix() {
    if (OS.IS_WINDOWS) {
      return "";
    } else {
      return "lib";
    }
  }

  private static String getOperatingSystemName() {
    if (OS.IS_WINDOWS) {
      return "winnt";
    } else if (OS.IS_MAC) {
      return "macosx";
    } else if (OS.IS_LINUX) {
      return "linux";
    } else if (OS.IS_SOLARIS) {
      return "solaris";
    } else {
      return OS.OS_NAME;
    }
  }

  public static boolean loadLibrary(final String name) {
    synchronized (LIBRARY_LOADED_MAP) {
      final Boolean loaded = LIBRARY_LOADED_MAP.get(name);
      if (loaded == null) {
        final String prefix = getLibraryPrefix();
        final String ext = getLibraryExtension();
        final String arch = OS.getArch();
        final String operatingSystemName = getOperatingSystemName();
        return loadLibrary(prefix, name, arch, operatingSystemName, ext);
      } else {
        return loaded;
      }
    }
  }

  public static boolean loadLibrary(final String path, final String name) {
    final URL url = ClasspathNativeLibraryUtil.class.getResource(path);
    boolean loaded = false;
    if (url == null) {
      try {
        System.loadLibrary(name);
        loaded = true;
      } catch (final Throwable e) {
        LOG.debug("Unable to load shared library " + name, e);
      }
    } else {
      try {
        final File directory = FileUtil.createTempDirectory("jni", "name");
        final File file = new File(directory, name + ".dll");
        file.deleteOnExit();
        FileUtil.copy(url.openStream(), file);
        System.load(file.getCanonicalPath());
        loaded = true;
      } catch (final Throwable e) {
        LOG.error("Unable to load shared library from classpath " + url, e);
      }
    }
    LIBRARY_LOADED_MAP.put(name, loaded);
    return loaded;
  }

  private static boolean loadLibrary(final String prefix, final String name, final String arch,
    final String operatingSystemName, final String ext) {
    boolean loaded = false;
    final String fileName = prefix + name + "." + ext;
    final String libraryName = "/native/" + operatingSystemName + "/" + arch + "/" + fileName;
    final URL url = ClasspathNativeLibraryUtil.class.getResource(libraryName);
    if (url == null) {
      if (arch.equals("x86_64")) {
        loaded = loadLibrary(prefix, libraryName, "x86", operatingSystemName, ext);
      } else {
        try {
          System.loadLibrary(name);
          loaded = true;
        } catch (final Throwable e) {
          LOG.debug("Unable to load shared library from classpath " + libraryName + " " + fileName,
            e);
        }
      }
    } else {
      try {
        final File directory = FileUtil.createTempDirectory("jni", "name");
        final File file = new File(directory, fileName);
        file.deleteOnExit();
        FileUtil.copy(url.openStream(), file);
        System.load(file.getCanonicalPath());
        loaded = true;
      } catch (final Throwable e) {
        LOG.debug("Unable to load shared library from classpath " + libraryName + " " + fileName,
          e);
      }
    }
    LIBRARY_LOADED_MAP.put(name, loaded);
    return loaded;
  }

}
