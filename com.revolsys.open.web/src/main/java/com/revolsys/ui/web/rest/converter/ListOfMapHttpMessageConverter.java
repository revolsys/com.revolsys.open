package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.collection.ArrayListOfMap;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class ListOfMapHttpMessageConverter extends
  AbstractHttpMessageConverter<ArrayListOfMap> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();

  public ListOfMapHttpMessageConverter() {
    super(ArrayListOfMap.class, null, IoFactoryRegistry.getInstance()
      .getMediaTypes(MapWriterFactory.class));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(
    final ArrayListOfMap list,
    final MediaType mediaType,
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
    writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, false);
    final HttpServletRequest request = HttpServletUtils.getRequest();
    writer.setProperty(IoConstants.JSON_LIST_ROOT_PROPERTY,
      request.getAttribute(IoConstants.JSON_LIST_ROOT_PROPERTY));
    String callback = request.getParameter("jsonp");
    if (callback == null) {
      callback = request.getParameter("callback");
    }
    if (callback != null) {
      writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
    }
    for (final Map<String, Object> map : (ArrayListOfMap<Object>)list) {
      writer.write(map);
    }
    writer.close();
  }
}
