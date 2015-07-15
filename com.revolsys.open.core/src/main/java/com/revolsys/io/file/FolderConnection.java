package com.revolsys.io.file;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.Property;

public class FolderConnection implements MapSerializer {
  private final Map<String, Object> config = new LinkedHashMap<String, Object>();

  private String name;

  private File file;

  private Path path;

  private FolderConnectionRegistry registry;

  public FolderConnection(final FolderConnectionRegistry registry, final String name,
    final File file) {
    this.registry = registry;
    setNameAndFile(name, file);
  }

  public FolderConnection(final FolderConnectionRegistry registry, final String name,
    final Path path) {
    this.registry = registry;
    setNameAndFile(name, path.toFile());
  }

  public void delete() {
    if (this.registry != null) {
      this.registry.removeConnection(this);
    }
    this.registry = null;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof FolderConnection) {
      final FolderConnection folderConnection = (FolderConnection)obj;
      if (this.registry == folderConnection.registry) {
        if (EqualsRegistry.equal(this.name, folderConnection.name)) {
          if (EqualsRegistry.equal(getFile(), folderConnection.getFile())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public File getFile() {
    return this.file;
  }

  public String getName() {
    return this.name;
  }

  public Path getPath() {
    return this.path;
  }

  public FolderConnectionRegistry getRegistry() {
    return this.registry;
  }

  @Override
  public int hashCode() {
    if (this.name == null) {
      return 0;
    } else {
      return this.name.hashCode();
    }
  }

  public boolean isReadOnly() {
    if (this.registry == null) {
      return true;
    } else {
      return this.registry.isReadOnly();
    }
  }

  public void setNameAndFile(final String name, final File file) {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null");
    }
    if (Property.hasValue(name)) {
      this.name = name;
    } else {
      this.name = FileUtil.getFileName(file);
    }
    this.file = file;
    this.path = file.toPath();
    this.config.put("type", "folderConnection");
    this.config.put("name", this.name);
    this.config.put("file", FileUtil.getCanonicalPath(file));
  }

  @Override
  public Map<String, Object> toMap() {
    return this.config;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
