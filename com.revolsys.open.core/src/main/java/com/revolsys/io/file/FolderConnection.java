package com.revolsys.io.file;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.io.connection.AbstractConnection;
import com.revolsys.util.Property;

public class FolderConnection extends AbstractConnection<FolderConnection, FolderConnectionRegistry>
  implements Parent<Path> {

  private final Path path;

  public FolderConnection(final FolderConnectionRegistry registry, final String name,
    final Path path) {
    super(registry, name);
    if (path == null) {
      throw new IllegalArgumentException("File must not be null");
    }
    if (!Property.hasValue(getName())) {
      final String fileName = Paths.getFileName(path);
      setName(fileName);
    }
    this.path = path.toAbsolutePath();
  }

  @Override
  public boolean equals(final Object obj) {
    if (super.equals(obj)) {
      if (DataType.equal(getFile(), ((FolderConnection)obj).getFile())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<Path> getChildren() {
    final List<Path> paths = Paths.getChildPaths(this.path);
    Collections.sort(paths);
    return paths;
  }

  public File getFile() {
    return this.path.toFile();
  }

  @Override
  public String getIconName() {
    return "folder";
  }

  public Path getPath() {
    return this.path;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addTypeToMap(map, "folderConnection");
    map.put("name", getName());
    map.put("file", this.path.toAbsolutePath().toString());
    return map;
  }
}
