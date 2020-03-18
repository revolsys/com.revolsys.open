package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactory;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.record.Record;
import com.revolsys.record.io.RecordReader;
import com.revolsys.record.io.RecordReaderFactory;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.kml.Kml22Constants;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.spring.resource.InputStreamResource;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class RecordReaderHttpMessageConverter extends AbstractHttpMessageConverter<RecordReader> {

  private GeometryFactory geometryFactory;

  private List<String> requestAttributeNames = Arrays.asList(IoConstants.SINGLE_OBJECT_PROPERTY,
    Kml22Constants.STYLE_URL_PROPERTY, Kml22Constants.LOOK_AT_POINT_PROPERTY,
    Kml22Constants.LOOK_AT_RANGE_PROPERTY, Kml22Constants.LOOK_AT_MIN_RANGE_PROPERTY,
    Kml22Constants.LOOK_AT_MAX_RANGE_PROPERTY, IoConstants.JSONP_PROPERTY,
    IoConstants.TITLE_PROPERTY, IoConstants.DESCRIPTION_PROPERTY);

  public RecordReaderHttpMessageConverter() {
    super(RecordReader.class, IoFactory.mediaTypes(RecordReaderFactory.class),
      IoFactory.mediaTypes(RecordWriterFactory.class));
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public List<String> getRequestAttributeNames() {
    return this.requestAttributeNames;
  }

  @Override
  public RecordReader read(final Class<? extends RecordReader> clazz,
    final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
    try {
      final HttpHeaders headers = inputMessage.getHeaders();
      final MediaType mediaType = headers.getContentType();
      Charset charset = mediaType.getCharset();
      if (charset == null) {
        charset = StandardCharsets.UTF_8;
      }
      final InputStream body = inputMessage.getBody();
      final String mediaTypeString = mediaType.getType() + "/" + mediaType.getSubtype();
      final RecordReaderFactory readerFactory = IoFactory
        .factoryByMediaType(RecordReaderFactory.class, mediaTypeString);
      if (readerFactory == null) {
        throw new HttpMessageNotReadableException("Cannot read data in format" + mediaType);
      } else {
        final Reader<Record> reader = readerFactory
          .newRecordReader(new InputStreamResource("recordInput", body));

        GeometryFactory factory = this.geometryFactory;
        final ServletWebRequest requestAttributes = (ServletWebRequest)RequestContextHolder
          .getRequestAttributes();
        final String srid = requestAttributes.getParameter("srid");
        if (srid != null && srid.trim().length() > 0) {
          factory = GeometryFactory.floating3d(Integer.parseInt(srid));
        }
        reader.setProperty(IoConstants.GEOMETRY_FACTORY, factory);
        return (RecordReader)reader;
      }
    } catch (final IOException e) {
      throw new HttpMessageNotReadableException("Error reading data", e);
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  public void setRequestAttributeNames(final List<String> requestAttributeNames) {
    this.requestAttributeNames = requestAttributeNames;
  }

  @Override
  public void write(final RecordReader reader, final MediaType mediaType,
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
        final String mediaTypeString = actualMediaType.getType() + "/"
          + actualMediaType.getSubtype();
        final RecordWriterFactory writerFactory = IoFactory
          .factoryByMediaType(RecordWriterFactory.class, mediaTypeString);
        if (writerFactory == null) {
          throw new IllegalArgumentException("Media type " + actualMediaType + " not supported");
        } else {
          final RecordDefinition recordDefinition = reader.getRecordDefinition();
          final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
          String baseName = (String)requestAttributes.getAttribute("contentDispositionFileName",
            RequestAttributes.SCOPE_REQUEST);
          if (baseName == null) {
            baseName = HttpServletUtils.getRequestBaseFileName();
          }
          String contentDisposition = (String)requestAttributes.getAttribute("contentDisposition",
            RequestAttributes.SCOPE_REQUEST);
          if (contentDisposition == null) {
            contentDisposition = "attachment";
          }
          final String fileName = baseName + "." + writerFactory.getFileExtension(mediaTypeString);
          final HttpHeaders headers = outputMessage.getHeaders();
          headers.set("Content-Disposition", contentDisposition + "; filename=" + fileName);

          final OutputStream body = outputMessage.getBody();
          final Writer<Record> writer = writerFactory.newRecordWriter(baseName, recordDefinition,
            body, charset);
          if (Boolean.FALSE
            .equals(requestAttributes.getAttribute("wrapHtml", RequestAttributes.SCOPE_REQUEST))) {
            writer.setProperty(IoConstants.WRAP_PROPERTY, false);
          }
          final HttpServletRequest request = HttpServletUtils.getRequest();
          String callback = request.getParameter("jsonp");
          if (callback == null) {
            callback = request.getParameter("callback");
          }
          if (callback != null) {
            writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
          }
          for (final String attributeName : requestAttributes
            .getAttributeNames(RequestAttributes.SCOPE_REQUEST)) {
            final Object value = requestAttributes.getAttribute(attributeName,
              RequestAttributes.SCOPE_REQUEST);
            if (value != null && attributeName.startsWith("java:")
              || this.requestAttributeNames.contains(attributeName)) {
              writer.setProperty(attributeName, value);
            }
          }

          final Iterator<Record> iterator = reader.iterator();
          if (iterator.hasNext()) {
            Record record = iterator.next();
            final Geometry geometry = record.getGeometry();
            if (geometry != null) {
              final GeometryFactory geometryFactory = geometry.getGeometryFactory();
              writer.setProperty(IoConstants.GEOMETRY_FACTORY, geometryFactory);
            }

            writer.write(record);
            while (iterator.hasNext()) {
              record = iterator.next();
              writer.write(record);

            }
          }
          writer.close();
        }
      }
    }
  }
}
