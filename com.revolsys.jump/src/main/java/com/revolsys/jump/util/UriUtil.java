package com.revolsys.jump.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.ZipEntry;

public final class UriUtil {
  private UriUtil() {
  }

  /**
   * Create a URI to the entry within a ZIP file.
   * 
   * @param file The ZIP file.
   * @param entry The ZIP entry.
   * @return The URI.
   */
  public static URI getUri(final File file, final ZipEntry entry) {
    try {
      String entryName = entry.getName();
      URI fileUri = file.toURI();
      String filePath = fileUri.getPath();
      URI uri = new URI("zip", null, filePath + "!/" + entryName, null);
      return uri;
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Cannot create URI for " + file + "!/"
        + entry);
    }
  }

  /**
   * Get the ZIP file name from a ZIP URI.
   * 
   * @param uri The URI.
   * @return The ZIP file.
   */
  public static File getZipFile(final URI uri) {
    String path = uri.getPath();
    int index = path.indexOf('!');
    if (index == -1) {
      return new File(uri);
    } else {
      try {
        URI fileUri = new URI("file", null, path.substring(0, index), null);
        return new File(fileUri);
      } catch (URISyntaxException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  }

  /**
   * Get the name of a ZIP file entry from a ZIP URI.
   * 
   * @param uri The URI.
   * @return The ZIP entry name.
   */
  public static String getZipEntryName(final URI uri) {
    String path = uri.getPath();
    int index = path.indexOf('!');
    if (index == -1) {
      return null;
    } else {
      return path.substring(index + 2);
    }
  }

  /**
   * Get the file extension from the URI path.
   * 
   * @param uri The URI.
   * @return The file extension.
   */
  public static String getFileExtension(final URI uri) {
    String path = uri.getPath();
    int dotIndex = path.lastIndexOf('.');
    if (dotIndex != -1) {
      return path.substring(dotIndex + 1);
    } else {
      return "";
    }
  }

  /**
   * Get the file name from the URI path.
   * 
   * @param uri The URI.
   * @return The file name.
   */
  public static String getFileName(final URI uri) {
    String path = uri.getPath();
    int slashIndex = path.lastIndexOf('/');
    if (slashIndex != -1) {
      return path.substring(slashIndex + 1);
    } else {
      return "";
    }
  }

  /**
   * Get the file name without the extension from the URI path.
   * 
   * @param uri The URI.
   * @return The file name.
   */
  public static String getFileNameWithoutExtension(final URI uri) {
    String name = getFileName(uri);
    int dotIndex = name.lastIndexOf('.');
    if (dotIndex != -1) {
      return name.substring(0, dotIndex);
    } else {
      return "";
    }
  }

  public static String getZipEntryFilePath(final URI uri) {
    String name = getZipEntryName(uri);
    int slashIndex = name.lastIndexOf('/');
    if (slashIndex != -1) {
      return name.substring(0, slashIndex);
    } else {
      return null;
    }
  }
}
