package com.revolsys.swing.map.layer.record.style.marker;

import java.net.URI;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jeometry.common.logging.Logs;
import org.w3c.dom.Document;

import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class SvgUtil {
  private static final String PARSER_CLASS_NAME = XMLResourceDescriptor.getXMLParserClassName();

  public static Document newDocument(final Object source) {
    final Resource resource = Resource.getResource(source);
    return newDocument(resource);
  }

  public static Document newDocument(final Resource resource) {
    if (resource == null) {
      return null;
    } else {
      final String uri = resource.getUriString();
      return newDocument(uri);
    }
  }

  public static Document newDocument(final String uri) {
    if (Property.hasValue(uri)) {
      final DocumentFactory documentFactory = new SAXSVGDocumentFactory(PARSER_CLASS_NAME);
      try {
        return documentFactory.createDocument(SVGConstants.SVG_NAMESPACE_URI,
          SVGConstants.SVG_SVG_TAG, uri);
      } catch (final Throwable e) {
        Logs.debug(SvgUtil.class, PARSER_CLASS_NAME, e);
      }
    }
    return null;
  }

  public static TranscoderInput newTranscodeInput(final URI uri) {
    final String uriString = uri.toString();
    final Document document = newDocument(uriString);
    return new TranscoderInput(document);
  }
}
