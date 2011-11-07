package com.revolsys.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractIoFactory implements IoFactory {

  private final List<String> fileExtensions = new ArrayList<String>();

  private final Map<String, Set<String>> fileExtensionToMediaType = new HashMap<String, Set<String>>();

  private final Set<String> mediaTypes = new HashSet<String>();

  private final Map<String, Set<String>> mediaTypeToFileExtension = new HashMap<String, Set<String>>();

  private final String name;

  public AbstractIoFactory(
    final String name) {
    this.name = name;
  }

  private void add(
    final Map<String, Set<String>> mapSet,
    final String key,
    final String value) {
    Set<String> set = mapSet.get(key);
    if (set == null) {
      set = new LinkedHashSet<String>();
      mapSet.put(key, set);
    }
    set.add(value);
  }

  protected void addMediaTypeAndFileExtension(
    final String mediaType,
    final String fileExtension) {
    mediaTypes.add(mediaType);
    fileExtensions.add(fileExtension);
    add(mediaTypeToFileExtension, mediaType, fileExtension);
    add(fileExtensionToMediaType, fileExtension, mediaType);
  }

  public String getFileExtension(
    final String mediaType) {
    final Set<String> fileExtensions = mediaTypeToFileExtension.get(mediaType);
    if (fileExtensions == null) {
      return null;
    } else {
      final Iterator<String> iterator = fileExtensions.iterator();
      if (iterator.hasNext()) {
        final String fileExtension = iterator.next();
        return fileExtension;
      } else {
        return null;
      }
    }
  }

  public List<String> getFileExtensions() {
    return fileExtensions;
  }

  public String getMediaType(
    final String fileExtension) {
    final Set<String> mediaTypes = fileExtensionToMediaType.get(fileExtension);
    if (mediaTypes == null) {
      return null;
    } else {
      final Iterator<String> iterator = mediaTypes.iterator();
      if (iterator.hasNext()) {
        final String mediaType = iterator.next();
        return mediaType;
      } else {
        return null;
      }
    }
  }

  public Set<String> getMediaTypes() {
    return mediaTypes;
  }

  public String getName() {
    return name;
  }

}
