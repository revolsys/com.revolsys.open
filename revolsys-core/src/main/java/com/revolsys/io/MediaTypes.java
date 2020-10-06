package com.revolsys.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.jeometry.common.logging.Logs;

public class MediaTypes {

  private static boolean initialized;

  private static Map<String, String> mediaTypeByFileExtension = new HashMap<>();

  private static Map<String, String> fileExtensionByMediaType = new HashMap<>();

  public static String extension(final String contentType) {
    return fileExtensionByMediaType.getOrDefault(contentType, "bin");
  }

  public static String fromFileExtension(final String fileExtension) {
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
              final String fileExtension = line.substring(0, tabIndex).intern();
              final String mediaType = line.substring(tabIndex + 1).intern();
              mediaTypeByFileExtension.put(fileExtension, mediaType);
              if (!fileExtensionByMediaType.containsKey(mediaType)) {
                fileExtensionByMediaType.put(mediaType, fileExtension);
              }
            }
          } catch (final IOException e) {
            Logs.error(MediaTypes.class, "Cannot read media types", e);
          }
        }
      }
    }
  }
}
