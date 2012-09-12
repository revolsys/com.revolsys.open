package com.revolsys.gis.graph.filter;

import java.util.Arrays;
import java.util.Collection;

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
  private final Collection<String> typePaths;

  /**
   * Construct a new EdgeTypeNameFilter.
   * 
   * @param typePaths The list of type names to accept.
   */
  public EdgeTypeNameFilter(final Collection<String> typePaths) {
    this.typePaths = typePaths;
  }

  /**
   * Construct a new EdgeTypeNameFilter.
   * 
   * @param typePaths The list of type names to accept.
   */
  public EdgeTypeNameFilter(final String... typePaths) {
    this(Arrays.asList(typePaths));
  }

  /**
   * Accept the edge if its type name is in the list of type names specified on
   * this filter.
   * 
   * @param edge The edge to filter.
   * @return True if the edge has one of the type names, false otherwise.
   */
  @Override
  public boolean accept(final Edge<T> edge) {
    final String typePath = edge.getTypeName();
    return typePaths.contains(typePath);
  }
}
