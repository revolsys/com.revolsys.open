package com.revolsys.swing;

import java.util.Comparator;

import javax.swing.filechooser.FileFilter;

/**
 * Compares the description of the two FileFilters.
 *
 * @author Paul Austin
 */
public class FileFilterComparator implements Comparator<FileFilter> {
  /**
   * Compares its two arguments for order. Returns a negative integer, zero, or
   * a positive integer as the description of the first filter is less than,
   * equal to, or greater than the description of the second.
   *
   * @param f1 the first filter to be compared.
   * @param f2 the second filter to be compared.
   * @return a negative integer, zero, or a positive integer as the first
   *         argument is less than, equal to, or greater than the second.
   */
  @Override
  public int compare(final FileFilter f1, final FileFilter f2) {
    final String description1 = f1.getDescription();
    final String description2 = f2.getDescription();
    return description1.compareTo(description2);
  }
}
