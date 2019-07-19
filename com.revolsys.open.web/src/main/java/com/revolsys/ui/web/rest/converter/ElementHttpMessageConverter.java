package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class ElementHttpMessageConverter extends AbstractHttpMessageConverter<Element> {

  private static final Collection<MediaType> WRITE_MEDIA_TYPES = Arrays
    .asList(MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML);

  public ElementHttpMessageConverter() {
    super(Element.class, Collections.emptySet(), WRITE_MEDIA_TYPES);
  }

  @Override
  public void write(final Element element, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    if (!HttpServletUtils.getResponse().isCommitted()) {
      if (element != null) {
        HttpServletUtils.setContentTypeWithCharset(outputMessage, mediaType);

        final OutputStream out = outputMessage.getBody();
        if (MediaType.TEXT_HTML.equals(mediaType)
          || MediaType.APPLICATION_XHTML_XML.equals(mediaType)) {
          element.serialize(out);
        }
      }
    }
  }

}
