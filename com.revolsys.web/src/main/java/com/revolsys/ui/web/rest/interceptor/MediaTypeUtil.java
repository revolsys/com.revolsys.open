package com.revolsys.ui.web.rest.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.WebUtils;

public class MediaTypeUtil {
  private static final String ACCEPT_HEADER = "Accept";

  static final String CONTENT_TYPE_HEADER = "Content-type";

  public static MediaType getMediaTypeFromFilename(
    Map<String, MediaType> extensionToMediaTypeMap,
    final String filename) {
    String extension = StringUtils.getFilenameExtension(filename);
    if (!StringUtils.hasText(extension)) {
      return null;
    }
    extension = extension.toLowerCase(Locale.ENGLISH);
    final MediaType mediaType = extensionToMediaTypeMap.get(extension);
    return mediaType;
  }

  public static MediaType getMediaTypeFromParameter(
    Map<String, MediaType> extensionToMediaTypeMap,
    final String parameterValue) {
    return extensionToMediaTypeMap.get(parameterValue.toLowerCase(Locale.ENGLISH));
  }

  public static List<MediaType> getAcceptedMediaTypes(
    final HttpServletRequest request,
    Map<String, MediaType> extensionToMediaTypeMap,
    final List<String> mediaTypeOrder,
    UrlPathHelper urlPathHelper,
    String parameterName,
    MediaType defaultMediaType) {
    List<MediaType> mediaTypes = new ArrayList<MediaType>();
    for (String source : mediaTypeOrder) {
      if (source.equals("pathExtension")) {
        final String requestUri = urlPathHelper.getRequestUri(request);
        final String filename = WebUtils.extractFullFilenameFromUrlPath(requestUri);
        final MediaType mediaType = getMediaTypeFromFilename(
          extensionToMediaTypeMap, filename);
        if (mediaType != null) {
          mediaTypes.add(mediaType);
        }
      } else if (source.equals("parameter")) {
        if (request.getParameter(parameterName) != null) {
          final String parameterValue = request.getParameter(parameterName);
          final MediaType mediaType = getMediaTypeFromParameter(
            extensionToMediaTypeMap, parameterValue);
          if (mediaType != null) {
            mediaTypes.add(mediaType);
          }
        }
      } else if (source.equals("acceptHeader")) {
        final String acceptHeader = request.getHeader(ACCEPT_HEADER);
        if (StringUtils.hasText(acceptHeader)) {
          mediaTypes.addAll(MediaType.parseMediaTypes(acceptHeader));
        }
      } else if (source.equals("defaultMediaType"))
        if (defaultMediaType != null) {
          mediaTypes.add(defaultMediaType);
        }
    }
    return mediaTypes;
  }

  public static MediaType getRequestMediaType(
    final HttpServletRequest request,
    Map<String, MediaType> extensionToMediaTypeMap,
    final List<String> mediaTypeOrder,
    UrlPathHelper urlPathHelper,
    String parameterName,
    MediaType defaultMediaType) {
    for (String source : mediaTypeOrder) {
      if (source.equals("pathExtension")) {
        final String requestUri = urlPathHelper.getRequestUri(request);
        final String filename = WebUtils.extractFullFilenameFromUrlPath(requestUri);
        final MediaType mediaType = getMediaTypeFromFilename(
          extensionToMediaTypeMap, filename);
        if (mediaType != null) {
          return mediaType;
        }
      } else if (source.equals("parameter")) {
        if (request.getParameter(parameterName) != null) {
          final String parameterValue = request.getParameter(parameterName);
          final MediaType mediaType = getMediaTypeFromParameter(
            extensionToMediaTypeMap, parameterValue);
          if (mediaType != null) {
            return mediaType;
          }
        }
      } else if (source.equals("acceptHeader")) {
        return getContentType(request);
      } else if (source.equals("defaultMediaType"))
        if (defaultMediaType != null) {
          return defaultMediaType;
        }
    }
    return null;
  }

  public static MediaType getContentType(
    final HttpServletRequest request) {
    final String contentTypeHeader = request.getHeader(CONTENT_TYPE_HEADER);
    if (StringUtils.hasText(contentTypeHeader)) {
      return MediaType.parseMediaType(contentTypeHeader);
    } else {
      return null;
    }
  }
}
