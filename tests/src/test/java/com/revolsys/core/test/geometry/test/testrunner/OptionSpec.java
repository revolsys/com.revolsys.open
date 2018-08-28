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
package com.revolsys.core.test.geometry.test.testrunner;

import java.util.Iterator;
import java.util.Vector;

/**
 * Specifes the syntax for a single option on a
 * command line
 *
 * ToDo:
 * - add syntax pattern parsing
 * Syntax patterns are similar to Java type signatures
 *  F - float
 *  I - int
 *  L - long
 *  S - string
 *  B - boolean
 *  + - one or more
 * eg:  "FIS+" takes a double, int, and one or more Strings
 * @version 1.7
 */
public class OptionSpec {

  public final static int NARGS_ONE_OR_MORE = -2;

  public final static int NARGS_ZERO_OR_MORE = -1;

  public final static int NARGS_ZERO_OR_ONE = -3;

  public final static String OPTION_FREE_ARGS = "**FREE_ARGS**"; // option name

  // for free
  // args

  String argDoc = ""; // arg syntax description

  String doc = ""; // option description

  int nAllowedArgs = 0; // number of arguments allowed

  String name;

  Vector options = new Vector();

  String syntaxPattern;

  public OptionSpec(final String optName) {
    this.name = optName;
    this.nAllowedArgs = 0;
  }

  public OptionSpec(final String optName, final int nAllowed) {
    this(optName);
    // check for invalid input
    if (this.nAllowedArgs >= NARGS_ZERO_OR_ONE) {
      this.nAllowedArgs = nAllowed;
    }
  }

  public OptionSpec(final String optName, final String _syntaxPattern) {
    this(optName);
    this.syntaxPattern = _syntaxPattern;
  }

  void addOption(final Option opt) {
    this.options.addElement(opt);
  }

  void checkNumArgs(final String[] args) throws ParseException {
    if (this.nAllowedArgs == NARGS_ZERO_OR_MORE) {
      // args must be ok
    } else if (this.nAllowedArgs == NARGS_ONE_OR_MORE) {
      if (args.length <= 0) {
        throw new ParseException(
          "option " + this.name + ": expected one or more args, found " + args.length);
      }
    } else if (this.nAllowedArgs == NARGS_ZERO_OR_ONE) {
      if (args.length > 1) {
        throw new ParseException(
          "option " + this.name + ": expected zero or one arg, found " + args.length);
      }
    } else if (args.length != this.nAllowedArgs) {
      throw new ParseException(
        "option " + this.name + ": expected " + this.nAllowedArgs + " args, found " + args.length);
    }
  }

  int getAllowedArgs() {
    return this.nAllowedArgs;
  }

  public String getArgDesc() {
    return this.argDoc;
  }

  public String getDocDesc() {
    return this.doc;
  }

  String getName() {
    return this.name;
  }

  public int getNumOptions() {
    return this.options.size();
  }

  public Option getOption(final int i) {
    if (this.options.size() > 0) {
      return (Option)this.options.elementAt(i);
    }
    return null;
  }

  public Iterator getOptions() {
    return this.options.iterator();
  }

  public boolean hasOption() {
    return this.options.size() > 0;
  }

  Option parse(final String[] args) throws ParseException {
    checkNumArgs(args);
    return new Option(this, args);
  }

  public void setDoc(final String _argDoc, final String docLine) {
    this.argDoc = _argDoc;
    this.doc = docLine;
  }

}
