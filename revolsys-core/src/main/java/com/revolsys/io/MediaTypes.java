package com.revolsys.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.file.Paths;
import com.revolsys.util.Property;

public class MediaTypes {

  private static boolean initialized;

  private static Map<String, String> mediaTypeByFileExtension = new HashMap<>();

  private static Map<String, String> fileExtensionByMediaType = new HashMap<>();

  public static String extension(final String contentType) {
    init();
    final String extension = fileExtensionByMediaType.get(contentType);
    if (Property.isEmpty(extension)) {
      return "bin";
    } else {
      return extension;
    }
  }

  /**
   * If the file extension is a registered extension then return that. Otherwise look up the
   * file extension by content type. Both the extension and contentType are converted to lower
   * case.
   *
   * @param extension
   * @param mediaType
   * @return
   */
  public static String extension(String extension, String mediaType) {
    init();
    if (Property.hasValue(extension)) {
      extension = extension.trim().toLowerCase();
    }

    if (mediaTypeByFileExtension.containsKey(extension)) {
      return extension;
    }

    if (Property.hasValue(mediaType)) {
      mediaType = mediaType.toLowerCase();
      final String result = fileExtensionByMediaType.get(mediaType);
      if (Property.hasValue(result)) {
        return result;
      }
    }

    if (Property.hasValue(extension)) {
      return extension;
    } else {
      return "bin";
    }
  }

  private static void init() {
    if (!initialized) {
      synchronized (MediaTypes.class) {
        if (!initialized) {
          initialized = true;
          try (
            final InputStream in = MediaTypes.class
              .getResourceAsStream("/com/revolsys/format/mediaTypes.tsv");
            Reader fileReader = new InputStreamReader(in);
            BufferedReader dataIn = new BufferedReader(fileReader);) {
            String line = dataIn.readLine();
            for (line = dataIn.readLine(); line != null; line = dataIn.readLine()) {
              final int tabIndex = line.indexOf('\t');
              final String fileExtension = line.substring(0, tabIndex)
                .toLowerCase()
                .trim()
                .intern();
              final String mediaType = line.substring(tabIndex + 1).toLowerCase().trim().intern();
              if (Property.hasValuesAll(fileExtension, mediaType)) {
                mediaTypeByFileExtension.put(fileExtension, mediaType);
                if (!fileExtensionByMediaType.containsKey(mediaType)) {
                  fileExtensionByMediaType.put(mediaType, fileExtension);
                }
              }
            }
          } catch (final IOException e) {
            Logs.error(MediaTypes.class, "Cannot read media types", e);
          }
        }
      }
    }
  }

  public static String mediaType(final Path file) {
    final String fileExtension = Paths.getFileNameExtension(file);
    return mediaType(fileExtension);
  }

  public static String mediaType(final String fileExtension) {
    init();
    if (fileExtension == null) {
      return "application/octet-stream";
    } else {
      String mediaType = mediaTypeByFileExtension.get(fileExtension);
      if (mediaType == null) {
        final String fileExtensionLower = fileExtension.toLowerCase();
        mediaType = mediaTypeByFileExtension.get(fileExtensionLower);
      }
      return mediaType;
    }
  }

  /**
   * If the file contentType is a registered contentType then return that. Otherwise look up the
   * file contentType by extension. Both the extension and contentType are converted to lower
   * case.
   *
   * @param extension
   * @param mediaType
   * @return
   */
  public static String mediaType(String mediaType, String extension) {
    init();
    if (Property.hasValue(mediaType)) {
      mediaType = mediaType.trim().toLowerCase();
    }

    if (fileExtensionByMediaType.containsKey(mediaType)) {
      return mediaType;
    }

    if (Property.hasValue(extension)) {
      extension = extension.toLowerCase();
      final String result = fileExtensionByMediaType.get(extension);
      if (Property.hasValue(result)) {
        return result;
      }
    }

    if (Property.hasValue(mediaType)) {
      return mediaType;
    } else {
      return "application/octet-stream";
    }
  }
}
