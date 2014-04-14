package com.revolsys.jtstest.util;

import org.jdom.Element;
import org.jdom.Namespace;

/**
 * This class extends a normal Element with a traceback to its
 * beginning and endling line number, if available and reported.
 * <p>
 * Each instance is created using a factory internal to the
 * LineNumberSAXBuilder class.
 *
 * @author Per Norrman
 *
 */
public class LineNumberElement extends Element {
  private int _startLine;

  private int _endLine;

  public LineNumberElement() {
    super();
  }

  public LineNumberElement(final String name) {
    super(name);
  }

  public LineNumberElement(final String name, final Namespace namespace) {
    super(name, namespace);
  }

  public LineNumberElement(final String name, final String uri) {
    super(name, uri);
  }

  public LineNumberElement(final String name, final String prefix,
    final String uri) {
    super(name, prefix, uri);
  }

  public int getEndLine() {
    return _endLine;
  }

  public int getStartLine() {
    return _startLine;
  }

  public void setEndLine(final int i) {
    _endLine = i;
  }

  public void setStartLine(final int i) {
    _startLine = i;
  }

}
