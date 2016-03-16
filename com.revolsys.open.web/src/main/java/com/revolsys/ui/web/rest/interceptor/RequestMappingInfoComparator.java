package com.revolsys.ui.web.rest.interceptor;

import java.util.Comparator;

import org.springframework.util.PathMatcher;

/**
 * Comparator capable of sorting {@link RequestMappingInfo}s (RHIs) so that
 * sorting a list with this comparator will result in:
 * <ul>
 * <li>RHIs with {@linkplain RequestMappingInfo#matchedPaths better matched
 * paths} take prescedence over those with a weaker match (as expressed by the
 * {@linkplain PathMatcher#getPatternComparator(String) path pattern
 * comparator}.) Typically, this means that patterns without wild cards and
 * uri templates will be ordered before those without.</li>
 * <li>RHIs with one single {@linkplain RequestMappingInfo#methods request
 * method} will be ordered before those without a method, or with more than
 * one method.</li>
 * <li>RHIs with more {@linkplain RequestMappingInfo#params request
 * parameters} will be ordered before those with less parameters</li> </ol>
 */
public class RequestMappingInfoComparator implements Comparator<RequestMappingInfo> {

  private final Comparator<String> pathComparator;

  RequestMappingInfoComparator(final Comparator<String> pathComparator) {
    this.pathComparator = pathComparator;
  }

  @Override
  public int compare(final RequestMappingInfo info1, final RequestMappingInfo info2) {
    final String path1 = info1.bestMatchedPath();
    final String path2 = info2.bestMatchedPath();
    if (path1.startsWith(path2)) {
      return -1;
    } else if (path2.startsWith(path1)) {
      return 1;
    } else {
      final int pathComparison = this.pathComparator.compare(path1, path2);
      if (pathComparison != 0) {
        return pathComparison;
      }
      final int info1MethodCount = info1.methods.length;
      final int info2MethodCount = info2.methods.length;
      if (info1MethodCount == 0 && info2MethodCount > 0) {
        return 1;
      } else if (info2MethodCount == 0 && info1MethodCount > 0) {
        return -1;
      } else if (info1MethodCount == 1 & info2MethodCount > 1) {
        return -1;
      } else if (info2MethodCount == 1 & info1MethodCount > 1) {
        return 1;
      }
      return 0;
    }
  }
}
