package com.revolsys.filter;

/**
 * The object interface is used by processes wishing to process data objects
 * that match a defined filter. The following example shows the typical use of
 * filters.
 * 
 * <pre>
 *   public void process(List objects) {
 *     for (Iterator iter = objects.iterator; iter.hasNext();; ) {
 *       DataObject object = (DataObject)iter.next(); 
 *       if (filter.accept(object) {
 *         // perform action
 *       }
 *     }
 *   }
 * </pre>
 * 
 * @author Paul Austin
 */
public interface Filter<T> {
  /**
   * Check that the object matches the filter, returning true if matched, false
   * otherwise.
   * 
   * @param object The object to check.
   * @return True if the object matched the filter, false otherwise.
   */
  boolean accept(
    T object);
}
