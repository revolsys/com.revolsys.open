package com.revolsys.jtstest.util;

import org.jdom.DefaultJDOMFactory;
import org.jdom.Element;
import org.jdom.JDOMFactory;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.input.SAXHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This builder works in parallell with {@link LineNumberElement}
 * to provide each element with information on its beginning and
 * ending line number in the corresponding source.
 * This only works for SAX parsers that supply that information, and
 * since this is optional, there are no guarantees.
 * <p>
 * Note that this builder always creates its own for each
 * build, thereby cancelling any previous call to setFactory.
 * <p>
 * All elements created are instances of {@link LineNumberElement}.
 * No other construct currently receive line number information.
 *
 * @author Per Norrman
 *
 */
public class LineNumberSAXBuilder extends SAXBuilder {
  private class MyFactory extends DefaultJDOMFactory {

    @Override
    public Element element(final String name) {
      return new LineNumberElement(name);
    }

    @Override
    public Element element(final String name, final Namespace namespace) {
      return new LineNumberElement(name, namespace);
    }

    @Override
    public Element element(final String name, final String uri) {
      return new LineNumberElement(name, uri);
    }

    @Override
    public Element element(final String name, final String prefix,
      final String uri) {
      return new LineNumberElement(name, prefix, uri);
    }

  }

  private class MySAXHandler extends SAXHandler {

    public MySAXHandler(final JDOMFactory f) {
      super(f);
    }

    /** override */
    @Override
    public void endElement(final String arg0, final String arg1,
      final String arg2) throws SAXException {
      final Locator l = getDocumentLocator();
      if (l != null) {
        ((LineNumberElement)getCurrentElement()).setEndLine(l.getLineNumber());
      }

      super.endElement(arg0, arg1, arg2);
    }

    /** override */
    @Override
    public void startElement(final String arg0, final String arg1,
      final String arg2, final Attributes arg3) throws SAXException {
      super.startElement(arg0, arg1, arg2, arg3);
      final Locator l = getDocumentLocator();
      if (l != null) {
        ((LineNumberElement)getCurrentElement()).setStartLine(l.getLineNumber());
      }
    }

  }

  @Override
  protected SAXHandler createContentHandler() {
    return new MySAXHandler(new MyFactory());
  }

}
