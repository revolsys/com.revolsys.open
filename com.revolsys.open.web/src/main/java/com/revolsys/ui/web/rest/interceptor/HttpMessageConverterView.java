package com.revolsys.ui.web.rest.interceptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.view.AbstractView;

import com.revolsys.ui.web.controller.PathViewController;

public class HttpMessageConverterView extends AbstractView {
  private static final String NAME = HttpMessageConverterView.class.getName();

  public static HttpMessageConverterView getMessageConverterView() {
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    final HttpMessageConverterView view = (HttpMessageConverterView)requestAttributes
      .getAttribute(NAME, RequestAttributes.SCOPE_REQUEST);
    return view;
  }

  private final MediaType mediaType;

  private final HttpMessageConverter<Object> messageConverter;

  private final Object returnValue;

  public HttpMessageConverterView(final HttpMessageConverter messageConverter,
    final MediaType mediaType, final Object returnValue) {
    this.messageConverter = messageConverter;
    this.mediaType = mediaType;
    this.returnValue = returnValue;
  }

  public MediaType getMediaType() {
    return this.mediaType;
  }

  public HttpMessageConverter<Object> getMessageConverter() {
    return this.messageConverter;
  }

  public Object getReturnValue() {
    return this.returnValue;
  }

  public void render(final HttpServletResponse response) throws IOException {
    this.messageConverter.write(this.returnValue, this.mediaType,
      new ServletServerHttpResponse(response));
  }

  @Override
  protected void renderMergedOutputModel(final Map<String, Object> model,
    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

    final String path = (String)requestAttributes.getAttribute("httpMessageConverterTemplatePath",
      RequestAttributes.SCOPE_REQUEST);
    if (path == null || !Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML)
      .contains(this.mediaType)) {
      render(response);
    } else {
      final Charset charSet = this.mediaType.getCharset();
      if (charSet == null) {
        response.setContentType(this.mediaType.toString() + "; charset=UTF-8");
      } else {
        response.setContentType(this.mediaType.toString());
      }
      final HttpMessageConverterView savedView = getMessageConverterView();
      requestAttributes.setAttribute(NAME, this, RequestAttributes.SCOPE_REQUEST);
      if (!PathViewController.include(request, response, path)) {
        render(response);
      }
      requestAttributes.setAttribute(NAME, savedView, RequestAttributes.SCOPE_REQUEST);
    }
  }
}
