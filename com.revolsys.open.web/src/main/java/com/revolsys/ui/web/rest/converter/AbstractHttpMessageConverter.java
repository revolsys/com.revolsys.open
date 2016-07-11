package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public abstract class AbstractHttpMessageConverter<T> implements HttpMessageConverter<T> {

  public static List<MediaType> getMediaTypes(final Collection<?> contentTypes) {
    final List<MediaType> mediaTypes = new ArrayList<>();
    if (contentTypes != null) {
      for (final Object mediaTypeObject : contentTypes) {
        if (mediaTypeObject instanceof MediaType) {
          final MediaType mediaType = (MediaType)mediaTypeObject;
          mediaTypes.add(mediaType);

        } else if (mediaTypeObject != null) {
          mediaTypes.add(MediaType.parseMediaType(mediaTypeObject.toString()));
        }
      }
    }
    return mediaTypes;
  }

  private MediaType defaultMediaType;

  private final List<MediaType> readMediaTypes;

  private final List<Class<T>> supportedClasses;

  private List<MediaType> supportedMediaTypes = new ArrayList<>();

  private final List<MediaType> writeMediaTypes;

  public AbstractHttpMessageConverter(final Class<T> supportedClass,
    final Collection<?> readMediaTypes, final Collection<?> writeMediaTypes) {
    this.supportedClasses = Collections.singletonList(supportedClass);
    this.readMediaTypes = getMediaTypes(readMediaTypes);
    this.writeMediaTypes = getMediaTypes(writeMediaTypes);
    final Set<MediaType> mediaTypes = new TreeSet<>();
    mediaTypes.addAll(this.readMediaTypes);
    mediaTypes.addAll(this.writeMediaTypes);
    this.supportedMediaTypes = new ArrayList<>(mediaTypes);
  }

  @Override
  public boolean canRead(final Class<?> clazz, final MediaType mediaType) {
    if (supports(clazz)) {
      if (mediaType == null) {
        return isReadSupported(this.defaultMediaType);
      } else {
        return isReadSupported(mediaType);
      }
    } else {
      return false;
    }
  }

  @Override
  public boolean canWrite(final Class<?> clazz, final MediaType mediaType) {
    if (supports(clazz)) {
      if (mediaType == null) {
        return isWriteSupported(this.defaultMediaType);
      } else {
        return isWriteSupported(mediaType);
      }
    } else {
      return false;
    }
  }

  public MediaType getDefaultMediaType() {
    return this.defaultMediaType;
  }

  @Override
  public List<MediaType> getSupportedMediaTypes() {
    return this.supportedMediaTypes;
  }

  protected boolean isReadSupported(final MediaType mediaType) {
    if (mediaType == null) {
      return true;
    } else {
      for (final MediaType supportedMediaType : this.readMediaTypes) {
        if (supportedMediaType.includes(mediaType)) {
          return true;
        }
      }
      return false;
    }
  }

  protected boolean isWriteSupported(final MediaType mediaType) {
    if (mediaType == null) {
      return true;
    } else {
      for (final MediaType supportedMediaType : this.writeMediaTypes) {
        if (supportedMediaType.includes(mediaType)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public T read(final Class<? extends T> clazz, final HttpInputMessage inputMessage)
    throws IOException, HttpMessageNotReadableException {
    return null;
  }

  public void setDefaultMediaType(final MediaType defaultMediaType) {
    this.defaultMediaType = defaultMediaType;
  }

  protected boolean supports(final Class<?> clazz) {
    for (final Class<?> supportedClass : this.supportedClasses) {
      if (supportedClass.isAssignableFrom(clazz)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void write(final T object, final MediaType contentType,
    final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
  }
}
