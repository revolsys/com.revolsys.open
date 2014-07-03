package com.revolsys.gis.web.rest.converter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.data.io.DataObjectReader;
import com.revolsys.data.io.DataObjectReaderFactory;
import com.revolsys.data.io.DataObjectWriterFactory;
import com.revolsys.data.io.ListDataObjectReader;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;
import com.revolsys.ui.web.utils.HttpServletUtils;

public class DataObjectHttpMessageConverter extends
  AbstractHttpMessageConverter<Record> {

  private final DataObjectReaderHttpMessageConverter readerConverter = new DataObjectReaderHttpMessageConverter();

  public DataObjectHttpMessageConverter() {
    super(Record.class, IoFactoryRegistry.getInstance().getMediaTypes(
      DataObjectReaderFactory.class), IoFactoryRegistry.getInstance()
      .getMediaTypes(DataObjectWriterFactory.class));
  }

  public GeometryFactory getGeometryFactory() {
    return readerConverter.getGeometryFactory();
  }

  public List<String> getRequestAttributeNames() {
    return readerConverter.getRequestAttributeNames();
  }

  @Override
  public Record read(final Class<? extends Record> clazz,
    final HttpInputMessage inputMessage) throws IOException,
    HttpMessageNotReadableException {
    final DataObjectReader reader = readerConverter.read(
      DataObjectReader.class, inputMessage);
    try {
      for (final Record dataObject : reader) {
        return dataObject;
      }
      return null;
    } finally {
      reader.close();
    }
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    readerConverter.setGeometryFactory(geometryFactory);
  }

  public void setRequestAttributeNames(final List<String> requestAttributeNames) {
    readerConverter.setRequestAttributeNames(requestAttributeNames);
  }

  @Override
  public void write(final Record dataObject, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException,
    HttpMessageNotWritableException {
    if (!HttpServletUtils.getResponse().isCommitted()) {
      if (dataObject != null) {
        final RecordDefinition metaData = dataObject.getMetaData();
        final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        requestAttributes.setAttribute(IoConstants.SINGLE_OBJECT_PROPERTY,
          true, RequestAttributes.SCOPE_REQUEST);
        final ListDataObjectReader reader = new ListDataObjectReader(metaData,
          dataObject);
        readerConverter.write(reader, mediaType, outputMessage);
      }
    }
  }
}
