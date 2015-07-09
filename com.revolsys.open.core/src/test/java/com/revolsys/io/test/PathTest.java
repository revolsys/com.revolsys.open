package com.revolsys.io.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.io.Path;

public class PathTest {

  private void assertAncestor(final String parentPath, final String childPath,
    final boolean expected) {
    final boolean ancestor = Path.isAncestor(parentPath, childPath);
    Assert.assertEquals(expected, ancestor);
  }

  private void assertChildName(final String parentPath, final String childPath,
    final String expected) {
    final String childName = Path.getChildName(parentPath, childPath);
    Assert.assertEquals(expected, childName);
  }

  private void assertChildPath(final String parentPath, final String childPath,
    final String expected) {
    final String childName = Path.getChildPath(parentPath, childPath);
    Assert.assertEquals(expected, childName);
  }

  private void assertClean(final String source, final String expected) {
    final String cleaned = Path.clean(source);
    Assert.assertEquals(expected, cleaned);

    final String cleanedUpper = Path.cleanUpper(source);
    String expectedUpper;
    if (expected == null) {
      expectedUpper = null;
    } else {
      expectedUpper = expected.toUpperCase();
    }
    Assert.assertEquals(expectedUpper, cleanedUpper);
  }

  private void assertName(final String source, final String expected) {
    final String name = Path.getName(source);
    Assert.assertEquals(expected, name);
  }

  private void assertParent(final String parentPath, final String childPath,
    final boolean expected) {
    final boolean parent = Path.isParent(parentPath, childPath);
    Assert.assertEquals(expected, parent);
  }

  private void assertPath(final String source, final String expected) {
    final String path = Path.getPath(source);
    Assert.assertEquals(expected, path);
  }

  private void assertPaths(final String path, final String... expected) {
    final List<String> paths = Path.getPaths(path);
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
    assertParent(null, null, false);
    assertParent("", null, false);
    assertParent(null, "", false);
    assertParent("/test", null, false);
    assertParent(null, "/test", false);
    assertParent("/", "", false);
    assertParent("/", "/", false);
    assertParent("", "/", false);
    assertParent("/test", "/test/aaa", true);
    assertParent("/test/aaa", "/test/aaa", false);
    assertParent("/test/aaa", "/test", false);
    assertParent("/test/aaa", "/test/aaa/bbb", true);
    assertParent("/test/", "/test/aaa/bbb", false);
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
