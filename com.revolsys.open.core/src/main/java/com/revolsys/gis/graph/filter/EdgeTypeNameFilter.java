package com.revolsys.gis.graph.filter;

import java.util.Arrays;
import java.util.Collection;

import javax.xml.namespace.QName;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.Edge;

/**
 * Filter {@link Edge} objects to include those which have one of the specified
 * type names.
 * 
 * @author Paul Austin
 * @param <T> The type of object stored in the {@link Edge}
 */
public class EdgeTypeNameFilter<T> implements Filter<Edge<T>> {
  /** The list of type names to accept. */
  private final Collection<QName> typeNames;

  /**
   * Construct a new EdgeTypeNameFilter.
   * 
   * @param typeNames The list of type names to accept.
   */
  public EdgeTypeNameFilter(final Collection<QName> typeNames) {
    this.typeNames = typeNames;
  }

  /**
   * Construct a new EdgeTypeNameFilter.
   * 
   * @param typeNames The list of type names to accept.
   */
  public EdgeTypeNameFilter(final QName... typeNames) {
    this(Arrays.asList(typeNames));
  }

  /**
   * Accept the edge if its type name is in the list of type names specified on
   * this filter.
   * 
   * @param edge The edge to filter.
   * @return True if the edge has one of the type names, false otherwise.
   */
  public boolean accept(final Edge<T> edge) {
    final QName typeName = edge.getTypeName();
    return typeNames.contains(typeName);
  }
}
