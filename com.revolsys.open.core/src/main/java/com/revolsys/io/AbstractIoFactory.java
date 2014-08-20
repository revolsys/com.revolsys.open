package com.revolsys.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

public abstract class AbstractIoFactory implements IoFactory {

  private final List<String> fileExtensions = new ArrayList<String>();

  private final Map<String, Set<String>> fileExtensionToMediaType = new HashMap<String, Set<String>>();

  private final Set<String> mediaTypes = new HashSet<String>();

  private final Map<String, Set<String>> mediaTypeToFileExtension = new HashMap<String, Set<String>>();

  private final String name;

  public AbstractIoFactory(final String name) {
    this.name = name;
  }

  private void add(final Map<String, Set<String>> mapSet, final String key,
    final String value) {
    Set<String> set = mapSet.get(key);
    if (set == null) {
      set = new LinkedHashSet<String>();
      mapSet.put(key, set);
    }
    set.add(value);
  }

  protected void addMediaType(final String mediaType) {
    this.mediaTypes.add(mediaType);
  }

  protected void addMediaTypeAndFileExtension(final String mediaType,
    final String fileExtension) {
    addMediaType(mediaType);
    this.fileExtensions.add(fileExtension);
    add(this.mediaTypeToFileExtension, mediaType, fileExtension);
    add(this.mediaTypeToFileExtension, fileExtension, fileExtension);
    add(this.fileExtensionToMediaType, fileExtension, mediaType);
    add(this.fileExtensionToMediaType, mediaType, mediaType);
  }

  @Override
  public String getFileExtension(final String mediaType) {
    final Set<String> fileExtensions = this.mediaTypeToFileExtension.get(mediaType);
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

  @Override
  public List<String> getFileExtensions() {
    return this.fileExtensions;
  }

  @Override
  public String getMediaType(final String fileExtension) {
    final Set<String> mediaTypes = this.fileExtensionToMediaType.get(fileExtension);
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

  @Override
  public Set<String> getMediaTypes() {
    return this.mediaTypes;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @PostConstruct
  public void init() {
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @Override
  public String toString() {
    return getName();
  }
}
