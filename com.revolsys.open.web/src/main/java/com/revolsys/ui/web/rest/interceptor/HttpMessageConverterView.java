package com.revolsys.ui.web.rest.interceptor;

import java.io.IOException;
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
    final HttpMessageConverterView view = (HttpMessageConverterView)requestAttributes.getAttribute(
      NAME, RequestAttributes.SCOPE_REQUEST);
    return view;
  }

  private final HttpMessageConverter<Object> messageConverter;

  private final MediaType mediaType;

  private final Object returnValue;

  public HttpMessageConverterView(final HttpMessageConverter messageConverter,
    final MediaType mediaType, final Object returnValue) {
    this.messageConverter = messageConverter;
    this.mediaType = mediaType;
    this.returnValue = returnValue;
  }

  public MediaType getMediaType() {
    return mediaType;
  }

  public HttpMessageConverter<Object> getMessageConverter() {
    return messageConverter;
  }

  public Object getReturnValue() {
    return returnValue;
  }

  @Override
  protected void renderMergedOutputModel(final Map<String, Object> model,
    final HttpServletRequest request, final HttpServletResponse response)
    throws Exception {
    final RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

    String path = (String)requestAttributes.getAttribute(
      "httpMessageConverterTemplatePath", RequestAttributes.SCOPE_REQUEST);
    if (path == null
      || !Arrays.asList(MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML)
        .contains(mediaType)) {
      render(response);
    } else {
      response.setContentType(mediaType.toString());
      final HttpMessageConverterView savedView = getMessageConverterView();
      requestAttributes.setAttribute(NAME, this,
        RequestAttributes.SCOPE_REQUEST);
      if (!PathViewController.include(request, response, path)) {
        render(response);
      }
      requestAttributes.setAttribute(NAME, savedView,
        RequestAttributes.SCOPE_REQUEST);
    }
  }

  public void render(final HttpServletResponse response) throws IOException {
    messageConverter.write(returnValue, mediaType,
      new ServletServerHttpResponse(response));
  }
}
