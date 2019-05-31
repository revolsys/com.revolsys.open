package com.revolsys.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.FileUtil;

public class ManifestUtil {

  public static String getImplementationVersion(final String implementationTitle) {
    final Manifest manifest = getManifestByImplementationTitle(implementationTitle);
    if (manifest != null) {
      return manifest.getMainAttributes().getValue(Name.IMPLEMENTATION_VERSION);
    } else {
      return "0.0.0";
    }

  }

  public static String getMainAttributeByImplementationTitle(final String implementationTitle,
    final String name) {
    final Manifest manifest = getManifestByImplementationTitle(implementationTitle);
    if (manifest != null) {
      return manifest.getMainAttributes().getValue(name);
    } else {
      return null;
    }

  }

  public static Manifest getManifestByImplementationTitle(final String implementationTitle) {
    try {
      final Enumeration resources = Thread.currentThread()
        .getContextClassLoader()
        .getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
        final URL url = (URL)resources.nextElement();

        final InputStream in = url.openStream();
        try {
          final Manifest manifest = new Manifest(in);
          final Attributes attrs = manifest.getMainAttributes();
          final String title = attrs.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
          if (implementationTitle.equals(title)) {
            return manifest;
          }
        } finally {
          FileUtil.closeSilent(in);
        }
      }
    } catch (final IOException e) {
      Logs.error(ManifestUtil.class, "Unable to get manifest for: " + implementationTitle, e);
    }
    return null;
  }
}
