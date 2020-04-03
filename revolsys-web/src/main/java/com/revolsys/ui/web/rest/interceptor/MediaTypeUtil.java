package com.revolsys.ui.web.rest.interceptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoFactory;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.Property;

public class MediaTypeUtil {
  private static final String ACCEPT_HEADER = "Accept";

  public static final String CONTENT_TYPE_HEADER = "Content-type";

  private static Map<String, MediaType> extensionToMediaTypeMap = new HashMap<>();

  private static Map<MediaType, String> mediaTypeToExtensionMap = new HashMap<>();

  private static final UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  static {
    for (final Entry<String, String> entry : IoFactory.mediaTypeByFileExtension().entrySet()) {
      final String exetension = entry.getKey();
      final String mimeType = entry.getValue();
      final MediaType mediaType = MediaType.parseMediaType(mimeType);
      extensionToMediaTypeMap.put(exetension, mediaType);
      mediaTypeToExtensionMap.put(mediaType, exetension);
    }
  }

  public static void addMediaType(final List<MediaType> mediaTypes, final String extension) {
    final MediaType mediaType = getMediaTypeFromParameter(extensionToMediaTypeMap, extension);
    if (mediaType != null) {
      mediaTypes.add(mediaType);
    }
  }

  public static void addMediaTypeFromFilename(final List<MediaType> mediaTypes,
    final String filename) {
    final String extension = StringUtils.getFilenameExtension(filename);
    addMediaType(mediaTypes, extension);
  }

  public static String getAcceptedFileNameExtension() {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    final List<MediaType> mediaTypes = getAcceptedMediaTypes(request);
    for (final MediaType mediaType : mediaTypes) {
      final String extension = mediaTypeToExtensionMap.get(mediaType);
      if (Property.hasValue(extension)) {
        return extension;
      }
    }
    return "html";
  }

  public static List<MediaType> getAcceptedMediaTypes(final HttpServletRequest request) {
    final List<MediaType> mediaTypes = new ArrayList<>();

    String format = (String)request.getAttribute("format");
    if (Property.hasValue(format)) {
      addMediaType(mediaTypes, format);
    }
    format = request.getParameter("format");
    if (Property.hasValue(format)) {
      addMediaType(mediaTypes, format);
    }
    final String requestUri = URL_PATH_HELPER.getRequestUri(request);
    final String filename = FileUtil.getFileName(requestUri);
    addMediaTypeFromFilename(mediaTypes, filename);

    final String acceptHeader = request.getHeader(ACCEPT_HEADER);
    if (Property.hasValue(acceptHeader)) {
      mediaTypes.addAll(MediaType.parseMediaTypes(acceptHeader));
    }
    mediaTypes.add(MediaType.TEXT_HTML);
    return mediaTypes;
  }

  public static List<MediaType> getAcceptedMediaTypes(final HttpServletRequest request,
    final Map<String, MediaType> extensionToMediaTypeMap, final List<String> mediaTypeOrder,
    final UrlPathHelper urlPathHelper, final String parameterName,
    final MediaType defaultMediaType) {

    final List<MediaType> mediaTypes = new ArrayList<>();
    for (final String source : mediaTypeOrder) {
      if (source.equals("pathExtension")) {
        final String requestUri = urlPathHelper.getRequestUri(request);
        final String filename = FileUtil.getFileName(requestUri);
        final MediaType mediaType = getMediaTypeFromFilename(extensionToMediaTypeMap, filename);
        if (mediaType != null) {
          mediaTypes.add(mediaType);
        }
      } else if (source.equals("attribute")) {
        final String parameterValue = (String)request.getAttribute(parameterName);
        if (Property.hasValue(parameterValue)) {
          final MediaType mediaType = getMediaTypeFromParameter(extensionToMediaTypeMap,
            parameterValue);
          if (mediaType != null) {
            mediaTypes.add(mediaType);
          }
        }
      } else if (source.equals("parameter")) {
        final String parameterValue = request.getParameter(parameterName);
        if (Property.hasValue(parameterValue)) {
          final MediaType mediaType = getMediaTypeFromParameter(extensionToMediaTypeMap,
            parameterValue);
          if (mediaType != null) {
            mediaTypes.add(mediaType);
          }
        }
      } else if (source.equals("acceptHeader")) {
        final String acceptHeader = request.getHeader(ACCEPT_HEADER);
        if (Property.hasValue(acceptHeader)) {
          mediaTypes.addAll(MediaType.parseMediaTypes(acceptHeader));
        }
      } else if (source.equals("defaultMediaType")) {
        if (defaultMediaType != null) {
          mediaTypes.add(defaultMediaType);
        }
      }
    }
    return mediaTypes;
  }

  public static MediaType getContentType() {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    return getContentType(request);
  }

  public static MediaType getContentType(final HttpServletRequest request) {
    final String contentTypeHeader = request.getHeader(CONTENT_TYPE_HEADER);
    if (Property.hasValue(contentTypeHeader)) {
      return MediaType.parseMediaType(contentTypeHeader);
    } else {
      return null;
    }
  }

