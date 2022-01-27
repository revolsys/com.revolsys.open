package com.revolsys.util;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.http.NameValuePair;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.message.ParserCursor;
import org.apache.http.message.TokenParser;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.TextUtils;

/**
 * Builder for {@link URI} instances.
 *
 * @since 4.2
 */
public class UriBuilder {

  private static final char PATH_SEPARATOR = '/';

  private static final BitSet PATH_SEPARATORS = new BitSet(256);

  /** Characters which are safe to use in a query or a fragment,
   * i.e. {@link #RESERVED} plus {@link #UNRESERVED} */
  private static final BitSet URIC = new BitSet(256);

  private static final BitSet PATH_SPECIAL = new BitSet(256);

  private static final int RADIX = 16;

  /**
   * Reserved characters, i.e. {@code ;/?:@&=+$,[]}
   * <p>
   *  This list is the same as the {@code reserved} list in
   *  <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
   *  as augmented by
   *  <a href="http://www.ietf.org/rfc/rfc2732.txt">RFC 2732</a>
   */
  private static final BitSet RESERVED = new BitSet(256);

  /**
   * Unreserved characters, i.e. alphanumeric, plus: {@code _ - ! . ~ ' ( ) *}
   * <p>
   *  This list is the same as the {@code unreserved} list in
   *  <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396</a>
   */
  private static final BitSet UNRESERVED = new BitSet(256);

  /**
   * Punctuation characters: , ; : $ & + =
   * <p>
   * These are the additional characters allowed by userinfo.
   */
  private static final BitSet PUNCT = new BitSet(256);

  /** Characters which are safe to use in a path,
   * i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation plus / @ */
  private static final BitSet PATHSAFE = new BitSet(256);

  /** Characters which are safe to use in userinfo,
   * i.e. {@link #UNRESERVED} plus {@link #PUNCT}uation */
  private static final BitSet USERINFO = new BitSet(256);

  /**
   * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
   * i.e. alphanumeric plus {@code "-", "_", ".", "*"}
   */
  private static final BitSet URLENCODER = new BitSet(256);

  /**
   * Safe characters for x-www-form-urlencoded data, as per java.net.URLEncoder and browser behaviour,
   * i.e. alphanumeric plus {@code "-", "_", ".", "*"}
   */

  static {
    // unreserved chars
    // alpha characters
    for (int i = 'a'; i <= 'z'; i++) {
      UNRESERVED.set(i);
    }
    for (int i = 'A'; i <= 'Z'; i++) {
      UNRESERVED.set(i);
    }
    // numeric characters
    for (int i = '0'; i <= '9'; i++) {
      UNRESERVED.set(i);
    }
    UNRESERVED.set('_'); // these are the charactes of the "mark" list
    UNRESERVED.set('-');
    UNRESERVED.set('.');
    UNRESERVED.set('*');
    URLENCODER.or(UNRESERVED); // skip remaining unreserved characters
    UNRESERVED.set('!');
    UNRESERVED.set('~');
    UNRESERVED.set('\'');
    UNRESERVED.set('(');
    UNRESERVED.set(')');
    // punct chars
    PUNCT.set(',');
    PUNCT.set(';');
    PUNCT.set(':');
    PUNCT.set('$');
    PUNCT.set('&');
    PUNCT.set('+');
    PUNCT.set('=');
    // Safe for userinfo
    USERINFO.or(UNRESERVED);
    USERINFO.or(PUNCT);

    // URL path safe
    PATHSAFE.or(UNRESERVED);
    PATHSAFE.set(';'); // param separator
    PATHSAFE.set(':'); // RFC 2396
    PATHSAFE.set('@');
    PATHSAFE.set('&');
    PATHSAFE.set('=');
    PATHSAFE.set('+');
    PATHSAFE.set('$');
    PATHSAFE.set(',');

    PATH_SPECIAL.or(PATHSAFE);
    PATH_SPECIAL.set('/');

    RESERVED.set(';');
    RESERVED.set('/');
    RESERVED.set('?');
    RESERVED.set(':');
    RESERVED.set('@');
    RESERVED.set('&');
    RESERVED.set('=');
    RESERVED.set('+');
    RESERVED.set('$');
    RESERVED.set(',');
    RESERVED.set('['); // added by RFC 2732
    RESERVED.set(']'); // added by RFC 2732

    URIC.or(RESERVED);
    URIC.or(UNRESERVED);

    PATH_SEPARATORS.set(PATH_SEPARATOR);
  }

  private String scheme;

