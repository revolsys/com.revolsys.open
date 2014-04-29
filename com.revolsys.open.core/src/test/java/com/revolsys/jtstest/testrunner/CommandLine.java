
/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.jtstest.testrunner;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * A class to parse Unix (and DOS/Win)-style application command-lines
 * @version 1.7
 */
public class CommandLine {

  Hashtable optSpecs = new Hashtable();

  Vector optVec = new Vector(); // used to store options in order of entry

  char optionChar; // the char that indicates an option. Default is '/', which
                   // is
                   // NT Standard, but this causes problems on Unix systems, so
                   // '-' should
                   // be used for cross-platform apps

  public CommandLine() {
    this('/');
  }

  public CommandLine(final char optionCh) {
    optionChar = optionCh;
  }

  /**
   *  adds an option for an <B>existing</B> option spec
   */
  void addOption(final Option opt) {
    final String name = opt.getName();
    ((OptionSpec)optSpecs.get(name.toLowerCase())).addOption(opt);
  }

  public void addOptionSpec(final OptionSpec optSpec) {
    final String name = optSpec.getName();
    // should check for duplicate option names here
    optSpecs.put(name.toLowerCase(), optSpec);
    optVec.add(optSpec);
  }

  public Option getOption(final String name) {
    final OptionSpec spec = getOptionSpec(name);
    if (spec == null) {
      return null;
    }
    return spec.getOption(0);
  }

  public Iterator getOptions(final String name) {
    final OptionSpec spec = getOptionSpec(name);
    return spec.getOptions();
  }

  OptionSpec getOptionSpec(final String name) {
    if (optSpecs.containsKey(name.toLowerCase())) {
      return (OptionSpec)optSpecs.get(name.toLowerCase());
    }
    return null;
  }

  public boolean hasOption(final String name) {
    final OptionSpec spec = getOptionSpec(name);
    if (spec == null) {
      return false;
    }
    return spec.hasOption();
  }

  public void parse(final String[] args) throws ParseException {
    String noOptMsg;
    String optName;
    final Vector params = new Vector();
    int i = 0;
    int paramStart;
    while (i < args.length) {
      if (args[i].charAt(0) == optionChar) {
        optName = args[i].substring(1);
        noOptMsg = "Invalid option: " + args[i];
        paramStart = i + 1;
      } else {
        optName = OptionSpec.OPTION_FREE_ARGS;
        noOptMsg = "Invalid option: " + args[i];
        paramStart = i;
      }
      final OptionSpec optSpec = getOptionSpec(optName);
      if (optSpec == null) {
        throw new ParseException(noOptMsg);
      }

      final int expectedArgCount = optSpec.getAllowedArgs();
      // parse option args
      parseParams(args, params, paramStart, expectedArgCount);
      final Option opt = optSpec.parse((String[])params.toArray(new String[0]));
      // check for number of allowed instances here
      addOption(opt);
      i++;
      i += params.size();
    }

  }

  void parseParams(final String[] args, final Vector params, int i,
    final int expectedArgCount) {
    params.clear();
    int count = 0;
    int expected = expectedArgCount;
    if (expectedArgCount == OptionSpec.NARGS_ZERO_OR_ONE) {
      expected = 1;
    }
    if (expectedArgCount == OptionSpec.NARGS_ZERO_OR_MORE) {
      expected = 999999999;
    }
    if (expectedArgCount == OptionSpec.NARGS_ONE_OR_MORE) {
      expected = 999999999;
    }
    while (i < args.length && count < expected
      && args[i].charAt(0) != optionChar) {
      params.addElement(args[i++]);
      count++;
    }
  }

  public void printDoc(final PrintStream out) {
    OptionSpec os = null;
    out.println("Options:");
    for (final Iterator i = optVec.iterator(); i.hasNext();) {
      os = (OptionSpec)i.next();
      String name = optionChar + os.getName();
      if (os.getName() == OptionSpec.OPTION_FREE_ARGS) {
        name = "(free)";
      }
      out.println("  " + name + " " + os.getArgDesc() + " - " + os.getDocDesc());
    }
  }

}
