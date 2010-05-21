package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpRequestUtils;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryHttpMessageConverter extends
  AbstractHttpMessageConverter<Geometry> {
  private static final Logger LOG = LoggerFactory.getLogger(GeometryHttpMessageConverter.class);

  public static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  public GeometryHttpMessageConverter() {
    super(Geometry.class,
      IoFactoryRegistry.INSTANCE.getMediaTypes(GeometryReaderFactory.class),
      IoFactoryRegistry.INSTANCE.getMediaTypes(GeometryWriterFactory.class));
  }

  @Override
  public Geometry read(
    Class<? extends Geometry> clazz,
    HttpInputMessage inputMessage)
    throws IOException,
    HttpMessageNotReadableException {
    final HttpHeaders headers = inputMessage.getHeaders();
    final MediaType mediaType = headers.getContentType();
    try {
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
        final Reader<Geometry> reader = readerFactory.createGeometryReader(new InputStreamResource(
          body));
        for (Geometry geometry : reader) {
          if (clazz.isAssignableFrom(geometry.getClass())) {
            return geometry;
          }
        }
        return null;
      }
    } catch (Throwable e) {
      LOG.error("Error reading data using " + mediaType, e);
      throw new HttpMessageNotReadableException("Error reading data using"
        + mediaType);
    }
  }

  @Override
  public void write(
    final Geometry geometry,
    final MediaType mediaType,
    final HttpOutputMessage outputMessage)
    throws IOException,
    HttpMessageNotWritableException {
    MediaType actualMediaType;
    if (mediaType == null) {
      actualMediaType = getDefaultMediaType();
    } else {
      actualMediaType = mediaType;
    }
    if (actualMediaType != null) {
      Charset charset = actualMediaType.getCharSet();
      if (charset == null) {
        charset = DEFAULT_CHARSET;
      }
      final HttpHeaders headers = outputMessage.getHeaders();
      headers.setContentType(actualMediaType);
      final OutputStream body = outputMessage.getBody();
      final String mediaTypeString = actualMediaType.getType() + "/"
        + actualMediaType.getSubtype();
      final GeometryWriterFactory writerFactory = ioFactoryRegistry.getFactoryByMediaType(
        GeometryWriterFactory.class, mediaTypeString);
      if (writerFactory == null) {
        throw new IllegalArgumentException("Media type " + actualMediaType
          + " not supported");
      } else {
        String baseName = HttpRequestUtils.getRequestBaseFileName();
        final Writer<Geometry> writer = writerFactory.createGeometryWriter(
          baseName, body, charset);
        writer.write(geometry);
        writer.close();
      }
    }
  }
}
