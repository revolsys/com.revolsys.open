package com.revolsys.io;

import java.util.LinkedList;
import java.util.List;

import com.revolsys.util.Property;

public class PathName implements Comparable<PathName>, CharSequence {

  public static final PathName ROOT = new PathName("/");

  public static PathName newPathName(final Object path) {
    if (Property.hasValue(path)) {
      String pathString = path.toString();
      pathString = Path.clean(pathString);
      if ("/".equals(pathString)) {
        return ROOT;
      } else if (Property.hasValue(pathString)) {
        return new PathName(pathString);
      }
    }
    return null;
  }

  private final String name;

  private PathName parent;

  private final String path;

  private final String upperPath;

  protected PathName(final String path) {
    this.path = path;
    this.upperPath = path.toUpperCase();
    this.name = Path.getName(path);
  }

  @Override
  public char charAt(final int index) {
    return this.path.charAt(index);
  }

  @Override
  public int compareTo(final PathName pathName) {
    return getUpperPath().compareTo(pathName.getUpperPath());
  }

  public PathName createChild(final String name) {
    final String childPath = getPath() + "/" + name;
    return newPathName(childPath);
  }

  @Override
  public boolean equals(final Object object) {
    if (object == null) {
      return false;
    } else if (object == this) {
      return true;
    } else {
      final String upperPath = getUpperPath();
      String upperPath2;
      if (object instanceof PathName) {
        final PathName path = (PathName)object;
        upperPath2 = path.getUpperPath();
      } else {
        upperPath2 = object.toString().toUpperCase();
      }
      return upperPath.equals(upperPath2);
    }
  }

  public List<String> getElements() {
    final LinkedList<String> elements = new LinkedList<>();
    PathName currentPath = this;
    for (PathName parentPath = getParent(); parentPath != null; parentPath = parentPath
      .getParent()) {
      elements.addFirst(currentPath.getName());
      currentPath = parentPath;
    }
    return elements;
  }

  public String getName() {
    return this.name;
  }

  public PathName getParent() {
    if (this.parent == null && this.path.length() > 1) {
      final String parentPath = getParentPath();
      this.parent = newPathName(parentPath);
    }
    return this.parent;
  }

  public String getParentPath() {
    if (this.path == null) {
      return null;
    } else if (this.path.length() > 1) {
      final int index = this.path.lastIndexOf('/');
      if (index == 0) {
        return "/";
      } else {
        return this.path.substring(0, index);
      }
    } else {
      return null;
    }
  }

  public String getPath() {
    return this.path;
  }

  public List<PathName> getPaths() {
    final LinkedList<PathName> elements = new LinkedList<>();
    elements.add(this);
    for (PathName parentPath = getParent(); parentPath != null; parentPath = parentPath
      .getParent()) {
      elements.addFirst(parentPath);
    }
    return elements;
  }

  public String getUpperPath() {
    return this.upperPath;
  }

  @Override
  public int hashCode() {
    return getUpperPath().hashCode();
  }

  /**
   * Test if that this path is an ancestor of the other path.
   *
   * @param path The path to test.
   * @return True if this path is an ancestor of the other path.
   */
  public boolean isAncestorOf(final PathName path) {
    if (path != null) {
      for (PathName parentPath = path.getParent(); parentPath != null; parentPath = parentPath
        .getParent()) {
        if (equals(parentPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Test if that this path is a child of the other path.
   *
   * @param path The path to test.
   * @return True if this path is a child of the other path.
   */
  public boolean isChildOf(final PathName path) {
    if (path != null) {
      final PathName parent = getParent();
      return path.equals(parent);
    }
    return false;
  }

  /**
   * Test if that this path is an descendant of the other path.
   *
   * @param path The path to test.
   * @return True if this path is an descendant of the other path.
   */
  public boolean isDescendantOf(final PathName path) {
    if (path != null) {
      for (PathName parentPath = getParent(); parentPath != null; parentPath = parentPath
        .getParent()) {
        if (path.equals(parentPath)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Test if that this path is the parent of the other path.
   *
   * @param path The path to test.
   * @return True if this path is the parent of the other path.
   */
  public boolean isParentOf(final PathName path) {
    if (path != null) {
      final PathName otherParent = path.getParent();
      return equals(otherParent);
    }
    return false;
  }

  /**
   * Test if that this path is a sibling of the other path.
   *
   * @param path The path to test.
   * @return True if this path is a sibling of the other path.
   */
  public boolean isSiblingOf(final PathName path) {
    if (path != null) {
      final PathName parent1 = getParent();
      final PathName parent2 = path.getParent();
      return parent1.equals(parent2);
    }
    return false;
  }

  @Override
  public int length() {
    return this.path.length();
  }

  @Override
  public CharSequence subSequence(final int start, final int end) {
    return this.path.subSequence(start, end);
  }

  @Override
  public String toString() {
    return this.path;
  }
}