  private String encodedSchemeSpecificPart;

  private String encodedAuthority;

  private String userInfo;

  private String encodedUserInfo;

  private String host;

  private int port = -1;

  private String encodedPath;

  private List<String> pathSegments;

  private String encodedQuery;

  private List<NameValuePair> queryParams;

  private String query;

  private Charset charset = StandardCharsets.UTF_8;

  private String fragment;

  private String encodedFragment;

  /**
   * Constructs an empty instance.
   */
  public UriBuilder() {
  }

  /**
   * Construct an instance from the string which must be a valid URI.
   *
   * @param uri a valid URI in string form
   */
  public UriBuilder(final String uri) {
    this(uri, null);
  }

  /**
   * Construct an instance from the string which must be a valid URI.
   *
   * @param string a valid URI in string form
   */
  public UriBuilder(final String uri, final Charset charset) {
    this(URI.create(uri), charset);
  }

  /**
  * Construct an instance from the provided URI.
  * @param uri
  */
  public UriBuilder(final URI uri) {
    this(uri, null);
  }

  /**
   * Construct an instance from the provided URI.
   * @param uri
   */
  public UriBuilder(final URI uri, final Charset charset) {
    setCharset(charset);
    this.scheme = uri.getScheme();
    this.encodedSchemeSpecificPart = uri.getRawSchemeSpecificPart();
    this.encodedAuthority = uri.getRawAuthority();
    this.host = uri.getHost();
    this.port = uri.getPort();
    this.encodedUserInfo = uri.getRawUserInfo();
    this.userInfo = uri.getUserInfo();
    this.encodedPath = uri.getRawPath();
    this.pathSegments = parsePath(uri.getRawPath());
    this.encodedQuery = uri.getRawQuery();
    this.queryParams = parseQuery(uri.getRawQuery());
    this.encodedFragment = uri.getRawFragment();
    this.fragment = uri.getFragment();
  }

