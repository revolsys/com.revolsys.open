package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.ui.model.PageInfo;
import com.revolsys.ui.web.utils.HttpRequestUtils;

public class PageInfoHttpMessageConverter extends
  AbstractHttpMessageConverter<PageInfo> {

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  private static final MediaType TEXT_URI_LIST = MediaType.parseMediaType("text/uri-list");

  private static final Collection<?> WRITE_MEDIA_TYPES = Arrays.asList(
    MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML,
    "application/vnd.sun.wadl+xml", TEXT_URI_LIST, MediaType.APPLICATION_JSON, MediaType.TEXT_XML);

  public PageInfoHttpMessageConverter() {
    super(PageInfo.class, Collections.emptySet(), WRITE_MEDIA_TYPES);
  }

  @Override
  public void write(final PageInfo pageInfo, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException,
    HttpMessageNotWritableException {
    if (pageInfo != null) {
      Charset charset = mediaType.getCharSet();
      if (charset == null) {
        charset = DEFAULT_CHARSET;
      }
      // MediaType mediaType = variant.getMediaType();
      // if (MediaType.APPLICATION_XHTML_XML.equals(mediaType)) {
      // Request request = getRequest();
      // Map<String, Object> attributes = request.getAttributes();
      // boolean wrapped = attributes.get(WrappedRepresentationFilter.WRAPPED)
      // == Boolean.TRUE;
      // Reference resourceRef = request.getResourceRef();
      // return new WadlRepresentation(attributes, resourceRef, resourceInfo,
      // wrapped);
      // } else if (MediaType.APPLICATION_WADL_XML.equals(mediaType)) {
      // return describe(variant);
      // } else
      outputMessage.getHeaders().setContentType(mediaType);
      final OutputStream out = outputMessage.getBody();
      if (TEXT_URI_LIST.equals(mediaType)) {
        writeUriList(out, pageInfo);
        // return getUriListRepresentation();
      } else {
        writeResourceList(mediaType, charset, out, pageInfo);
      }
    }
  }

  public void writeResourceList(final MediaType mediaType, Charset charset,
    final OutputStream out, final PageInfo pageInfo) {

    final String mediaTypeString = mediaType.getType() + "/"
      + mediaType.getSubtype();
    final MapWriterFactory writerFactory = ioFactoryRegistry.getFactoryByMediaType(
      MapWriterFactory.class, mediaTypeString);
    if (writerFactory != null) {
      final MapWriter writer = writerFactory.getWriter(new OutputStreamWriter(
        out, charset));
      writer.setProperty(IoConstants.INDENT_PROPERTY, true);
      writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, false);
      final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
      String callback = request.getParameter("jsonp");
      if (callback == null) {
        callback = request.getParameter("callback");
      }
      if (callback != null) {
        writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
      }

      for (final Entry<String, PageInfo> childPage : pageInfo.getPages()
        .entrySet()) {
        final String path = childPage.getKey();
        final PageInfo childPageInfo = childPage.getValue();
        final Map<String, String> childPageMap = new LinkedHashMap<String, String>();
        // TODO add parent path
        childPageMap.put("resourceUri", path);
        childPageMap.put("title", childPageInfo.getTitle());
        childPageMap.put("description", childPageInfo.getDescription());
        writer.write(childPageMap);
      }
      writer.close();
    }
  }

  private void writeUriList(final OutputStream out, final PageInfo pageInfo)
    throws IOException {
    final Writer writer = new OutputStreamWriter(out,
      Charset.forName("US-ASCII"));

    try {
      for (final String uri : pageInfo.getPages().keySet()) {
        writer.write(uri);
        writer.write("\r\n");
      }
    } finally {
      FileUtil.closeSilent(writer);
    }
  }
}
