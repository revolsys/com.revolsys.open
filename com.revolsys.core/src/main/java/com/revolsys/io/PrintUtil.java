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
package com.revolsys.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * The PrintUtil class is a utility class for performing common print tasks with
 * classes from the java.io package.
 * 
 * @author Chris O'Grady
 */
public final class PrintUtil {

  /**
   * Given an input stream, print it to the standard out.
   * 
   * @param inputStream A printable input stream.
   * @param printStream The PrintStream to direct the output to.
   * @throws IOException
   */
  public static void print(
    final InputStream inputStream,
    final PrintStream printStream)
    throws IOException {
    IOUtils.copy(inputStream, printStream);
  }

  /**
   * Print the List, 1 item per line.
   * 
   * @param stringList The list of strings.
   * @param printStream The PrintStream to direct the output to.
   */
  public static void print(
    final List<String> stringList,
    final PrintStream printStream) {
    if (stringList == null) {
      printStream.println("The List has not been initialized.");
    } else if (stringList.size() == 0) {
      printStream.println("The List is empty.");
    } else {
      for (final String listItem : stringList) {
        printStream.println(listItem);
      }
    }
  }

  /**
   * Print the Map, 1 Key,Value pair per line.
   * 
   * @param stringMap The String Map to be printed
   * @param printStream The PrintStream to direct the output to.
   */
  public static void print(
    final Map<String, String> stringMap,
    final PrintStream printStream) {
    if (stringMap == null) {
      printStream.println("The Map has not been initialized.");
    } else if (stringMap.size() == 0) {
      printStream.println("There is nothing to print.");
    } else {
      for (final String responseKey : stringMap.keySet()) {
        printStream.println(responseKey + "=" + stringMap.get(responseKey));
      }
    }
  }

  /**
   * Print the MapReader, 1 Key,Value pair per line
   * 
   * @param resultsMapReader
   * @param printStream The PrintStream to direct the output to.
   */
  public static void print(
    final MapReader resultsMapReader,
    final PrintStream printStream) {
    final Iterator<Map<String, Object>> resultsIterator = resultsMapReader.iterator();
    boolean first = true;
    while (resultsIterator.hasNext()) {
      final Map<String, Object> result = resultsIterator.next();
      if (first) {
        for (final String csvKey : result.keySet()) {
          printStream.print(csvKey + "\t");
        }
        printStream.println("");
        first = false;
      }
      for (final String csvKey : result.keySet()) {
        printStream.print(result.get(csvKey) + "\t");
      }
      printStream.println("");
    }
  }
}
