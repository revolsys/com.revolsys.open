package com.revolsys.io;

import com.revolsys.util.Property;

public class PathName {

  public static final PathName ROOT = new PathName("/");

  public static PathName create(String fullPath) {
    fullPath = Path.clean(fullPath);
    if ("/".equals(fullPath)) {
      return ROOT;
    } else if (Property.hasValue(fullPath)) {
      return new PathName(fullPath);
    } else {
      throw new NullPointerException("PathName cannot be null or empty");
    }
  }

  private final String name;

  private final String path;

  private final String upperPath;

  private PathName parent;

  protected PathName(final String path) {
    this.path = path;
    this.upperPath = path.toUpperCase();
    this.name = Path.getName(path);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof PathName) {
      final PathName path = (PathName)object;
      return path.getUpperPath().equals(getUpperPath());
    }
    return false;
  }

  public String getName() {
    return this.name;
  }

  public PathName getParent() {
    if (this.parent == null && this.path.length() > 1) {
      final String parentFullPath = Path.getPath(this.path);
      this.parent = create(parentFullPath);
    }
    return this.parent;
  }

  public String getPath() {
    return this.path;
  }

  public String getUpperPath() {
    return this.upperPath;
  }

  @Override
  public int hashCode() {
    return getUpperPath().hashCode();
  }

  /**
   * Test if that this is a child of the path.
   *
   * @param path The path to test.
   * @return True if this path is a child of the path.
   */
  public boolean isChild(final PathName path) {
    if (path != null) {
      return path.equals(getParent());
    }
    return false;
  }

  /**
   * Test if that this is the parent of the path.
   *
   * @param path The path to test.
   * @return True if this path is the parent of the path.
   */
  public boolean isParent(final PathName path) {
    if (path != null) {
      return equals(path.getParent());
    }
    return false;
  }

  @Override
  public String toString() {
    return this.path;
  }
}
