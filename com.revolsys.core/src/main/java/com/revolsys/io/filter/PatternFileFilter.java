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
import java.io.FileFilter;
import java.util.regex.Pattern;

/**
 * The PatternFileFilter is a {@link FileFilter} that only returns files if they
 * match the regular expression.
 * 
 * @author Paul Austin
 */
public class PatternFileFilter implements FileFilter {
  /** The regular expression pattern to match file names. */
  private final Pattern pattern;

  /**
   * Construct a new PatternFileFilter.
   * 
   * @param regex The regular expression.
   */
  public PatternFileFilter(
    final String regex) {
    pattern = Pattern.compile(regex);
  }

  /**
   * Check to see if the file should be included in the list of matched files
   * 
   * @param file The file.
   * @return True if the file matched, false otherwise.
   */
  public boolean accept(
    final File file) {
    final String fileName = file.getName();
    return pattern.matcher(fileName).matches();
  }
}
