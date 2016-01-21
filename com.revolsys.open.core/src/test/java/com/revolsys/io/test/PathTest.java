package com.revolsys.io.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.io.PathUtil;
import com.revolsys.io.PathName;
import com.revolsys.util.Property;

public class PathTest {

  private void assertAncestor(final String parentPath, final String childPath,
    final boolean expected) {
    final boolean ancestor = PathUtil.isAncestor(parentPath, childPath);
    Assert.assertEquals(expected, ancestor);
  }

  private void assertAncestorOrDescendant(final String path1, final String path2,
    final boolean expectedAncestor, final boolean expectedDescendant) {
    {
      final PathName pathName1 = PathName.newPathName(path1);
      final PathName pathName2 = PathName.newPathName(path2);

      if (pathName1 != null) {
        final boolean isAncestor2 = pathName1.isAncestorOf(pathName2);
        Assert.assertEquals(expectedAncestor, isAncestor2);
        final boolean isDescendant12 = pathName1.isDescendantOf(pathName2);
        Assert.assertEquals(expectedDescendant, isDescendant12);
      }

      if (expectedAncestor != expectedDescendant) {
        final boolean isAncestor21 = pathName2.isAncestorOf(pathName1);
        Assert.assertEquals(!expectedAncestor, isAncestor21);

        final boolean isDescendant21 = pathName2.isDescendantOf(pathName1);
        Assert.assertEquals(expectedAncestor, isDescendant21);
      }
    }
  }

  private void assertChildName(final String parentPath, final String childPath,
    final String expected) {
    final String childName = PathUtil.getChildName(parentPath, childPath);
    Assert.assertEquals(expected, childName);
  }

  private void assertChildPath(final String parentPath, final String childPath,
    final String expected) {
    final String childName = PathUtil.getChildPath(parentPath, childPath);
    Assert.assertEquals(expected, childName);

    final PathName pathName1 = PathName.newPathName(parentPath);
    final PathName pathName2 = PathName.newPathName(childPath);
    if (pathName1 != null) {
      final PathName childPathName = pathName1.getChild(pathName2);
      if (expected == null) {
        Assert.assertNull("getChildPath", childPathName);
      } else {
        Assert.assertEquals("getChildPath " + pathName1 + ", " + pathName2,
          PathName.newPathName(expected), childPathName);

      }
    }
  }

  private void assertClean(final String source, final String expected) {
    final String cleaned = PathUtil.clean(source);
    Assert.assertEquals(expected, cleaned);

    final String cleanedUpper = PathUtil.cleanUpper(source);
    String expectedUpper;
    if (expected == null) {
      expectedUpper = null;
    } else {
      expectedUpper = expected.toUpperCase();
    }
    Assert.assertEquals(expectedUpper, cleanedUpper);
  }

  private void assertElements(final String path, final String... expected) {
    final List<String> paths = PathName.newPathName(path).getElements();
    Assert.assertEquals(Arrays.asList(expected), paths);
  }

  private void assertName(final String source, final String expected) {
    final String name = PathUtil.getName(source);
    Assert.assertEquals(expected, name);
  }

  private void assertParent(final String parentPath, final String childPath,
    final boolean expectedParent, final boolean expectedChild) {
    {
      final boolean parent = PathUtil.isParent(parentPath, childPath);
      Assert.assertEquals(expectedParent, parent);
    }
    {
      final PathName path1 = PathName.newPathName(parentPath);
      final PathName path2 = PathName.newPathName(childPath);
      if (path1 != null) {
        final boolean isParent12 = path1.isParentOf(path2);
        Assert.assertEquals(expectedParent, isParent12);

        final boolean isChild12 = path1.isChildOf(path2);
        Assert.assertEquals(expectedChild, isChild12);
      }
      if (expectedParent != expectedChild) {
        final boolean isParent21 = path2.isParentOf(path1);
        Assert.assertEquals(!expectedParent, isParent21);

        final boolean isChild21 = path2.isChildOf(path1);
        Assert.assertEquals(expectedParent, isChild21);
      }
    }
  }

  private void assertPath(final String source, final String expected) {
    final String path = PathUtil.getPath(source);
    Assert.assertEquals(expected, path);
    if (Property.hasValue(source)) {
      final PathName parent = PathName.newPathName(source).getParent();
      if (parent != null) {
        Assert.assertEquals(expected, parent.toString());
      }
    }
  }

  private void assertPaths(final String path, final String... expected) {
    final List<String> paths = PathUtil.getPaths(path);
    Assert.assertEquals(Arrays.asList(expected), paths);
  }

  @Test
  public void testAncestor() {
    assertAncestor(null, null, false);
    assertAncestor("", null, false);
    assertAncestor(null, "", false);
    assertAncestor("/test", null, false);
    assertAncestor(null, "/test", false);
    assertAncestor("/", "", false);
    assertAncestor("/", "/", false);
    assertAncestor("", "/", false);
    assertAncestor("/", "/test", true);
    assertAncestor("/", "/test/aaa", true);
    assertAncestor("/test", "/test/aaa", true);
    assertAncestor("/test/aaa", "/test/aaa", false);
    assertAncestor("/test/aaa", "/test", false);
    assertAncestor("/test/aaa", "/test/aaa/bbb", true);
    assertAncestor("/test/", "/test/aaa/bbb", true);
  }