  public static MediaType getMediaTypeFromFilename(
    final Map<String, MediaType> extensionToMediaTypeMap, final String filename) {
    final String extension = StringUtils.getFilenameExtension(filename);
    return getMediaTypeFromParameter(extensionToMediaTypeMap, extension);
  }

  public static MediaType getMediaTypeFromParameter(
    final Map<String, MediaType> extensionToMediaTypeMap, final String extension) {
    if (!Property.hasValue(extension)) {
      return null;
    } else if (extension.matches("[^/]+/[^/]+")) {
      return MediaType.valueOf(extension);
    } else {
      final String lowerExtension = extension.toLowerCase(Locale.ENGLISH);
      final MediaType mediaType = extensionToMediaTypeMap.get(lowerExtension);
      return mediaType;
    }
  }

  public static String getPathWithExtension(final String path) {
    final String extension = getAcceptedFileNameExtension();
    final String pathWithExtension = path + "." + extension;
    return pathWithExtension;
  }

  public static MediaType getRequestMediaType(final HttpServletRequest request,
    final Map<String, MediaType> extensionToMediaTypeMap, final List<String> mediaTypeOrder,
    final UrlPathHelper urlPathHelper, final String parameterName, final MediaType defaultMediaType,
    final String fileName) {
    for (final String source : mediaTypeOrder) {
      if (source.equals("fileName")) {
        final MediaType mediaType = getMediaTypeFromFilename(extensionToMediaTypeMap, fileName);
        if (mediaType != null) {
          return mediaType;
        }
      } else if (source.equals("pathExtension")) {
        final String requestUri = urlPathHelper.getRequestUri(request);
        final String pathFileName = FileUtil.getFileName(requestUri);
        final MediaType mediaType = getMediaTypeFromFilename(extensionToMediaTypeMap, pathFileName);
        if (mediaType != null) {
          return mediaType;
        }
      } else if (source.equals("attribute")) {
        final String parameterValue = (String)request.getAttribute(parameterName);
        if (Property.hasValue(parameterValue)) {
          final MediaType mediaType = getMediaTypeFromParameter(extensionToMediaTypeMap,
            parameterValue);
          if (mediaType != null) {
            return mediaType;
          }
        }
      } else if (source.equals("parameter")) {
        if (request.getParameter(parameterName) != null) {
          final String parameterValue = request.getParameter(parameterName);
          final MediaType mediaType = getMediaTypeFromParameter(extensionToMediaTypeMap,
            parameterValue);
          if (mediaType != null) {
            return mediaType;
          }
        }
      } else if (source.equals("acceptHeader")) {
        return getContentType(request);
      } else if (source.equals("defaultMediaType")) {
        if (defaultMediaType != null) {
          return defaultMediaType;
        }
      }
    }
    return null;
  }

  public static String getUrlWithExtension(String path) {
    final String extension = "." + getAcceptedFileNameExtension();
    path = path.replaceAll("/+$", "");
    if (!path.endsWith(extension)) {
      path += extension;
    }
    if (path.startsWith("http")) {
      return path;
    } else {
      String baseUrl = HttpServletUtils.getFullRequestUrl();

      if (baseUrl.endsWith(extension)) {
        baseUrl = baseUrl.substring(0, baseUrl.length() - extension.length());
      }
      if (!path.startsWith("/") && !baseUrl.endsWith("/")) {
        baseUrl += '/';
      }
      return baseUrl + path;
    }
  }

  public static boolean isContentType(final MediaType contentType) {
    return contentType.equals(getContentType());
  }

  public static boolean isHtmlPage() {
    final HttpServletRequest request = HttpServletUtils.getRequest();
    final String format = request.getParameter("format");
    if (Property.hasValue(format)) {
      return false;
    }
    final String requestUri = URL_PATH_HELPER.getRequestUri(request);
    final String filename = FileUtil.getFileName(requestUri);
    final String extension = StringUtils.getFilenameExtension(filename);
    if (Property.hasValue(extension)) {
      return false;
    }

    final String acceptHeader = request.getHeader(ACCEPT_HEADER);
    if (Property.hasValue(acceptHeader)) {
      for (final MediaType mediaType : MediaType.parseMediaTypes(acceptHeader)) {
        if (mediaType.includes(MediaType.TEXT_HTML)) {
          return true;
        }
      }
      return false;
    } else {
      return true;
    }
  }

  public static boolean isPreferedMediaType(final HttpServletRequest request,
    final MediaType mediaType) {
    final List<MediaType> mediaTypes = getAcceptedMediaTypes(request);
    if (mediaTypes.isEmpty()) {
      return true;
    } else {
      for (final MediaType acceptedMediaType : mediaTypes) {
        if (acceptedMediaType.includes(mediaType)) {
          return true;
        } else if (!acceptedMediaType.isWildcardType() && !acceptedMediaType.isWildcardSubtype()) {
          return false;
        }
      }
      return false;

    }
  }
}
