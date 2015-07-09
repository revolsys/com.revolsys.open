package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import com.revolsys.data.io.GeometryReader;
import com.revolsys.gis.geometry.io.GeometryReaderFactory;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.spring.InputStreamResource;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;

public class GeometryReaderHttpMessageConverter
  extends AbstractHttpMessageConverter<GeometryReader> {

  private GeometryFactory geometryFactory;

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();

  public GeometryReaderHttpMessageConverter() {
    super(GeometryReader.class,
      IoFactoryRegistry.getInstance().getMediaTypes(GeometryReaderFactory.class), null);
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public GeometryReader read(final Class<? extends GeometryReader> clazz,
    final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    try {
      final HttpHeaders headers = inputMessage.getHeaders();
      final MediaType mediaType = headers.getContentType();
      Charset charset = mediaType.getCharSet();
      if (charset == null) {
        charset = StandardCharsets.UTF_8;
      }
      final InputStream body = inputMessage.getBody();
      final String mediaTypeString = mediaType.getType() + "/" + mediaType.getSubtype();
      final GeometryReaderFactory readerFactory = this.ioFactoryRegistry
        .getFactoryByMediaType(GeometryReaderFactory.class, mediaTypeString);
      if (readerFactory == null) {
        throw new HttpMessageNotReadableException("Cannot read data in format" + mediaType);
      } else {
        final InputStreamResource resource = new InputStreamResource("geometryInput", body);
        final GeometryReader reader = readerFactory.createGeometryReader(resource);
        GeometryFactory factory = this.geometryFactory;
        final ServletWebRequest requestAttributes = (ServletWebRequest)RequestContextHolder
          .getRequestAttributes();
        final String srid = requestAttributes.getParameter("srid");
        if (srid != null && srid.trim().length() > 0) {
          factory = GeometryFactory.floating3(Integer.parseInt(srid));
        }
        reader.setProperty(IoConstants.GEOMETRY_FACTORY, factory);
        return reader;
      }
    } catch (final IOException e) {
      throw new HttpMessageNotReadableException("Error reading data", e);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }
}
