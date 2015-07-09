package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.revolsys.data.types.DataTypes;
import com.revolsys.format.xml.XmlConstants;
import com.revolsys.format.xml.XmlWriter;
import com.revolsys.format.xml.wadl.WadlConstants;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.IoFactoryRegistry;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.io.NamedLinkedHashMap;
import com.revolsys.ui.html.view.Element;
import com.revolsys.ui.model.DocInfo;
import com.revolsys.ui.model.PageInfo;
import com.revolsys.ui.model.ParameterInfo;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class PageInfoHttpMessageConverter extends AbstractHttpMessageConverter<PageInfo>
  implements WadlConstants {
  private static final MediaType APPLICATION_VND_SUN_WADL_XML = MediaType
    .parseMediaType("application/vnd.sun.wadl+xml");

  private static final MediaType TEXT_URI_LIST = MediaType.parseMediaType("text/uri-list");

  private static final Collection<MediaType> WRITE_MEDIA_TYPES = Arrays.asList(
    MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML, APPLICATION_VND_SUN_WADL_XML,
    TEXT_URI_LIST, MediaType.APPLICATION_JSON, MediaType.TEXT_XML);

  private static Map<MediaType, String> MEDIA_TYPE_TO_EXTENSION_MAP = new HashMap<MediaType, String>();

  static {
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.APPLICATION_XHTML_XML, ".html");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.TEXT_HTML, ".html");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(APPLICATION_VND_SUN_WADL_XML, ".wadl");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(TEXT_URI_LIST, ".uri-list");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.APPLICATION_JSON, ".json");
    MEDIA_TYPE_TO_EXTENSION_MAP.put(MediaType.TEXT_XML, ".xml");
  }

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.getInstance();

  public PageInfoHttpMessageConverter() {
    super(PageInfo.class, Collections.emptySet(), WRITE_MEDIA_TYPES);
  }

  private Map<String, Object> getMap(final String url, final PageInfo pageInfo) {
    final Map<String, Object> pageMap = new NamedLinkedHashMap<String, Object>("resource");
    pageMap.put("resourceUri", url);
    pageMap.put("title", pageInfo.getTitle());
    final String description = pageInfo.getDescription();
    if (Property.hasValue(description)) {
      pageMap.put("description", description);
    }
    for (final Entry<String, Object> attribute : pageInfo.getFields().entrySet()) {
      final String key = attribute.getKey();
      final Object value = attribute.getValue();
      pageMap.put(key, value);
    }
    final List<Map<String, Object>> childPages = new ArrayList<Map<String, Object>>();
    for (final Entry<String, PageInfo> childPage : pageInfo.getPages().entrySet()) {
      final String childPath = childPage.getKey();
      final PageInfo childPageInfo = childPage.getValue();
      final String childUri = getUrl(url, childPath);
      final Map<String, Object> childPageMap = getMap(childUri, childPageInfo);
      childPages.add(childPageMap);
    }
    if (!childPages.isEmpty()) {
      pageMap.put("resources", childPages);
    }
    return pageMap;
  }

  private String getUrl(final String parentUrl, final String childUrl) {
    String childUri;
    if (childUrl.startsWith("http")) {
      return childUrl;
    } else if (childUrl.startsWith("/")) {
      childUri = HttpServletUtils.getServerUrl() + childUrl;
    } else if (parentUrl.charAt(parentUrl.length() - 1) != '/') {
      childUri = parentUrl + "/" + childUrl;
    } else {
      childUri = parentUrl + childUrl;
    }
    return childUri;
  }

  @Override
  public void write(final PageInfo pageInfo, final MediaType mediaType,
    final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
    if (!HttpServletUtils.getResponse().isCommitted()) {
      if (pageInfo != null) {
        final Charset charset = HttpServletUtils.setContentTypeWithCharset(outputMessage,
          mediaType);

        final HttpServletRequest request = HttpServletUtils.getRequest();

        String url = pageInfo.getUrl();
        if (url == null) {
          url = HttpServletUtils.getFullRequestUrl();
          final String extension = MEDIA_TYPE_TO_EXTENSION_MAP.get(mediaType);
          if (extension != null) {
            url = url.replaceAll(extension + "/?$", "/");
          }
        } else if (url.startsWith("/")) {
          url = HttpServletUtils.getServerUrl() + url;
        }

        final boolean showTitle = !"false".equalsIgnoreCase(request.getParameter("showTitle"));

        final OutputStream out = outputMessage.getBody();
        if (APPLICATION_VND_SUN_WADL_XML.equals(mediaType)) {
          writeWadl(out, url, pageInfo);
        } else if (MediaType.TEXT_HTML.equals(mediaType)
          || MediaType.APPLICATION_XHTML_XML.equals(mediaType)) {
          writeHtml(out, url, pageInfo, showTitle);
        } else if (TEXT_URI_LIST.equals(mediaType)) {
          writeUriList(out, url, pageInfo);
        } else {
          writeResourceList(mediaType, charset, out, url, pageInfo);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void writeHtml(final OutputStream out, final String url, final PageInfo pageInfo,
    final boolean showTitle) {
    final XmlWriter writer = new XmlWriter(out);
    writer.startTag(HtmlUtil.DIV);
    if (showTitle) {
      writer.element(HtmlUtil.H1, pageInfo.getTitle());
      final DocInfo docInfo = pageInfo.getDefaultDocumentation();
      if (docInfo != null) {
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_STYLE, "margin-bottom: 1em");
        final String description = docInfo.getDescription();
        if (description != null) {
          if (docInfo.isHtml()) {
            writer.write(description);
          } else {
            writer.element(HtmlUtil.P, description);
          }
        }
        writer.endTag(HtmlUtil.DIV);
      }
    }
    final HttpServletRequest request = HttpServletUtils.getRequest();
    for (final String method : pageInfo.getMethods()) {
      @SuppressWarnings("rawtypes")
      final Map parameterMap = request.getParameterMap();
      writeMethod(writer, url, pageInfo, method, parameterMap);
    }

    final Map<String, PageInfo> pages = pageInfo.getPages();
    final Element pagesElement = pageInfo.getPagesElement();
    if (pagesElement != null) {
      pagesElement.serialize(writer);
    } else if (!pages.isEmpty()) {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "resources");
      writer.startTag(HtmlUtil.DL);
      for (final Entry<String, PageInfo> childPage : pages.entrySet()) {
        final String childPath = childPage.getKey();
        final PageInfo childPageInfo = childPage.getValue();
        String childUri;
        if (childPath.startsWith("/") || childPath.startsWith("http")) {
          childUri = childPath;
        } else if (url.charAt(url.length() - 1) != '/') {
          childUri = url + "/" + childPath;
        } else {
          childUri = url + childPath;
        }

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
                final String name = pathElement.substring(1, pathElement.length() - 1);
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

  private void writeHtmlField(final XmlWriter writer, final ParameterInfo parameter,
    final Map<String, ?> formValues) {
    final String parameterName = parameter.getName();
    final Object defaultValue = parameter.getDefaultValue();

    Object value = formValues.get(parameterName);
    if (value == null) {
      value = defaultValue;
    }
    writer.startTag(HtmlUtil.TR);
    writer.startTag(HtmlUtil.TH);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, parameterName);
    writer.text(CaseConverter.toCapitalizedWords(parameterName));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.TH);

    writer.startTag(HtmlUtil.TD);
    final int maxLength = 255;
    HtmlUtil.serializeTextInput(writer, parameterName, value, 50, maxLength);
    writeInstructions(writer, parameter);
    writer.endTag(HtmlUtil.TD);
    writer.endTag(HtmlUtil.TR);
  }

  private void writeHtmlFileField(final XmlWriter writer, final ParameterInfo parameter,
    final Map<String, ?> formValues) {
    final String name = parameter.getName();
    final Object value = formValues.get(name);
    writer.startTag(HtmlUtil.TR);
    writer.startTag(HtmlUtil.TH);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, name);
    writer.text(CaseConverter.toCapitalizedWords(name));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.TH);

    writer.startTag(HtmlUtil.TD);
    HtmlUtil.serializeFileInput(writer, name, value);
    writeInstructions(writer, parameter);
    writer.endTag(HtmlUtil.TD);
    writer.endTag(HtmlUtil.TR);
  }

  private void writeHtmlSelect(final XmlWriter writer, final ParameterInfo parameter,
    final Map<String, ?> formValues, final List<? extends Object> values) {
    final String name = parameter.getName();
    final boolean optional = !parameter.isRequired();
    Object value = formValues.get(name);
    if (value == null) {
      value = parameter.getDefaultValue();
    }
    writer.startTag(HtmlUtil.TR);
    writer.startTag(HtmlUtil.TH);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, name);
    writer.text(CaseConverter.toCapitalizedWords(name));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.TH);

    writer.startTag(HtmlUtil.TD);
    HtmlUtil.serializeSelect(writer, name, value, optional, values);
    writeInstructions(writer, parameter);
    writer.endTag(HtmlUtil.TD);
    writer.endTag(HtmlUtil.TR);
  }

  private void writeHtmlSelect(final XmlWriter writer, final ParameterInfo parameter,
    final Map<String, ?> formValues, final Map<? extends Object, ? extends Object> values) {
    final String name = parameter.getName();
    final boolean optional = !parameter.isRequired();
    Object value = formValues.get(name);
    if (value == null) {
      value = parameter.getDefaultValue();
    }
    writer.startTag(HtmlUtil.TR);
    writer.startTag(HtmlUtil.TH);
    writer.startTag(HtmlUtil.LABEL);

    writer.attribute(HtmlUtil.ATTR_FOR, name);
    writer.text(CaseConverter.toCapitalizedWords(name));
    writer.endTag(HtmlUtil.LABEL);
    writer.endTag(HtmlUtil.TH);

    writer.startTag(HtmlUtil.TD);
    HtmlUtil.serializeSelect(writer, name, value, optional, values);
    writeInstructions(writer, parameter);
    writer.endTag(HtmlUtil.TD);
    writer.endTag(HtmlUtil.TR);
  }

  private void writeInstructions(final XmlWriter writer, final ParameterInfo parameter) {
    final String description = parameter.getDescription();
    if (Property.hasValue(description)) {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "fieldDescription");
      writer.text(description);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  private void writeMethod(final XmlWriter writer, final String url, final PageInfo pageInfo,
    final String method, final Map<String, Object> values) {
    final Collection<ParameterInfo> parameters = pageInfo.getParameters();
    final boolean hasParameters = !parameters.isEmpty();
    if (hasParameters) {
      // final String title = method;
      // writer.element(HtmlUtil.H2, title);

      if (hasParameters) {
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "form");
        writer.startTag(HtmlUtil.FORM);
        writer.attribute(HtmlUtil.ATTR_ACTION, url);
        writer.attribute(HtmlUtil.ATTR_TARGET, "_top");
        if (method.equals("post")) {
          boolean isFormData = false;
          for (final MediaType mediaType : pageInfo.getInputContentTypes()) {
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(mediaType)) {
              isFormData = true;
            }
          }
          if (isFormData) {
            writer.attribute(HtmlUtil.ATTR_ENCTYPE, MediaType.MULTIPART_FORM_DATA.toString());
          }
        }
        writer.attribute(HtmlUtil.ATTR_METHOD, method);
        writer.startTag(HtmlUtil.DIV);
        writer.attribute(HtmlUtil.ATTR_CLASS, "objectView");
        writer.startTag(HtmlUtil.TABLE);
        writer.attribute(HtmlUtil.ATTR_CLASS, "data");
        writer.startTag(HtmlUtil.TBODY);

        for (final ParameterInfo parameter : parameters) {
          final Map<Object, Object> options = parameter.getAllowedValues();
          if (options.isEmpty()) {
            if (parameter.getType().equals(DataTypes.BASE64_BINARY)) {
              writeHtmlFileField(writer, parameter, values);
            } else {
              writeHtmlField(writer, parameter, values);
            }
          } else {
            writeHtmlSelect(writer, parameter, values, options);
          }
        }
      }
      final List<MediaType> mediaTypes = pageInfo.getMediaTypes();
      if (!mediaTypes.isEmpty()) {
        final ParameterInfo parameter = new ParameterInfo("format", true, DataTypes.STRING,
          "Select the file format to return the result data in.", mediaTypes);
        writeHtmlSelect(writer, parameter, values, mediaTypes);
      }
      writer.endTag(HtmlUtil.TBODY);
      writer.endTag(HtmlUtil.TABLE);
      writer.endTag(HtmlUtil.DIV);
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "actionMenu");
      HtmlUtil.serializeSubmitInput(writer, "Submit", "submit");
      writer.endTag(HtmlUtil.DIV);
      writer.endTag(HtmlUtil.FORM);
      writer.endTag(HtmlUtil.DIV);
    }
  }

  public void writeResourceList(final MediaType mediaType, Charset charset, final OutputStream out,
    final String url, final PageInfo pageInfo) {
    if (charset == null) {
      charset = StandardCharsets.UTF_8;
    }
    final String mediaTypeString = mediaType.getType() + "/" + mediaType.getSubtype();
    final MapWriterFactory writerFactory = this.ioFactoryRegistry
      .getFactoryByMediaType(MapWriterFactory.class, mediaTypeString);
    if (writerFactory != null) {
      final MapWriter writer = writerFactory.createMapWriter(out, charset);
      writer.setProperty(IoConstants.INDENT, true);
      writer.setProperty(IoConstants.SINGLE_OBJECT_PROPERTY, true);
      final HttpServletRequest request = HttpServletUtils.getRequest();
      String callback = request.getParameter("jsonp");
      if (callback == null) {
        callback = request.getParameter("callback");
      }
      if (callback != null) {
        writer.setProperty(IoConstants.JSONP_PROPERTY, callback);
      }
      final Map<String, Object> page = getMap(url, pageInfo);
      writer.write(page);
      writer.close();
    }
  }

  private void writeUriList(final OutputStream out, final String url, final PageInfo pageInfo)
    throws IOException {
    final Writer writer = FileUtil.createUtf8Writer(out);

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

  private void writeWadl(final OutputStream out, final String url, final PageInfo pageInfo) {
    final XmlWriter writer = new XmlWriter(out);
    writer.startDocument("UTF-8", "1.0");
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
          writer.attribute(XmlConstants.XML_LANG, locale);
        }
        writer.attribute(TITLE, title);
        writer.text(description);
        writer.endTag(DOC);
      }
    }
  }

  private void writeWadlResource(final XmlWriter writer, final String url, final PageInfo pageInfo,
    final boolean writeChildren) {
    writer.startTag(RESOURCE);
    writer.attribute(PATH, url);
    writeWadlDoc(writer, pageInfo);
    if (writeChildren) {
      for (final Entry<String, PageInfo> childPage : pageInfo.getPages().entrySet()) {
        final String childPath = childPage.getKey();
        final PageInfo childPageInfo = childPage.getValue();
        writeWadlResource(writer, childPath, childPageInfo, false);
      }
    }
    writer.endTag(RESOURCE);
  }
}
