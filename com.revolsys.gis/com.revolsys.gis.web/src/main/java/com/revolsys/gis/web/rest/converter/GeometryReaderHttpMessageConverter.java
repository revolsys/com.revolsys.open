package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;

import com.revolsys.gis.data.io.DataObjectWriterFactory;
import com.revolsys.gis.data.io.GeometryReader;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;

public class GeometryReaderHttpMessageConverter extends
  AbstractHttpMessageConverter<GeometryReader> {

  public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  public GeometryReaderHttpMessageConverter() {
    super(GeometryReader.class,
      IoFactoryRegistry.INSTANCE.getMediaTypes(DataObjectWriterFactory.class),
      null);
  }

  @Override
  public GeometryReader read(
    Class<? extends GeometryReader> clazz,
    HttpInputMessage inputMessage)
    throws IOException,
    HttpMessageNotReadableException {
    try {
      final HttpHeaders headers = inputMessage.getHeaders();
      final MediaType mediaType = headers.getContentType();
      Charset charset = mediaType.getCharSet();
      if (charset == null) {
        charset = DEFAULT_CHARSET;
      }
      final InputStream body = inputMessage.getBody();
      final String mediaTypeString = mediaType.getType() + "/"
        + mediaType.getSubtype();
      final GeometryReaderFactory readerFactory = ioFactoryRegistry.getFactoryByMediaType(
        GeometryReaderFactory.class, mediaTypeString);
      if (readerFactory == null) {
        throw new HttpMessageNotReadableException("Cannot read data in format"
          + mediaType);
      } else {
        final InputStreamResource in = new InputStreamResource(body);
        final GeometryReader reader = readerFactory.createGeometryReader(in);

        return reader;
      }
    } catch (final IOException e) {
      throw new HttpMessageNotReadableException("Error reading data", e);
    }
  }
}