  /**
   * Adds parameter to URI query. The parameter name and value are expected to be unescaped
   * and may contain non ASCII characters.
   * <p>
   * Please note query parameters and custom query component are mutually exclusive. This method
   * will remove custom query if present.
   * </p>
   */
  public UriBuilder addParameter(final String param, final String value) {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList<NameValuePair>();
    }
    this.queryParams.add(new BasicNameValuePair(param, value));
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    this.query = null;
    return this;
  }

  /**
   * Adds URI query parameters. The parameter name / values are expected to be unescaped
   * and may contain non ASCII characters.
   * <p>
   * Please note query parameters and custom query component are mutually exclusive. This method
   * will remove custom query if present.
   * </p>
   *
   * @since 4.3
   */
  public UriBuilder addParameters(final List<NameValuePair> nvps) {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList<NameValuePair>();
    }
    this.queryParams.addAll(nvps);
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    this.query = null;
    return this;
  }

  public UriBuilder appendPathSegments(final String... segments) {
    for (final String segment : segments) {
      this.pathSegments.add(segment);
    }
    this.encodedSchemeSpecificPart = null;
    this.encodedPath = null;
    return this;
  }

  private void appendUrlEncode(final StringBuilder sb, final String content, final BitSet safechars,
    final boolean blankAsPlus) {
    if (content != null) {
      final ByteBuffer bb = this.charset.encode(content);
      while (bb.hasRemaining()) {
        final int b = bb.get() & 0xff;
        if (safechars.get(b)) {
          sb.append((char)b);
        } else if (blankAsPlus && b == ' ') {
          sb.append('+');
        } else {
          sb.append("%");
          final char hex1 = Character.toUpperCase(Character.forDigit(b >> 4 & 0xF, RADIX));
          final char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
          sb.append(hex1);
          sb.append(hex2);
        }
      }
    }
  }

  /**
   * Builds a {@link URI} instance.
   */
  public URI build() {
    final String uriString = buildString();
    return URI.create(uriString);
  }

  public String buildString() {
    final StringBuilder sb = new StringBuilder();
    if (this.scheme != null) {
      sb.append(this.scheme).append(':');
    }
    if (this.encodedSchemeSpecificPart != null) {
      sb.append(this.encodedSchemeSpecificPart);
    } else {
      if (this.encodedAuthority != null) {
        sb.append("//").append(this.encodedAuthority);
      } else if (this.host != null) {
        sb.append("//");
        if (this.encodedUserInfo != null) {
          sb.append(this.encodedUserInfo).append("@");
        } else if (this.userInfo != null) {
          appendUrlEncode(sb, this.userInfo, USERINFO, false);
          sb.append("@");
        }
        if (InetAddressUtils.isIPv6Address(this.host)) {
          sb.append("[").append(this.host).append("]");
        } else {
          sb.append(this.host);
        }
        if (this.port >= 0) {
          sb.append(":").append(this.port);
        }
      }
      if (this.encodedPath != null) {
        sb.append(normalizePath(this.encodedPath, sb.length() == 0));
      } else if (this.pathSegments != null) {
        for (final String segment : this.pathSegments) {
          sb.append(PATH_SEPARATOR);
          appendUrlEncode(sb, segment, PATHSAFE, false);
        }
      }
      if (this.encodedQuery != null) {
        sb.append("?").append(this.encodedQuery);
      } else if (this.queryParams != null && !this.queryParams.isEmpty()) {
        sb.append("?");
        encodeUrlForm(sb, this.queryParams);
      } else if (this.query != null) {
        sb.append("?");
        appendUrlEncode(sb, this.query, PATHSAFE, false);
      }
    }
    if (this.encodedFragment != null) {
      sb.append("#").append(this.encodedFragment);
    } else if (this.fragment != null) {
      sb.append("#");
      appendUrlEncode(sb, this.fragment, URIC, false);
    }
    return sb.toString();
  }

  /**
   * Clears URI query parameters.
   *
   * @since 4.3
   */
  public UriBuilder clearParameters() {
    this.queryParams = null;
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    return this;
  }

  private void encodeUrlForm(final StringBuilder result, final List<NameValuePair> params) {
    boolean first = true;
    for (final NameValuePair parameter : params) {
      final String name = parameter.getName();
      final String value = parameter.getValue();
      if (first) {
        first = false;
      } else {
        result.append('&');
      }
      if (name != null) {
        appendUrlEncode(result, name, URLENCODER, true);
      }
      if (value != null) {
        result.append("=");
        appendUrlEncode(result, value, URLENCODER, false);
      }
    }
  }

  /**
   * @since 4.4
   */
  public Charset getCharset() {
    return this.charset;
  }

  public String getFragment() {
    return this.fragment;
  }

  public String getHost() {
    return this.host;
  }

  public String getPath() {
    if (this.pathSegments == null) {
      return null;
    }
    final StringBuilder result = new StringBuilder();
    for (final String segment : this.pathSegments) {
      result.append('/').append(segment);
    }
    return result.toString();
  }

  /**
   * @since 4.5.8
   */
  public List<String> getPathSegments() {
    return this.pathSegments != null ? new ArrayList<String>(this.pathSegments)
      : Collections.<String> emptyList();
  }

  public int getPort() {
    return this.port;
  }

  public List<NameValuePair> getQueryParams() {
    return this.queryParams != null ? new ArrayList<NameValuePair>(this.queryParams)
      : Collections.<NameValuePair> emptyList();
  }

  public String getScheme() {
    return this.scheme;
  }

  public String getUserInfo() {
    return this.userInfo;
  }

  /**
   * @since 4.3
   */
  public boolean isAbsolute() {
    return this.scheme != null;
  }

  /**
   * @since 4.3
   */
  public boolean isOpaque() {
    return this.pathSegments == null && this.encodedPath == null;
  }

  /**
   * @since 4.5.8
   */
  public boolean isPathEmpty() {
    return (this.pathSegments == null || this.pathSegments.isEmpty())
      && (this.encodedPath == null || this.encodedPath.isEmpty());
  }

  /**
   * @since 4.5.8
   */
  public boolean isQueryEmpty() {
    return (this.queryParams == null || this.queryParams.isEmpty()) && this.encodedQuery == null;
  }

  private String normalizePath(final String path, final boolean relative) {
    String s = path;
    if (TextUtils.isBlank(s)) {
      return "";
    }
    if (!relative && !s.startsWith("/")) {
      s = "/" + s;
    }
    return s;
  }

  private List<String> parsePath(final String path) {
    final List<String> list = splitPathSegments(path);
    if (list != null) {
      for (final ListIterator<String> iterator = list.listIterator(); iterator.hasNext();) {
        String element = iterator.next();
        if (element.isEmpty()) {
          iterator.remove();
        } else {
          element = urlDecode(element, false);
          iterator.set(element);
        }
      }
    }
    return list;
  }

  private List<NameValuePair> parseQuery(final String query) {
    if (query != null && !query.isEmpty()) {
      final CharArrayBuffer buffer = new CharArrayBuffer(query.length());
      buffer.append(query);
      final TokenParser tokenParser = TokenParser.INSTANCE;
      final BitSet delimSet = new BitSet();
      delimSet.set(';');
      delimSet.set('&');
      final ParserCursor cursor = new ParserCursor(0, buffer.length());
      final List<NameValuePair> list = new ArrayList<NameValuePair>();
      while (!cursor.atEnd()) {
        delimSet.set('=');
        final String name = tokenParser.parseToken(buffer, cursor, delimSet);
        String value = null;
        if (!cursor.atEnd()) {
          final int delim = buffer.charAt(cursor.getPos());
          cursor.updatePos(cursor.getPos() + 1);
          if (delim == '=') {
            delimSet.clear('=');
            value = tokenParser.parseToken(buffer, cursor, delimSet);
            if (!cursor.atEnd()) {
              cursor.updatePos(cursor.getPos() + 1);
            }
          }
        }
        if (!name.isEmpty()) {
          final String content = value;
          list.add(new BasicNameValuePair(urlDecode(name, true), urlDecode(content, true)));
        }
      }
      return list;
    }
    return null;
  }

  /**
   * Removes URI query.
   */
  public UriBuilder removeQuery() {
    this.queryParams = null;
    this.query = null;
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    return this;
  }

  /**
   * @since 4.4
   */
  public UriBuilder setCharset(final Charset charset) {
    if (charset == null) {
      this.charset = StandardCharsets.UTF_8;
    } else {
      this.charset = charset;
    }
    return this;
  }

  /**
   * Sets custom URI query. The value is expected to be unescaped and may contain non ASCII
   * characters.
   * <p>
   * Please note query parameters and custom query component are mutually exclusive. This method
   * will remove query parameters if present.
   * </p>
   *
   * @since 4.3
   */
  public UriBuilder setCustomQuery(final String query) {
    this.query = query;
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    this.queryParams = null;
    return this;
  }

  /**
   * Sets URI fragment. The value is expected to be unescaped and may contain non ASCII
   * characters.
   */
  public UriBuilder setFragment(final String fragment) {
    this.fragment = fragment;
    this.encodedFragment = null;
    return this;
  }

  /**
   * Sets URI host.
   */
  public UriBuilder setHost(final String host) {
    this.host = host;
    this.encodedSchemeSpecificPart = null;
    this.encodedAuthority = null;
    return this;
  }

  public UriBuilder setParameter(final String param, final Object value) {
    final String string = value == null ? null : value.toString();
    return setParameter(param, string);
  }

  /**
   * Sets parameter of URI query overriding existing value if set. The parameter name and value
   * are expected to be unescaped and may contain non ASCII characters.
   * <p>
   * Please note query parameters and custom query component are mutually exclusive. This method
   * will remove custom query if present.
   * </p>
   */
  public UriBuilder setParameter(final String param, final String value) {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList<NameValuePair>();
    }
    if (!this.queryParams.isEmpty()) {
      for (final Iterator<NameValuePair> it = this.queryParams.iterator(); it.hasNext();) {
        final NameValuePair nvp = it.next();
        if (nvp.getName().equals(param)) {
          it.remove();
        }
      }
    }
    this.queryParams.add(new BasicNameValuePair(param, value));
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    this.query = null;
    return this;
  }

  /**
   * Sets URI query parameters. The parameter name / values are expected to be unescaped
   * and may contain non ASCII characters.
   * <p>
   * Please note query parameters and custom query component are mutually exclusive. This method
   * will remove custom query if present.
   * </p>
   *
   * @since 4.3
   */
  public UriBuilder setParameters(final List<NameValuePair> nvps) {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList<NameValuePair>();
    } else {
      this.queryParams.clear();
    }
    this.queryParams.addAll(nvps);
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    this.query = null;
    return this;
  }

  /**
   * Sets URI query parameters. The parameter name / values are expected to be unescaped
   * and may contain non ASCII characters.
   * <p>
   * Please note query parameters and custom query component are mutually exclusive. This method
   * will remove custom query if present.
   * </p>
   *
   * @since 4.3
   */
  public UriBuilder setParameters(final NameValuePair... nvps) {
    if (this.queryParams == null) {
      this.queryParams = new ArrayList<NameValuePair>();
    } else {
      this.queryParams.clear();
    }
    for (final NameValuePair nvp : nvps) {
      this.queryParams.add(nvp);
    }
    this.encodedQuery = null;
    this.encodedSchemeSpecificPart = null;
    this.query = null;
    return this;
  }

  /**
   * Sets URI path. The value is expected to be unescaped and may contain non ASCII characters.
   *
   * @return this.
   */
  public UriBuilder setPath(final String path) {
    final List<String> pathSegments = splitPathSegments(path);
    return setPathSegments(pathSegments);
  }

  /**
   * Sets URI path. The value is expected to be unescaped and may contain non ASCII characters.
   *
   * @return this.
   *
   * @since 4.5.8
   */
  public UriBuilder setPathSegments(final List<String> pathSegments) {
    this.pathSegments = pathSegments != null && pathSegments.size() > 0
      ? new ArrayList<String>(pathSegments)
      : null;
    this.encodedSchemeSpecificPart = null;
    this.encodedPath = null;
    return this;
  }

  /**
   * Sets URI path. The value is expected to be unescaped and may contain non ASCII characters.
   *
   * @return this.
   *
   * @since 4.5.8
   */
  public UriBuilder setPathSegments(final String... pathSegments) {
    this.pathSegments = pathSegments.length > 0 ? Arrays.asList(pathSegments) : null;
    this.encodedSchemeSpecificPart = null;
    this.encodedPath = null;
    return this;
  }

  /**
   * Sets URI port.
   */
  public UriBuilder setPort(final int port) {
    this.port = port < 0 ? -1 : port;
    this.encodedSchemeSpecificPart = null;
    this.encodedAuthority = null;
    return this;
  }

  /**
   * Sets URI scheme.
   */
  public UriBuilder setScheme(final String scheme) {
    this.scheme = scheme;
    return this;
  }

  /**
   * Sets URI user info. The value is expected to be unescaped and may contain non ASCII
   * characters.
   */
  public UriBuilder setUserInfo(final String userInfo) {
    this.userInfo = userInfo;
    this.encodedSchemeSpecificPart = null;
    this.encodedAuthority = null;
    this.encodedUserInfo = null;
    return this;
  }

  /**
   * Sets URI user info as a combination of username and password. These values are expected to
   * be unescaped and may contain non ASCII characters.
   */
  public UriBuilder setUserInfo(final String username, final String password) {
    return setUserInfo(username + ':' + password);
  }

  private List<String> splitPathSegments(final String s) {
    if (s == null || s.isEmpty()) {
      return null;
    } else {
      final ParserCursor cursor = new ParserCursor(0, s.length());
      // Skip leading separator
      if (cursor.atEnd()) {
        return Collections.emptyList();
      }
      if (PATH_SEPARATORS.get(s.charAt(cursor.getPos()))) {
        cursor.updatePos(cursor.getPos() + 1);
      }
      final List<String> list = new ArrayList<String>();
      final StringBuilder buf = new StringBuilder();
      for (;;) {
        if (cursor.atEnd()) {
          list.add(buf.toString());
          break;
        }
        final char current = s.charAt(cursor.getPos());
        if (PATH_SEPARATORS.get(current)) {
          list.add(buf.toString());
          buf.setLength(0);
        } else {
          buf.append(current);
        }
        cursor.updatePos(cursor.getPos() + 1);
      }
      return list;
    }
  }

  @Override
  public String toString() {
    return buildString();
  }

  /**
   * Decode/unescape a portion of a URL, to use with the query part ensure {@code plusAsBlank} is true.
  *
  * @param content the portion to decode
   * @param plusAsBlank if {@code true}, then convert '+' to space (e.g. for www-url-form-encoded content), otherwise leave as is.
   * @param charset the charset to use
  * @return encoded string
  */
  private String urlDecode(final String content, final boolean plusAsBlank) {
    if (content == null) {
      return null;
    }
    final ByteBuffer bb = ByteBuffer.allocate(content.length());
    final CharBuffer cb = CharBuffer.wrap(content);
    while (cb.hasRemaining()) {
      final char c = cb.get();
      if (c == '%' && cb.remaining() >= 2) {
        final char uc = cb.get();
        final char lc = cb.get();
        final int u = Character.digit(uc, 16);
        final int l = Character.digit(lc, 16);
        if (u != -1 && l != -1) {
          bb.put((byte)((u << 4) + l));
        } else {
          bb.put((byte)'%');
          bb.put((byte)uc);
          bb.put((byte)lc);
        }
      } else if (plusAsBlank && c == '+') {
        bb.put((byte)' ');
      } else {
        bb.put((byte)c);
      }
    }
    bb.flip();
    return this.charset.decode(bb).toString();
  }

}
