package com.revolsys.ui.web.rest.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

  private UrlPathHelper urlPathHelper = new UrlPathHelper();

  private static final Charset DEFAULT_CHARSET = Charset.forName("ISO-8859-1");

  private final IoFactoryRegistry ioFactoryRegistry = IoFactoryRegistry.INSTANCE;

  private static final MediaType TEXT_URI_LIST = MediaType.parseMediaType("text/uri-list");

  private static final Collection<MediaType> WRITE_MEDIA_TYPES = Arrays.asList(
    MediaType.APPLICATION_XHTML_XML, MediaType.TEXT_HTML,
    APPLICATION_VND_SUN_WADL_XML, TEXT_URI_LIST, MediaType.APPLICATION_JSON,
    MediaType.TEXT_XML);

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

      HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
      String path = urlPathHelper.getOriginatingRequestUri(request);
      if (!path.endsWith("/")) {
        path += "/";
      }
      HttpHeaders headers = outputMessage.getHeaders();
      headers.setContentType(mediaType);

      final OutputStream out = outputMessage.getBody();
      if (APPLICATION_VND_SUN_WADL_XML.equals(mediaType)) {
        writeWadl(out, path, pageInfo);
      } else if (MediaType.TEXT_HTML.equals(mediaType)
        || MediaType.APPLICATION_XHTML_XML.equals(mediaType)) {
        writeHtml(out, path, pageInfo);
      } else if (TEXT_URI_LIST.equals(mediaType)) {
        writeUriList(out, path, pageInfo);
        // return getUriListRepresentation();
      } else {
        writeResourceList(mediaType, charset, out, path, pageInfo);
      }
    }
  }

  private void writeWadl(OutputStream out, String path, PageInfo pageInfo) {
    XmlWriter writer = new XmlWriter(out);
    writer.startDocument();
    writer.startTag(APPLICATION);
    

    writer.startTag(RESOURCES);
    writeWadlResource(writer, path, pageInfo, true);
    writer.endTag(RESOURCES);

    writer.endTag(APPLICATION);
    writer.endDocument();
    writer.close();
  }

  private void writeWadlResource(XmlWriter writer, String path,
    PageInfo pageInfo, boolean writeChildren) {
    writer.startTag(RESOURCE);
    writer.attribute(PATH, path);
    writeWadlDoc(writer, pageInfo);
    if (writeChildren) {
      for (Entry<String, PageInfo> childPage : pageInfo.getPages().entrySet()) {
        String childPath = childPage.getKey();
        PageInfo childPageInfo = childPage.getValue();
        writeWadlResource(writer, childPath, childPageInfo, false);
      }
    }
    writer.endTag(RESOURCE);
  }

  private void writeWadlDoc(XmlWriter writer, PageInfo pageInfo) {
    for (DocInfo documentation : pageInfo.getDocumentation()) {
      String title = documentation.getTitle();
      String description = documentation.getDescription();
      if (title != null && description != null) {
        writer.startTag(DOC);
        Locale locale = documentation.getLocale();
        if (locale != null) {
          writer.attribute(XmlContants.XML_LANG, locale);
        }
        writer.attribute(TITLE, title);
        writer.text(description);
        writer.endTag(DOC);
      }
    }
  }

  private void writeHtml(OutputStream out, String path, PageInfo pageInfo) {
    XmlWriter writer = new XmlWriter(out);
    writer.startTag(HtmlUtil.DIV);
    writer.element(HtmlUtil.H2, pageInfo.getTitle());
    String description = pageInfo.getDescription();
    if (description != null) {
      writer.element(HtmlUtil.P, description);
    }

    HttpServletRequest request = HttpRequestUtils.getHttpServletRequest();
    for (String method : pageInfo.getMethods()) {
      writeMethod(writer, path, pageInfo, method, request.getParameterMap());
    }

    Map<String, PageInfo> pages = pageInfo.getPages();
    if (!pages.isEmpty()) {
      writer.startTag(HtmlUtil.DIV);
      writer.attribute(HtmlUtil.ATTR_CLASS, "resources");
      writer.element(HtmlUtil.H3, "Resources");
      writer.startTag(HtmlUtil.DL);
      for (Entry<String, PageInfo> childPage : pages.entrySet()) {
        String childPath = childPage.getKey();
        PageInfo childPageInfo = childPage.getValue();
        String childUri;
        if (childPath.startsWith("/")) {
          childUri = childPath;
        } else {
          childUri = path + childPath;
        }

        // Reference childReference = reference.clone();
        // childReference.setQuery(null);
        // for (String childPathElement : childPath.split("/")) {
        // childReference.addSegment(childPathElement);
        // }
        writer.startTag(HtmlUtil.DT);
        String childTitle = childPageInfo.getTitle();
        HtmlUtil.serializeA(writer, null, childUri, childTitle);
        writer.endTag(HtmlUtil.DT);
        boolean isTemplate = childPath.matches(".*(\\{[^\\}]+\\}.*)+");
        String childDescription = childPageInfo.getDescription();
        if (childDescription != null || isTemplate) {
          writer.startTag(HtmlUtil.DD);
          if (childDescription != null) {
            writer.element(HtmlUtil.P, childDescription);
          }
          if (isTemplate) {
            writer.startTag(HtmlUtil.FORM);
            writer.attribute(HtmlUtil.ATTR_ACTION, childUri);
            writer.attribute(HtmlUtil.ATTR_METHOD, "get");
            for (String pathElement : childPath.split("/")) {
              if (pathElement.matches("\\{[^\\}]+\\}")) {
                String name = pathElement.substring(1, pathElement.length() - 1);
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

  private void writeMethod(final XmlWriter writer, String path,
    PageInfo pageInfo, final String method, Map<String, Object> values) {
    Collection<ParameterInfo> parameters = pageInfo.getParameters();
    boolean hasParameters = !parameters.isEmpty();
    if (hasParameters) {
      String title = method;
      writer.element(HtmlUtil.H2, title);

      if (hasParameters) {
        writer.startTag(HtmlUtil.FORM);
        writer.attribute(HtmlUtil.ATTR_ACTION, path);
        if (method.equals("post")) {
          boolean isFormData = false;
          for (MediaType mediaType : pageInfo.getInputContentTypes()) {
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
        for (ParameterInfo parameter : parameters) {
          String parameterName = parameter.getName();
          List<Object> options = parameter.getAllowedValues();
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
      List<MediaType> mediaTypes = pageInfo.getMediaTypes();
      if (!mediaTypes.isEmpty()) {
        writeHtmlSelect(writer, "format", values, false, mediaTypes);
      }
      writer.endTag(HtmlUtil.DL);
      HtmlUtil.serializeSubmitInput(writer, "Submit", "submit");
      writer.endTag(HtmlUtil.FORM);
    }
  }

  private void writeHtmlFileField(final XmlWriter writer, final String name,
    final Map<String, ?> formValues) {
    Object value = formValues.get(name);
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
    Object value = formValues.get(name);
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

  private void writeHtmlField(final XmlWriter writer, final String name,
    final Map<String, ?> formValues) {
    Object value = formValues.get(name);
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

  public void writeResourceList(final MediaType mediaType, Charset charset,
    final OutputStream out, String path, final PageInfo pageInfo) {

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
        final Map<String, String> childPageMap = new LinkedHashMap<String, String>();
        String childUri;
        if (childPath.startsWith("/")) {
          childUri = childPath;
        } else {
          childUri = path + childPath;
        }
        childPageMap.put("resourceUri", childUri);
        childPageMap.put("title", childPageInfo.getTitle());
        childPageMap.put("description", childPageInfo.getDescription());
        writer.write(childPageMap);
      }
      writer.close();
    }
  }

  private void writeUriList(final OutputStream out, String path,
    final PageInfo pageInfo) throws IOException {
    final Writer writer = new OutputStreamWriter(out,
      Charset.forName("US-ASCII"));

    try {
      for (final String childPath : pageInfo.getPages().keySet()) {
        String childUri;
        if (childPath.startsWith("/")) {
          childUri = childPath;
        } else {
          childUri = path + childPath;
        }
        writer.write(path + childUri);
        writer.write("\r\n");
      }
    } finally {
      FileUtil.closeSilent(writer);
    }
  }
}
