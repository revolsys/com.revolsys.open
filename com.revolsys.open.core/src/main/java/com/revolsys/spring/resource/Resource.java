package com.revolsys.spring.resource;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import com.revolsys.io.FileNames;

public interface Resource extends org.springframework.core.io.Resource {

  static org.springframework.core.io.Resource getResource(final Object source) {
    org.springframework.core.io.Resource resource;
    if (source instanceof org.springframework.core.io.Resource) {
      resource = (org.springframework.core.io.Resource)source;
    } else if (source instanceof Path) {
      resource = new PathResource((Path)source);
    } else if (source instanceof File) {
      resource = new FileSystemResource((File)source);
    } else if (source instanceof URL) {
      resource = new UrlResource((URL)source);
    } else if (source instanceof URI) {
      resource = new UrlResource((URI)source);
    } else if (source instanceof String) {
      return SpringUtil.getResource((String)source);
    } else {
      throw new IllegalArgumentException(source.getClass() + " is not supported");
    }
    return resource;
  }

  @Override
  Resource createRelative(String relativePath);

  default String getBaseName() {
    final String filename = getFilename();
    return FileNames.getBaseName(filename);
  }

  default String getFileNameExtension(final Resource resource) {
    final String filename = resource.getFilename();
    return FileNames.getFileNameExtension(filename);
  }

  default Resource getParent() {
    return null;
  }

  default Resource getResourceWithExtension(final String extension) {
    final String baseName = getBaseName();
    final String newFileName = baseName + "." + extension;
    final Resource parent = getParent();
    if (parent == null) {
      return null;
    } else {
      return parent.createRelative(newFileName);
    }
  }
}
