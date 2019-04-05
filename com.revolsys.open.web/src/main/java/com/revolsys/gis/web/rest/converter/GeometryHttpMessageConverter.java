package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.jeometry.common.logging.Logs;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import com.revolsys.geometry.io.GeometryReaderFactory;
import com.revolsys.geometry.io.GeometryWriter;
import com.revolsys.geometry.io.GeometryWriterFactory;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class GeometryHttpMessageConverter extends AbstractHttpMessageConverter<Geometry> {
  private GeometryFactory geometryFactory = GeometryFactory.wgs84();

  public GeometryHttpMessageConverter() {
    super(Geometry.class, IoFactory.mediaTypes(GeometryReaderFactory.class),
      IoFactory.mediaTypes(GeometryWriterFactory.class));
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public Geometry read(final Class<? extends Geometry> clazz, final HttpInputMessage inputMessage)
    throws IOException, HttpMessageNotReadableException {
    final HttpHeaders headers = inputMessage.getHeaders();
    final MediaType mediaType = headers.getContentType();
    try {
      Charset charset = mediaType.getCharset();
      if (charset == null) {
        charset = StandardCharsets.UTF_8;
      }
      final InputStream body = inputMessage.getBody();
      final String mediaTypeString = mediaType.getType() + "/" + mediaType.getSubtype();
      final GeometryReaderFactory readerFactory = IoFactory
        .factoryByMediaType(GeometryReaderFactory.class, mediaTypeString);
      if (readerFactory == null) {
        throw new HttpMessageNotReadableException("Cannot read data in format" + mediaType);
      } else {
        final Reader<Geometry> reader = readerFactory
          .newGeometryReader(new InputStreamResource("geometryUpload", body));
        GeometryFactory factory = this.geometryFactory;
        final ServletWebRequest requestAttributes = (ServletWebRequest)RequestContextHolder
          .getRequestAttributes();
        final String srid = requestAttributes.getParameter("srid");
        if (srid != null && srid.trim().length() > 0) {
          factory = GeometryFactory.floating3d(Integer.parseInt(srid));
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
      Logs.error(this, "Error reading data using " + mediaType, e);
      throw new HttpMessageNotReadableException("Error reading data using" + mediaType);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void write(final Geometry geometry, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    if (!HttpServletUtils.getResponse().isCommitted()) {
      MediaType actualMediaType;
      if (mediaType == null) {
        actualMediaType = getDefaultMediaType();
      } else {
        actualMediaType = mediaType;
      }
      if (actualMediaType != null) {
        final Charset charset = HttpServletUtils.setContentTypeWithCharset(outputMessage,
          actualMediaType);
        final OutputStream body = outputMessage.getBody();
        final String mediaTypeString = actualMediaType.getType() + "/"
          + actualMediaType.getSubtype();
        final GeometryWriterFactory writerFactory = IoFactory
          .factoryByMediaType(GeometryWriterFactory.class, mediaTypeString);
        if (writerFactory == null) {
          throw new IllegalArgumentException("Media type " + actualMediaType + " not supported");
        } else {
          final String baseName = HttpServletUtils.getRequestBaseFileName();
          final GeometryWriter writer = writerFactory.newGeometryWriter(baseName, body, charset);
          writer.write(geometry);
          writer.close();
        }
      }
    }
  }
}
