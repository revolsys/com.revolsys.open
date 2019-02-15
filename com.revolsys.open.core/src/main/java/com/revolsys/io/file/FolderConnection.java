package com.revolsys.io.file;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnection;
import com.revolsys.util.Property;

public class FolderConnection extends AbstractConnection<FolderConnection, FolderConnectionRegistry>
  implements Parent<Path> {
  private File file;

  private Path path;

  public FolderConnection(final FolderConnectionRegistry registry, final String name,
    final File file) {
    super(registry, name);
    setFile(file);
  }

  public FolderConnection(final FolderConnectionRegistry registry, final String name,
    final Path path) {
    super(registry, name);
    setFile(path.toFile());
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
    paths.sort((a, b) -> {
      final String name1 = a.getName(a.getNameCount() - 1).toString().toLowerCase();
      final String name2 = b.getName(b.getNameCount() - 1).toString().toLowerCase();
      return name1.compareTo(name2);
    });
    return paths;
  }

  public File getFile() {
    return this.file;
  }

  @Override
  public String getIconName() {
    return "folder";
  }

  public Path getPath() {
    return this.path;
  }

  public void setFile(final File file) {
    if (file == null) {
      throw new IllegalArgumentException("File must not be null");
    }
    if (!Property.hasValue(getName())) {
      final String fileName = FileUtil.getFileName(file);
      setName(fileName);
    }
    this.file = file;
    this.path = file.toPath();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addTypeToMap(map, "folderConnection");
    map.put("name", getName());
    map.put("file", FileUtil.getCanonicalPath(this.file));
    return map;
  }
}
