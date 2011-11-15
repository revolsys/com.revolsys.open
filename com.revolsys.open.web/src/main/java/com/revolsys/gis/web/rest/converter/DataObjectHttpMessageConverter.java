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

import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.gis.data.io.DataObjectReaderFactory;
import com.revolsys.gis.data.io.DataObjectWriterFactory;
import com.revolsys.gis.data.io.ListDataObjectReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.ui.web.rest.converter.AbstractHttpMessageConverter;

public class DataObjectHttpMessageConverter extends
  AbstractHttpMessageConverter<DataObject> {

  private final DataObjectReaderHttpMessageConverter readerConverter = new DataObjectReaderHttpMessageConverter();

  public DataObjectHttpMessageConverter() {
    super(DataObject.class,
      IoFactoryRegistry.INSTANCE.getMediaTypes(DataObjectReaderFactory.class),
      IoFactoryRegistry.INSTANCE.getMediaTypes(DataObjectWriterFactory.class));
  }

  public GeometryFactory getGeometryFactory() {
    return readerConverter.getGeometryFactory();
  }

  public List<String> getRequestAttributeNames() {
    return readerConverter.getRequestAttributeNames();
  }

  @Override
  public DataObject read(
    final Class<? extends DataObject> clazz,
    final HttpInputMessage inputMessage)
    throws IOException,
    HttpMessageNotReadableException {
    final DataObjectReader reader = readerConverter.read(
      DataObjectReader.class, inputMessage);
    try {
      for (final DataObject dataObject : reader) {
        return dataObject;
      }
      return null;
    } finally {
      reader.close();
    }
  }

  public void setGeometryFactory(
    final GeometryFactory geometryFactory) {
    readerConverter.setGeometryFactory(geometryFactory);
  }

  public void setRequestAttributeNames(
    final List<String> requestAttributeNames) {
    readerConverter.setRequestAttributeNames(requestAttributeNames);
  }

  @Override
  public void write(
    final DataObject dataObject,
    final MediaType mediaType,
    final HttpOutputMessage outputMessage)
    throws IOException,
    HttpMessageNotWritableException {
    if (dataObject != null) {
      final DataObjectMetaData metaData = dataObject.getMetaData();
      final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
      requestAttributes.setAttribute(IoConstants.SINGLE_OBJECT_PROPERTY, true,
        RequestAttributes.SCOPE_REQUEST);
      final ListDataObjectReader reader = new ListDataObjectReader(metaData,
        dataObject);
      readerConverter.write(reader, mediaType, outputMessage);
    }
  }
}
