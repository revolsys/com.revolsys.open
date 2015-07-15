package com.revolsys.io;

import java.util.ArrayList;
import java.util.List;

public class FileNames {

  public static String getBaseName(final String fileName) {
    int startIndex = fileName.lastIndexOf("/");
    if (startIndex == -1) {
      startIndex = 0;
    }
    final int dotIndex = fileName.lastIndexOf('.', startIndex);
    if (dotIndex != -1) {
      return fileName.substring(0, dotIndex);
    } else {
      return fileName;
    }
  }

  public static String getFileNameExtension(final String fileName) {
    final int dotIndex = fileName.lastIndexOf('.');
    if (dotIndex != -1) {
      final int startIndex = fileName.lastIndexOf("/");
      if (startIndex == -1) {
        return fileName.substring(dotIndex + 1);
      } else if (dotIndex > startIndex) {
        return fileName.substring(dotIndex + 1);
      }
    }
    return "";
  }

  public static List<String> getFileNameExtensions(final String fileName) {
    final List<String> extensions = new ArrayList<>();
    int startIndex = fileName.lastIndexOf("/");
    if (startIndex == -1) {
      startIndex = 0;
    }
    for (int dotIndex = fileName.indexOf('.', startIndex); dotIndex > 0; dotIndex = fileName
      .indexOf('.', startIndex)) {
      dotIndex++;
      final String extension = fileName.substring(dotIndex);
      extensions.add(extension);
      startIndex = dotIndex;
    }
    return extensions;
  }

}
