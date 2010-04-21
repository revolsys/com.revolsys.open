package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.projection.GeometryProjectionUtil;
import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectWriterFactory;
import com.revolsys.gis.data.io.Reader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.Writer;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpRequestUtils;
import com.vividsolutions.jts.geom.Geometry;

public class DataObjectHttpMessageConverter extends
  AbstractHttpMessageConverter<DataObject> {

  public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  private List<String> requestAttributeNames = Arrays.asList(
    IoConstants.STYLE_URL_PROPERTY, IoConstants.JSONP_PROPERTY,
    IoConstants.TITLE_PROPERTY,
    IoConstants.DESCRIPTION_PROPERTY);

  public DataObjectHttpMessageConverter() {
    super(DataObject.class,
      IoFactoryRegistry.INSTANCE.getMediaTypes(DataObjectReaderFactory.class),
      IoFactoryRegistry.INSTANCE.getMediaTypes(DataObjectWriterFactory.class));
  }

  public List<String> getRequestAttributeNames() {
    return requestAttributeNames;
  }

  @Override
  public DataObject read(
    final Class<? extends DataObject> clazz,
    final HttpInputMessage inputMessage)
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
      final DataObjectReaderFactory readerFactory = ioFactoryRegistry.getFactoryByMediaType(
        DataObjectReaderFactory.class, mediaTypeString);
      if (readerFactory == null) {
        throw new HttpMessageNotReadableException("Cannot read data in format"
          + mediaType);
      } else {
        final Reader<DataObject> reader = readerFactory.createDataObjectReader(
          body, charset);
        for (final DataObject dataObject : reader) {
          return dataObject;
        }
        return null;
      }
    } catch (final IOException e) {
      throw new HttpMessageNotReadableException("Error reading data", e);
    }
  }

  public void setRequestAttributeNames(
    final List<String> requestAttributeNames) {
    this.requestAttributeNames = requestAttributeNames;
  }

  @Override
  public void write(
    final DataObject dataObject,
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
      final String mediaTypeString = actualMediaType.getType() + "/"
        + actualMediaType.getSubtype();
      final DataObjectWriterFactory writerFactory = ioFactoryRegistry.getFactoryByMediaType(
        DataObjectWriterFactory.class, mediaTypeString);
      if (writerFactory == null) {
        throw new IllegalArgumentException("Media type " + actualMediaType
          + " not supported");
      } else {
        final DataObjectMetaData metaData = dataObject.getMetaData();
        final String baseName = HttpRequestUtils.getRequestBaseFileName();
        final HttpHeaders headers = outputMessage.getHeaders();
        final String fileName = baseName + "."
          + writerFactory.getFileExtension(mediaTypeString);
        headers.set("Content-Disposition", "inline; filename=" + fileName);

        final OutputStream body = outputMessage.getBody();
        final Writer<DataObject> writer = writerFactory.createDataObjectWriter(
          baseName, metaData, body, charset);

        final Geometry geometry = dataObject.getGeometryValue();
        if (geometry != null) {
          final CoordinateSystem coordinateSystem = GeometryProjectionUtil.getCoordinateSystem(geometry);
          writer.setProperty(IoConstants.SRID_PROPERTY,
            coordinateSystem.getId());
          writer.setProperty(IoConstants.COORDINATE_SYSTEM_PROPERTY,
            coordinateSystem);
        }
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (Boolean.FALSE.equals(requestAttributes.getAttribute("wrapHtml",
          RequestAttributes.SCOPE_REQUEST))) {
          writer.setProperty(IoConstants.WRAP_PROPERTY, false);
        }
        for (final String attributeName : requestAttributeNames) {
          final Object value = requestAttributes.getAttribute(attributeName,
            RequestAttributes.SCOPE_REQUEST);
          if (value != null) {
            writer.setProperty(attributeName, value);
          }
        }
        writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
        writer.write(dataObject);
        writer.close();
      }
    }
  }
}
