package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.collection.ArrayListOfMap;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class ListOfMapHttpMessageConverter extends AbstractHttpMessageConverter<ArrayListOfMap> {
  public ListOfMapHttpMessageConverter() {
    super(ArrayListOfMap.class, null, IoFactory.mediaTypes(MapWriterFactory.class));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(final ArrayListOfMap list, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    if (!HttpServletUtils.getResponse().isCommitted()) {
      final Charset charset = HttpServletUtils.setContentTypeWithCharset(outputMessage, mediaType);
      final OutputStream body = outputMessage.getBody();
      final String mediaTypeString = mediaType.getType() + "/" + mediaType.getSubtype();
      final MapWriterFactory writerFactory = IoFactory.factoryByMediaType(MapWriterFactory.class,
        mediaTypeString);
      final MapWriter writer = writerFactory.newMapWriter(body, charset);
      writer.setProperty(IoConstants.INDENT, true);
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
}
