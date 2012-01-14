package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.ui.html.view.Element;

public class ElementHttpMessageConverter extends
  AbstractHttpMessageConverter<Element> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private static final Collection<MediaType> WRITE_MEDIA_TYPES = Arrays.asList(
    MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML);

  public ElementHttpMessageConverter() {
    super(Element.class, Collections.emptySet(), WRITE_MEDIA_TYPES);
  }

  @Override
  public void write(
    final Element element,
    final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException,
    HttpMessageNotWritableException {
    if (element != null) {
      Charset charset = mediaType.getCharSet();
      if (charset == null) {
        charset = DEFAULT_CHARSET;
      }

      final HttpHeaders headers = outputMessage.getHeaders();
      headers.setContentType(mediaType);

      final OutputStream out = outputMessage.getBody();
      if (MediaType.TEXT_HTML.equals(mediaType)
        || MediaType.APPLICATION_XHTML_XML.equals(mediaType)) {
        element.serialize(out);
      }
    }
  }

}
