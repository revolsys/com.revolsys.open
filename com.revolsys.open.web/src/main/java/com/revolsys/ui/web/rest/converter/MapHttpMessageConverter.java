package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.json.JsonParser;
import com.revolsys.ui.web.utils.HttpRequestUtils;

public class MapHttpMessageConverter extends AbstractHttpMessageConverter<Map> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();

  public MapHttpMessageConverter() {
    super(Map.class, Collections.singleton(MediaType.APPLICATION_JSON),
      IoFactoryRegistry.getInstance().getMediaTypes(MapWriterFactory.class));
  }

  @Override
  public Map read(final Class<? extends Map> clazz,
    final HttpInputMessage inputMessage) throws IOException,
    HttpMessageNotReadableException {
    try {
      final Map<String, Object> map = new HashMap<String, Object>();
      final InputStream in = inputMessage.getBody();
      final Map<String, Object> readMap = JsonParser.read(in);
      if (readMap != null) {
        map.putAll(readMap);
      }
      return map;
    } catch (final Throwable e) {
      throw new HttpMessageNotReadableException(e.getMessage(), e);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(final Map map, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException,
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
    final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
    String callback = request.getParameter("jsonp");
    if (callback == null) {
      callback = request.getParameter("callback");
    }
    if (callback != null) {
      writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
    }
    final Object title = request.getAttribute(IoConstants.TITLE_PROPERTY);
    if (title != null) {
      writer.setProperty(IoConstants.TITLE_PROPERTY, title);
    }
    writer.write(map);
    writer.close();
  }
}
