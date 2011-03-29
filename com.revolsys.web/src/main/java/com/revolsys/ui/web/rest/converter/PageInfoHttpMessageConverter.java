package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.util.UrlPathHelper;

import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.NamedLinkedHashMap;
import com.revolsys.ui.model.DocInfo;
import com.revolsys.ui.model.PageInfo;
import com.revolsys.ui.model.ParameterInfo;
import com.revolsys.ui.web.utils.HttpRequestUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.xml.XmlContants;
import com.revolsys.xml.io.XmlWriter;
import com.revolsys.xml.wadl.WadlConstants;

public class PageInfoHttpMessageConverter extends
  AbstractHttpMessageConverter<PageInfo> implements WadlConstants {
  private static final MediaType APPLICATION_VND_SUN_WADL_XML = MediaType.parseMediaType("application/vnd.sun.wadl+xml");

  private final UrlPathHelper urlPathHelper = new UrlPathHelper();

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  private static final MediaType TEXT_URI_LIST = MediaType.parseMediaType("text/uri-list");

  private static final Collection<MediaType> WRITE_MEDIA_TYPES = Arrays.asList(
    MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML,
    APPLICATION_VND_SUN_WADL_XML, TEXT_URI_LIST, MediaType.APPLICATION_JSON,
    MediaType.TEXT_XML);

  private static Map<MediaType, String> MEDIA_TYPE_TO_EXTENSION_MAP = new HashMap<MediaType, String>();
  static {
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.APPLICATION_XHTML_XML, ".html");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.TEXT_HTML, ".html");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(APPLICATION_VND_SUN_WADL_XML, ".wadl");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(TEXT_URI_LIST, ".uri-list");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.APPLICATION_JSON, ".json");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.TEXT_XML, ".xml");
  }

  public PageInfoHttpMessageConverter() {
    super(PageInfo.class, Collections.emptySet(), WRITE_MEDIA_TYPES);
  }

  @Override
  public void write(final PageInfo pageInfo, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException,
    HttpMessageNotWritableException {
    if (pageInfo != null) {
      Charset charset = mediaType.getCharSet();
      if (charset == null) {
        charset = DEFAULT_CHARSET;
      }

      final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();

      String url = pageInfo.getUrl();
      if (url == null) {
        url = HttpRequestUtils.getServerUrl();
        url += urlPathHelper.getOriginatingRequestUri(request);
      }
      if (url.charAt(url.length() - 1) != '/') {
        url += '/';
      }

      final String extension = MEDIA_TYPE_TO_EXTENSION_MAP.get(mediaType);
      if (extension != null) {
        url = url.replaceAll(extension + "/?$", "/");
      }
      final HttpHeaders headers = outputMessage.getHeaders();
      headers.setContentType(mediaType);

      final OutputStream out = outputMessage.getBody();
      if (APPLICATION_VND_SUN_WADL_XML.equals(mediaType)) {
        writeWadl(out, url, pageInfo);
      } else if (MediaType.TEXT_HTML.equals(mediaType)
        || MediaType.APPLICATION_XHTML_XML.equals(mediaType)) {
        writeHtml(out, url, pageInfo);
      } else if (TEXT_URI_LIST.equals(mediaType)) {
        writeUriList(out, url, pageInfo);
        // return getUriListRepresentation();
      } else {
        writeResourceList(mediaType, charset, out, url, pageInfo);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void writeHtml(final OutputStream out, final String url,
    final PageInfo pageInfo) {
    final XmlWriter writer = new XmlWriter(out);
    writer.startTag(HtmlUtil.DIV);
    writer.element(HtmlUtil.H1, pageInfo.getTitle());
    final String description = pageInfo.getDescription();
    if (description != null) {
      writer.element(HtmlUtil.P, description);
    }

    final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
    for (final String method : pageInfo.getMethods()) {
      final Map<String, Object> parameterMap = request.getParameterMap();
      writeMethod(writer, url, pageInfo, method, parameterMap);
    }

    final Map<String, PageInfo> pages = pageInfo.getPages();
    if (!pages.isEmpty()) {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "resources");
      writer.element(HtmlUtil.H2, "Resources");
      writer.startTag(HtmlUtil.DL);
      for (final Entry<String, PageInfo> childPage : pages.entrySet()) {
        final String childPath = childPage.getKey();
        final PageInfo childPageInfo = childPage.getValue();
        String childUri;
        if (childPath.startsWith("/")) {
          childUri = childPath;
        } else {
          childUri = url + childPath;
        }

        // Reference childReference = reference.clone();
        // childReference.setQuery(null);
        // for (String childPathElement : childPath.split("/")) {
        // childReference.addSegment(childPathElement);
        // }
        writer.startTag(HtmlUtil.DT);
        final String childTitle = childPageInfo.getTitle();
        HtmlUtil.serializeA(writer, null, childUri, childTitle);
        writer.endTag(HtmlUtil.DT);
        final boolean isTemplate = childPath.matches(".*(\\{[^\\}]+\\}.*)+");
        final String childDescription = childPageInfo.getDescription();
        if (childDescription != null || isTemplate) {
          writer.startTag(HtmlUtil.DD);
          if (childDescription != null) {
            writer.element(HtmlUtil.P, childDescription);
          }
          if (isTemplate) {
            writer.startTag(HtmlUtil.FORM);
            writer.attribute(HtmlUtil.ATTR_ACTION, childUri);
            writer.attribute(HtmlUtil.ATTR_METHOD, "get");
            for (final String pathElement : childPath.split("/")) {
              if (pathElement.matches("\\{[^\\}]+\\}")) {
                final String name = pathElement.substring(1,
                  pathElement.length() - 1);
                HtmlUtil.serializeTextInput(writer, name, name, 20, 255);
              }
            }
            HtmlUtil.serializeButtonInput(writer, "go", "doGet(this.form)");
            writer.endTag(HtmlUtil.FORM);
          }
          writer.endTag(HtmlUtil.DD);
        }
      }
      writer.endTag(HtmlUtil.DL);
      writer.endTag(HtmlUtil.DIV);
    }
    writer.endTag(HtmlUtil.DIV);
    writer.endDocument();
    writer.close();
  }

  private void writeHtmlField(final XmlWriter writer, final String name,
    final Map<String, ?> formValues) {
    final Object value = formValues.get(name);
    writer.startTag(HtmlUtil.DT);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, name);
    writer.text(CaseConverter.toCapitalizedWords(name));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.DT);

    writer.startTag(HtmlUtil.DD);
    HtmlUtil.serializeTextInput(writer, name, value, 50, 255);
    writer.endTag(HtmlUtil.DD);
  }

  private void writeHtmlFileField(final XmlWriter writer, final String name,
    final Map<String, ?> formValues) {
    final Object value = formValues.get(name);
    writer.startTag(HtmlUtil.DT);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, name);
    writer.text(CaseConverter.toCapitalizedWords(name));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.DT);

    writer.startTag(HtmlUtil.DD);
    HtmlUtil.serializeFileInput(writer, name, value);
    writer.endTag(HtmlUtil.DD);
  }

  private void writeHtmlSelect(final XmlWriter writer, final String name,
    final Map<String, ?> formValues, final boolean optional,
    final List<? extends Object> values) {
    final Object value = formValues.get(name);
    writer.startTag(HtmlUtil.DT);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, name);
    writer.text(CaseConverter.toCapitalizedWords(name));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.DT);

    writer.startTag(HtmlUtil.DD);
    HtmlUtil.serializeSelect(writer, name, value, optional, values);
    writer.endTag(HtmlUtil.DD);
  }

  private void writeMethod(final XmlWriter writer, final String url,
    final PageInfo pageInfo, final String method,
    final Map<String, Object> values) {
    final Collection<ParameterInfo> parameters = pageInfo.getParameters();
    final boolean hasParameters = !parameters.isEmpty();
    if (hasParameters) {
      final String title = method;
      writer.element(HtmlUtil.H2, title);

      if (hasParameters) {
        writer.startTag(HtmlUtil.FORM);
        writer.attribute(HtmlUtil.ATTR_ACTION, url);
        if (method.equals("post")) {
          boolean isFormData = false;
          for (final MediaType mediaType : pageInfo.getInputContentTypes()) {
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
              isFormData = true;
            }
          }
          if (isFormData) {
            writer.attribute(HtmlUtil.ATTR_ENCTYPE,
              MediaType.MULTIPART_FORM_DATA.toString());
          }
        }
        writer.attribute(HtmlUtil.ATTR_METHOD, method);
        writer.startTag(HtmlUtil.DL);
        for (final ParameterInfo parameter : parameters) {
          final String parameterName = parameter.getName();
          final List<Object> options = parameter.getAllowedValues();
          if (options.isEmpty()) {
            if (parameter.getType().equals("xsd:base64Binary")) {
              writeHtmlFileField(writer, parameterName, values);
            } else {
              writeHtmlField(writer, parameterName, values);
            }
          } else {
            writeHtmlSelect(writer, parameterName, values,
              !parameter.isRequired(), options);
          }
        }
      }
      final List<MediaType> mediaTypes = pageInfo.getMediaTypes();
      if (!mediaTypes.isEmpty()) {
        writeHtmlSelect(writer, "format", values, false, mediaTypes);
      }
      writer.endTag(HtmlUtil.DL);
      HtmlUtil.serializeSubmitInput(writer, "Submit", "submit");
      writer.endTag(HtmlUtil.FORM);
    }
  }

  public void writeResourceList(final MediaType mediaType,
    final Charset charset, final OutputStream out, final String url,
    final PageInfo pageInfo) {

    final String mediaTypeString = mediaType.getType() + "/"
      + mediaType.getSubtype();
    final MapWriterFactory writerFactory = ioFactoryRegistry.getFactoryByMediaType(
      MapWriterFactory.class, mediaTypeString);
    if (writerFactory != null) {
      final MapWriter writer = writerFactory.getWriter(new OutputStreamWriter(
        out, charset));
      writer.setProperty(IoConstants.INDENT_PROPERTY, true);
      writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, false);
      final HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
      String callback = request.getParameter("jsonp");
      if (callback == null) {
        callback = request.getParameter("callback");
      }
      if (callback != null) {
        writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
      }

      for (final Entry<String, PageInfo> childPage : pageInfo.getPages()
        .entrySet()) {
        final String childPath = childPage.getKey();
        final PageInfo childPageInfo = childPage.getValue();
        final Map<String, String> childPageMap = new NamedLinkedHashMap<String, String>(
          "resource");
        String childUri;
        if (childPath.startsWith("/")) {
          childUri = childPath;
        } else {
          childUri = url + childPath;
        }
        childPageMap.put("resourceUri", childUri);
        childPageMap.put("title", childPageInfo.getTitle());
        childPageMap.put("description", childPageInfo.getDescription());
        writer.write(childPageMap);
      }
      writer.close();
    }
  }

  private void writeUriList(final OutputStream out, final String url,
    final PageInfo pageInfo) throws IOException {
    final Writer writer = new OutputStreamWriter(out,
      Charset.forName("US-ASCII"));

    try {
      for (final String childPath : pageInfo.getPages().keySet()) {
        String childUri;
        if (childPath.startsWith("/")) {
          childUri = childPath;
        } else {
          childUri = url + childPath;
        }
        writer.write(url + childUri);
        writer.write("\r\n");
      }
    } finally {
      FileUtil.closeSilent(writer);
    }
  }

  private void writeWadl(final OutputStream out, final String url,
    final PageInfo pageInfo) {
    final XmlWriter writer = new XmlWriter(out);
    writer.startDocument();
    writer.startTag(APPLICATION);

    writer.startTag(RESOURCES);
    writeWadlResource(writer, url, pageInfo, true);
    writer.endTag(RESOURCES);

    writer.endTag(APPLICATION);
    writer.endDocument();
    writer.close();
  }

  private void writeWadlDoc(final XmlWriter writer, final PageInfo pageInfo) {
    for (final DocInfo documentation : pageInfo.getDocumentation()) {
      final String title = documentation.getTitle();
      final String description = documentation.getDescription();
      if (title != null && description != null) {
        writer.startTag(DOC);
        final Locale locale = documentation.getLocale();
        if (locale != null) {
          writer.attribute(XmlContants.XML_LANG, locale);
        }
        writer.attribute(TITLE, title);
        writer.text(description);
        writer.endTag(DOC);
      }
    }
  }

  private void writeWadlResource(final XmlWriter writer, final String url,
    final PageInfo pageInfo, final boolean writeChildren) {
    writer.startTag(RESOURCE);
    writer.attribute(PATH, url);
    writeWadlDoc(writer, pageInfo);
    if (writeChildren) {
      for (final Entry<String, PageInfo> childPage : pageInfo.getPages()
        .entrySet()) {
        final String childPath = childPage.getKey();
        final PageInfo childPageInfo = childPage.getValue();
        writeWadlResource(writer, childPath, childPageInfo, false);
      }
    }
    writer.endTag(RESOURCE);
  }
}
