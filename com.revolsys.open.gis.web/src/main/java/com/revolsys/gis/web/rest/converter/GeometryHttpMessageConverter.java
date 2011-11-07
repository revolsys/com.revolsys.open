package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.gis.geometry.io.GeometryWriterFactory;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.spring.InputStreamResource;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpRequestUtils;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryHttpMessageConverter extends
  AbstractHttpMessageConverter<Geometry> {
  public static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private static final Logger LOG = LoggerFactory.getLogger(GeometryHttpMessageConverter.class);

  private GeometryFactory geometryFactory = GeometryFactory.getFactory(4326);

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  public GeometryHttpMessageConverter() {
    super(Geometry.class,
      IoFactoryRegistry.INSTANCE.getMediaTypes(GeometryReaderFactory.class),
      IoFactoryRegistry.INSTANCE.getMediaTypes(GeometryWriterFactory.class));
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public Geometry read(
    final Class<? extends Geometry> clazz,
    final HttpInputMessage inputMessage)
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
        final Reader<Geometry> reader = readerFactory.createGeometryReader(new InputStreamResource("geometryUpload",
          body));
        GeometryFactory factory = geometryFactory;
        final ServletWebRequest requestAttributes = (ServletWebRequest)RequestContextHolder.getRequestAttributes();
        final String srid = requestAttributes.getParameter("srid");
        if (srid != null && srid.trim().length() > 0) {
          factory = GeometryFactory.getFactory(Integer.parseInt(srid));
        }
        reader.setProperty(IoConstants.GEOMETRY_FACTORY, factory);
        for (final Geometry geometry : reader) {
          if (clazz.isAssignableFrom(geometry.getClass())) {
            return geometry;
          }
        }
        return null;
      }
    } catch (final Throwable e) {
      LOG.error("Error reading data using " + mediaType, e);
      throw new HttpMessageNotReadableException("Error reading data using"
        + mediaType);
    }
  }

  public void setGeometryFactory(
    final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
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
        final String baseName = HttpRequestUtils.getRequestBaseFileName();
        final Writer<Geometry> writer = writerFactory.createGeometryWriter(
          baseName, body, charset);
        writer.write(geometry);
        writer.close();
      }
    }
  }
}
