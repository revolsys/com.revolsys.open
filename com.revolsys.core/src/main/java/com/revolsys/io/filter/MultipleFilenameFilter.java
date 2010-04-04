/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.io.filter;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The MultipleFilenameFilter is a {@link FilenameFilter} that can be used to
 * apply more than one filename filter to the result. The filters can either be
 * used to exclude files from the result
 * {@link #addExclusionFilter(FilenameFilter)} or include files from the result
 * {@link #addFilter(FilenameFilter)}. For each file the filter will look for a
 * match in the exclusion filters followed by the inclusion filters. If an
 * exclusion filter matches the file will be excluded, if an inclusion filter
 * matches the file will be included, if no files match the file will be
 * excluded.
 * 
 * @author Paul Austin
 */
public class MultipleFilenameFilter implements FilenameFilter {
  /** The filters to exclude files from the results. */
  private final List exclusionFilters = new ArrayList();

  /** The filters to include files from the results. */
  private final List filters = new ArrayList();

  /**
   * Check to see if the file should be included in the list of matched files
   * 
   * @param directory The directory in which the file was found.
   * @param filename The name of the file.
   * @return True if the file matched, false otherwise.
   */
  public boolean accept(
    final File directory,
    final String filename) {
    for (final Iterator excludes = exclusionFilters.iterator(); excludes.hasNext();) {
      final FilenameFilter filter = (FilenameFilter)excludes.next();
      if (filter.accept(directory, filename)) {
        return false;
      }
    }
    for (final Iterator includes = filters.iterator(); includes.hasNext();) {
      final FilenameFilter filter = (FilenameFilter)includes.next();
      if (filter.accept(directory, filename)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Add a filter used to exclude files from the results.
   * 
   * @param filter The filename filter.
   */
  public void addExclusionFilter(
    final FilenameFilter filter) {
    exclusionFilters.add(filter);
  }

  /**
   * Add a filter used to include files from the results.
   * 
   * @param filter The filename filter.
   */
  public void addFilter(
    final FilenameFilter filter) {
    filters.add(filter);
  }

}
