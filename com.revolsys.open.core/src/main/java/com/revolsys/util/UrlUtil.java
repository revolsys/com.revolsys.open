/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.revolsys.io.FileUtil;

/**
 * The UrlUtil class is a utility class for processing and create URL strings.
 * 
 * @author Paul Austin
 */
public final class UrlUtil {

  private static final String DOMAIN_PART = "\\p{Alpha}[\\p{Alpha}0-9\\-]*\\.";

  private static final String TLD = "\\p{Alpha}+";

  private static final String DOMAIN_NAME = "(?:" + DOMAIN_PART + ")+" + TLD;

  private static final String IP4_ADDRESS = "\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";

  private static final String DOMAIN = "(?:" + IP4_ADDRESS + "|" + DOMAIN_NAME
    + ")";

  private static final String WORD_CHARACTERS = "a-zA-Z0-9\\+!#$%&'*+-/=?^_`{}|~";

  private static final String LOCAL_PART = "[" + WORD_CHARACTERS + "]["
    + WORD_CHARACTERS + "\\.]*[" + WORD_CHARACTERS + "]?";

  private static final String EMAIL_RE = "^(" + LOCAL_PART + ")@(" + DOMAIN
    + ")$";

  private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_RE);

  public static String getContent(final String urlString) {
    try {
      final URL url = UrlUtil.getUrl(urlString);
      final InputStream in = url.openStream();
      return FileUtil.getString(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read " + urlString);
    }
  }

  public static String getFileBaseName(final URL url) {
    final String name = getFileName(url);
    final int dotIndex = name.lastIndexOf('.');
    if (dotIndex != -1) {
      return name.substring(0, dotIndex);
    } else {
      return name;
    }
  }

  public static String getFileName(final String url) {
    return getFileName(getUrl(url));
  }

  public static String getFileName(final URL url) {
    final String path = url.getPath();
    final int index = path.lastIndexOf('/');
    if (index != -1) {
      return path.substring(index + 1);
    } else {
      return path;
    }
  }

  public static InputStream getInputStream(final String urlString) {
    final URL url = getUrl(urlString);
    return getInputStream(url);
  }

  public static InputStream getInputStream(final URL url) {
    try {
      return url.openStream();
    } catch (final IOException e) {
      throw new IllegalArgumentException("Cannot open stream for: " + url, e);
    }
  }

  public static URL getParent(final URL url) {
    final String urlString = url.toString();
    final int index = urlString.lastIndexOf('/');
    if (index != -1) {
      final String parentPath = urlString.substring(0, index);
      return getUrl(parentPath);
    } else {
      return url;
    }
  }

  public static String getParentString(final URL url) {
    final String urlString = url.toString();
    return getParent(urlString);
  }

  public static String getParent(final String urlString) {
    final int index = urlString.lastIndexOf('/');
    if (index != -1) {
      final String parentPath = urlString.substring(0, index);
      return parentPath;
    } else {
      return urlString;
    }
  }

  /**
   * Create a new URL from the baseUrl with the additional query string
   * parameters.
   * 
   * @param baseUrl The baseUrl.
   * @param parameters The additional parameters to add to the query string.
   * @return The new URL.
   */
  public static String getUrl(final Object baseUrl,
    final Map<String, ? extends Object> parameters) {
    return getUrl(baseUrl.toString(), parameters);
  }

  public static URL getUrl(final String urlString) {
    try {
      return new URL(urlString);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Unknown URL", e);
    }
  }

  /**
   * Create a new URL from the baseUrl with the additional query string
   * parameters.
   * 
   * @param baseUrl The baseUrl.
   * @param parameters The additional parameters to add to the query string.
   * @return The new URL.
   */
  public static String getUrl(final String baseUrl,
    final Map<String, ? extends Object> parameters) {
    final StringBuffer query = new StringBuffer();
    if (parameters != null) {
      boolean firstParameter = true;
      for (final Entry<String, ? extends Object> parameter : parameters.entrySet()) {
        final String name = parameter.getKey();
        final Object value = parameter.getValue();
        if (name != null && value != null) {
          if (!firstParameter) {
            query.append('&');
          } else {
            firstParameter = false;
          }
          try {
            if (value instanceof String[]) {
              final String[] values = (String[])value;
              for (int i = 0; i < values.length; i++) {
                query.append(name)
                  .append('=')
                  .append(URLEncoder.encode(values[i], "US-ASCII"));
                if (i < values.length - 1) {
                  query.append('&');
                }
              }
            } else if (value instanceof List) {
              final List values = (List)value;
              for (int i = 0; i < values.size(); i++) {
                query.append(name)
                  .append('=')
                  .append(URLEncoder.encode((String)values.get(i), "US-ASCII"));
                if (i < values.size() - 1) {
                  query.append('&');
                }
              }
            } else {
              query.append(name)
                .append('=')
                .append(URLEncoder.encode(value.toString(), "US-ASCII"));
            }
          } catch (final UnsupportedEncodingException e) {
            throw new Error(e);
          }

        }
      }
    }
    if (query.length() == 0) {
      return baseUrl;
    } else {
      final int qsIndex = baseUrl.indexOf('?');
      if (qsIndex == baseUrl.length() - 1) {
        return baseUrl + query;
      } else if (qsIndex > -1) {
        return baseUrl + '&' + query;
      } else {
        return baseUrl + '?' + query;
      }
    }
  }

  public static boolean isValidEmail(final String email) {
    return EMAIL_PATTERN.matcher(email).matches();
  }

  public static Map<String, String> parseMatrixParams(final String matrixParams) {
    final Map<String, String> params = new LinkedHashMap<String, String>();
    parseMatrixParams(matrixParams, params);
    return params;
  }

  public static void parseMatrixParams(final String matrixParams,
    final Map<String, String> params) {
    for (final String param : matrixParams.split(";")) {
      final String[] paramParts = param.split("=");
      final String key = paramParts[0];
      if (paramParts.length == 1) {
        params.put(key, null);
      } else {
        final String value = paramParts[1];
        params.put(key, value);
      }
    }
  }

  /**
   * Construct a new UrlUtil.
   */
  private UrlUtil() {
  }
}