  @Test
  public void testAncestorOrDescendant() {
    assertAncestorOrDescendant("/test", null, false, false);
    assertAncestorOrDescendant("/", "", false, false);
    assertAncestorOrDescendant("/", "/", false, false);
    assertAncestorOrDescendant("", "/", false, false);
    assertAncestorOrDescendant("/", "/test", true, false);
    assertAncestorOrDescendant("/", "/test/aaa", true, false);
    assertAncestorOrDescendant("/test", "/test/aaa", true, false);
    assertAncestorOrDescendant("/test/aaa", "/test/aaa", false, false);
    assertAncestorOrDescendant("/test/aaa", "/test", false, true);
    assertAncestorOrDescendant("/test/aaa", "/test/aaa/bbb", true, false);
    assertAncestorOrDescendant("/test/", "/test/aaa/bbb", true, false);
  }

  @Test
  public void testChildName() {
    assertChildName(null, null, null);
    assertChildName("", null, null);
    assertChildName(null, "", null);
    assertChildName("/test", null, null);
    assertChildName(null, "/test", null);
    assertChildName("/test", "/test/", null);
    assertChildName("/", "", null);
    assertChildName("/", "/", null);
    assertChildName("/", "/test", "test");
    assertChildName("", "/", null);
    assertChildName("/test", "/test/aaa", "aaa");
    assertChildName("/test/aaa", "/test/aaa", null);
    assertChildName("/test/aaa", "/test", null);
    assertChildName("/test/aaa", "/test/aaa/bbb", "bbb");
    assertChildName("/test/", "/test/aaa/bbb", "aaa");
  }

  @Test
  public void testChildPath() {
    assertChildPath(null, null, null);
    assertChildPath("", null, null);
    assertChildPath(null, "", null);
    assertChildPath("/test", null, null);
    assertChildPath(null, "/test", null);
    assertChildPath("/test", "/test/", null);
    assertChildPath("/", "", null);
    assertChildPath("/", "/", null);
    assertChildPath("/", "/test", "/test");
    assertChildPath("", "/", null);
    assertChildPath("/test", "/test/aaa", "/test/aaa");
    assertChildPath("/test/aaa", "/test/aaa", null);
    assertChildPath("/test/aaa", "/test", null);
    assertChildPath("/test/aaa", "/test/aaa/bbb", "/test/aaa/bbb");
    assertChildPath("/test/", "/test/aaa/bbb", "/test/aaa");
  }

  @Test
  public void testClean() {
    assertClean(null, null);
    assertClean("", "/");
    assertClean("//", "/");
    assertClean(" \t//", "/");
    assertClean("\\/", "/");
    assertClean("/test/", "/test");
    assertClean(" \t\n\r/test/\\ /\\\t", "/test");
    assertClean("/test/", "/test");
    assertClean("/test/a/bbbbb", "/test/a/bbbbb");
    assertClean("/test /\\a\\/bbbbb", "/test /a/bbbbb");
  }

  @Test
  public void testElements() {
    assertElements("/");
    assertElements("/t", "t");
    assertElements("/test", "test");
    assertElements("/test/aaa", "test", "aaa");
  }

  @Test
  public void testName() {
    assertName(null, null);
    assertName("", "/");
    assertName("//", "/");
    assertName(" \t//", "/");
    assertName("\\/", "/");
    assertName("/test/", "test");
    assertName(" \t\n\r/test/\\ /\\\t", "test");
    assertName("/test/", "test");
    assertName("/test/aaaa", "aaaa");
    assertName("/test /\\a\\/bbbbb", "bbbbb");
  }

  @Test
  public void testParent() {
    assertParent("/", null, false, false);
    assertParent("/test", null, false, false);
    assertParent("/", "", false, false);
    assertParent("/", "/", false, false);
    assertParent("", "/", false, false);
    assertParent("/test", "/test/aaa", true, false);
    assertParent("/test/aaa", "/test/aaa", false, false);
    assertParent("/test/aaa", "/test", false, true);
    assertParent("/test/aaa", "/test/aaa/bbb", true, false);
    assertParent("/test/", "/test/aaa/bbb", false, false);
  }

  @Test
  public void testPath() {
    assertPath(null, null);
    assertPath("", "/");
    assertPath("//", "/");
    assertPath(" \t//", "/");
    assertPath("\\/", "/");
    assertPath("/test/", "/");
    assertPath(" \t\n\r/test/\\ /\\\t", "/");
    assertPath("/test/", "/");
    assertPath("/test/aaa", "/test");
    assertPath("/test /\\a\\/bbbbb", "/test /a");
  }

  @Test
  public void testPaths() {
    assertPaths(null);
    assertPaths("", "/");
    assertPaths("/", "/");
    assertPaths("/t", "/", "/t");
    assertPaths("/test", "/", "/test");
    assertPaths("/test/aaa", "/", "/test", "/test/aaa");
  }
}
