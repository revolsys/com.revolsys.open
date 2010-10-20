package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;

public class MapHttpMessageConverter extends AbstractHttpMessageConverter<Map> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  public MapHttpMessageConverter() {
    super(Map.class, null,
      IoFactoryRegistry.INSTANCE.getMediaTypes(MapWriterFactory.class));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(
    final Map map,
    final MediaType mediaType,
    final HttpOutputMessage outputMessage)
    throws IOException,
    HttpMessageNotWritableException {
    Charset charset = mediaType.getCharSet();
    if (charset == null) {
      charset = DEFAULT_CHARSET;
    }
    outputMessage.getHeaders().setContentType(mediaType);
    final OutputStream body = outputMessage.getBody();
    final String mediaTypeString = mediaType.getType() + "/"
      + mediaType.getSubtype();
    final MapWriterFactory writerFactory = ioFactoryRegistry.getFactoryByMediaType(
      MapWriterFactory.class, mediaTypeString);
    final MapWriter writer = writerFactory.getWriter(new OutputStreamWriter(
      body, charset));
    writer.setProperty(IoConstants.INDENT_PROPERTY, true);
    writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
      final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    final String callback = (String)requestAttributes.getAttribute("jsonp",
      RequestAttributes.SCOPE_REQUEST);
    if (callback != null) {
      writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
    }
    writer.write(map);
    writer.close();
  }
}
