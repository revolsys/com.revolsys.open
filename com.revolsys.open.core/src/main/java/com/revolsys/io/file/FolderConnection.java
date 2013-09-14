package com.revolsys.io.file;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;

public class FolderConnection implements MapSerializer {
  private final Map<String, Object> config = new LinkedHashMap<String, Object>();

  private String name;

  private FolderConnectionFile file;

  private FolderConnectionRegistry registry;

  public FolderConnection(final FolderConnectionRegistry registry,
    final String name, final File file) {
    this.registry = registry;
    setNameAndFile(name, file);
  }

  public void delete() {
    if (registry != null) {
      registry.removeConnection(this);
    }
    name = null;
    file = null;
    registry = null;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof FolderConnection) {
      final FolderConnection folderConnection = (FolderConnection)obj;
      if (registry == folderConnection.registry) {
        if (EqualsRegistry.equal(name, folderConnection.name)) {
          if (EqualsRegistry.equal(file.getFile(),
            folderConnection.file.getFile())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public FolderConnectionFile getFile() {
    return file;
  }

  public String getName() {
    return name;
  }

  public FolderConnectionRegistry getRegistry() {
    return registry;
  }

  @Override
  public int hashCode() {
    if (name == null) {
      return 0;
    } else {
      return name.hashCode();
    }
  }

  public boolean isReadOnly() {
    if (registry == null) {
      return true;
    } else {
      return registry.isReadOnly();
    }
  }

  public void setNameAndFile(final String name, final File file) {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null");
    }
    if (StringUtils.hasText(name)) {
      this.name = name;
    } else {
      this.name = file.getName();
      if (!StringUtils.hasText(this.name)) {
        this.name = "/";
      }
    }
    this.file = new FolderConnectionFile(this, file);
    this.config.put("type", "folderConnection");
    this.config.put("name", this.name);
    this.config.put("file", FileUtil.getCanonicalPath(file));
  }

  @Override
  public Map<String, Object> toMap() {
    return config;
  }

  @Override
  public String toString() {
    return name;
  }
}
