package com.revolsys.ui.web.rest.interceptor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ValueConstants;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.WebUtils;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.Reader;
import com.revolsys.io.Writer;
import com.revolsys.ui.web.annotation.RequestAttribute;
import com.revolsys.ui.web.utils.HttpServletUtils;
import com.revolsys.util.Exceptions;
import com.revolsys.util.Property;
import com.revolsys.util.WrappedException;
import com.revolsys.util.function.Function2;
import com.revolsys.util.function.Function3;

public class WebMethodHandler {
  private static final Map<Class<?>, WebParameterHandler> CLASS_HANDLERS = new HashMap<>();

  private static final Map<Class<?>, Function3<WebAnnotationMethodHandlerAdapter, Parameter, Annotation, WebParameterHandler>> ANNOTATION_HANDLERS = new HashMap<>();

  static {
    // TODO Map
    final WebParameterHandler requestHandler = (request, response) -> {
      return request;
    };
    CLASS_HANDLERS.put(ServletRequest.class, requestHandler);
    CLASS_HANDLERS.put(HttpServletRequest.class, requestHandler);
    final WebParameterHandler responseHandler = (request, response) -> {
      return response;
    };
    CLASS_HANDLERS.put(ServletResponse.class, responseHandler);
    CLASS_HANDLERS.put(HttpServletResponse.class, responseHandler);
    CLASS_HANDLERS.put(HttpSession.class, (request, response) -> {
      return request.getSession();
    });
    CLASS_HANDLERS.put(Principal.class, (request, response) -> {
      return request.getUserPrincipal();
    });
    CLASS_HANDLERS.put(Locale.class, (request, response) -> {
      return RequestContextUtils.getLocale(request);
    });
    CLASS_HANDLERS.put(InputStream.class, (request, response) -> {
      try {
        return request.getInputStream();
      } catch (final Exception e) {
        return Exceptions.throwUncheckedException(e);
      }
    });
    CLASS_HANDLERS.put(OutputStream.class, (request, response) -> {
      try {
        return response.getOutputStream();
      } catch (final Exception e) {
        return Exceptions.throwUncheckedException(e);
      }
    });
    CLASS_HANDLERS.put(Reader.class, (request, response) -> {
      try {
        return request.getReader();
      } catch (final Exception e) {
        return Exceptions.throwUncheckedException(e);
      }
    });
    CLASS_HANDLERS.put(Writer.class, (request, response) -> {
      try {
        return response.getWriter();
      } catch (final Exception e) {
        return Exceptions.throwUncheckedException(e);
      }
    });

    ANNOTATION_HANDLERS.put(RequestBody.class, WebMethodHandler::body);
    ANNOTATION_HANDLERS.put(CookieValue.class, WebMethodHandler::cookie);
    ANNOTATION_HANDLERS.put(RequestHeader.class, WebMethodHandler::requestHeader);
    ANNOTATION_HANDLERS.put(RequestParam.class, WebMethodHandler::requestParameter);
    ANNOTATION_HANDLERS.put(RequestAttribute.class, WebMethodHandler::requestAttribute);
    ANNOTATION_HANDLERS.put(PathVariable.class, WebMethodHandler::pathVariable);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  public static WebParameterHandler body(final WebAnnotationMethodHandlerAdapter adapter,
    final Parameter parameter, final Annotation annotation) {
    final boolean required = ((RequestBody)annotation).required();
    final String parameterName = parameter.getName();
    final Class parameterType = parameter.getType();
    final Function<Object, Object> converter = (value) -> {
      return DataTypes.toObject(parameterType, value);
    };
    return WebParameterHandler.function( //
      parameterName, //
      (request, response) -> {
        try {
          final HttpInputMessage inputMessage = new ServletServerHttpRequest(request);
          MediaType contentType = MediaTypeUtil.getContentType(request);
          if (contentType == null) {
            contentType = MediaType.APPLICATION_FORM_URLENCODED;
          }
          if (!MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)
            && !MediaType.MULTIPART_FORM_DATA.includes(contentType)) {
            contentType = MediaTypeUtil.getRequestMediaType(request, adapter.mediaTypes,
              adapter.mediaTypeOrder, adapter.urlPathHelper, adapter.parameterName,
              adapter.defaultMediaType, "");
          }

          final HttpHeaders headers = inputMessage.getHeaders();
          if (contentType == null) {
            final StringBuilder builder = new StringBuilder(ClassUtils.getShortName(parameterType));
            final String paramName = parameterName;
            if (paramName != null) {
              builder.append(' ');
              builder.append(paramName);
            }
            throw new HttpMediaTypeNotSupportedException("Cannot extract @RequestBody parameter ("
              + builder.toString() + "): no Content-Type found");
          } else {
            HttpServletUtils.setContentTypeWithCharset(headers, contentType);
          }
          final List<MediaType> allSupportedMediaTypes = new ArrayList<MediaType>();
          if (adapter.messageConverters != null) {
            for (final HttpMessageConverter<?> messageConverter : adapter.messageConverters) {
              allSupportedMediaTypes.addAll(messageConverter.getSupportedMediaTypes());
              if (messageConverter.canRead(parameterType, contentType)) {
                return messageConverter.read(parameterType, inputMessage);
              }
            }
            String body = null;
            if (MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)) {
              Charset charset = contentType.getCharSet();
              if (charset == null) {
                charset = StandardCharsets.UTF_8;
              }
              final String urlBody = FileCopyUtils
                .copyToString(new InputStreamReader(inputMessage.getBody(), charset));

              final String[] pairs = StringUtils.tokenizeToStringArray(urlBody, "&");

              final MultiValueMap<String, String> values = new LinkedMultiValueMap<String, String>(
                pairs.length);

              for (final String pair : pairs) {
                final int idx = pair.indexOf('=');
                if (idx == -1) {
                  values.add(URLDecoder.decode(pair, charset.name()), null);
                } else {
                  final String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                  final String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                  values.add(name, value);
                }
              }
              body = values.getFirst("body");
            } else if (request instanceof MultipartHttpServletRequest) {
              final MultipartHttpServletRequest multiPartRequest = (MultipartHttpServletRequest)request;
              final MultipartFile bodyFile = multiPartRequest.getFile("body");
              contentType = MediaTypeUtil.getRequestMediaType(request, adapter.mediaTypes,
                adapter.mediaTypeOrder, adapter.urlPathHelper, adapter.parameterName,
                adapter.defaultMediaType, bodyFile.getOriginalFilename());
              HttpServletUtils.setContentTypeWithCharset(headers, contentType);

              final HttpInputMessage newInputMessage = new HttpInputMessage() {
                @Override
                public InputStream getBody() throws IOException {
                  return bodyFile.getInputStream();
                }

                @Override
                public HttpHeaders getHeaders() {
                  return headers;
                }
              };
              for (final HttpMessageConverter<?> messageConverter : adapter.messageConverters) {
                if (messageConverter.canRead(parameterType, contentType)) {
                  return messageConverter.read(parameterType, newInputMessage);
                }
              }

            }
            if (body == null) {
              body = request.getParameter("body");
            }

            if (body != null) {
              contentType = MediaTypeUtil.getRequestMediaType(request, adapter.mediaTypes,
                adapter.mediaTypeOrder, adapter.urlPathHelper, adapter.parameterName,
                adapter.defaultMediaType, "");
              HttpServletUtils.setContentTypeWithCharset(headers, contentType);
              byte[] bytes;
              bytes = body.getBytes();
              final InputStream bodyIn = new ByteArrayInputStream(bytes);
              final HttpInputMessage newInputMessage = new HttpInputMessage() {

                @Override
                public InputStream getBody() throws IOException {
                  return bodyIn;
                }

                @Override
                public HttpHeaders getHeaders() {
                  return headers;
                }
              };
              for (final HttpMessageConverter<?> messageConverter : adapter.messageConverters) {
                if (messageConverter.canRead(parameterType, contentType)) {
                  return messageConverter.read(parameterType, newInputMessage);
                }
              }
            }
          }
          throw new HttpMediaTypeNotSupportedException(contentType, allSupportedMediaTypes);
        } catch (final Exception e) {
          return Exceptions.throwUncheckedException(e);
        }
      }, //
      converter, //
      required, //
      null//
    );
  }

  public static WebParameterHandler cookie(final WebAnnotationMethodHandlerAdapter adapter,
    final Parameter parameter, final Annotation annotation) {
    final Class<?> parameterType = parameter.getType();
    final Function<Object, Object> converter = (value) -> {
      return DataTypes.toObject(parameterType, value);
    };
    final CookieValue cookieValue = (CookieValue)annotation;
    final String name = getName(parameter, cookieValue.value());
    final boolean required = cookieValue.required();
    final Object defaultValue = parseDefaultValueAttribute(converter, cookieValue.defaultValue());

    Function2<HttpServletRequest, HttpServletResponse, Object> function;
    if (Cookie.class.equals(parameterType)) {
      function = (request, response) -> {
        final Cookie cookie = WebUtils.getCookie(request, name);
        return cookie;
      };
    } else {
      function = (request, response) -> {
        final Cookie cookie = WebUtils.getCookie(request, name);
        if (cookie == null) {
          return null;
        } else {
          return cookie.getValue();
        }
      };
    }
    return WebParameterHandler.function( //
      name, //
      function, //
      converter, //
      required, //
      defaultValue //
    );
  }

  private static String getName(final Parameter parameter, final String name) {
    if (Property.hasValue(name)) {
      return name;
    } else {
      return parameter.getName();
    }
  }

  public static Object parseDefaultValueAttribute(final Function<Object, Object> converter,
    final String value) {
    if (ValueConstants.DEFAULT_NONE.equals(value)) {
      return null;
    } else {
      return converter.apply(value);
    }
  }

  @SuppressWarnings("unchecked")
  public static WebParameterHandler pathVariable(final WebAnnotationMethodHandlerAdapter adapter,
    final Parameter parameter, final Annotation annotation) {
    final Class<?> parameterType = parameter.getType();
    final Function<Object, Object> converter = (value) -> {
      return DataTypes.toObject(parameterType, value);
    };

    final PathVariable pathVariable = (PathVariable)annotation;
    final String name = pathVariable.value();

    return WebParameterHandler.function( //
      name, //
      (request, response) -> {
        final Map<String, String> uriTemplateVariables = (Map<String, String>)request
          .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (uriTemplateVariables == null) {
          return null;
        } else {
          return uriTemplateVariables.get(name);
        }
      }, //
      converter, //
      true, //
      null //
    );
  }

  public static WebParameterHandler requestAttribute(
    final WebAnnotationMethodHandlerAdapter adapter, final Parameter parameter,
    final Annotation annotation) {
    final Class<?> parameterType = parameter.getType();
    final Function<Object, Object> converter = (value) -> {
      return DataTypes.toObject(parameterType, value);
    };
    final RequestAttribute requestAttribute = (RequestAttribute)annotation;
    final String name = getName(parameter, requestAttribute.value());
    final boolean required = requestAttribute.required();
    final Object defaultValue = parseDefaultValueAttribute(converter,
      requestAttribute.defaultValue());

    return WebParameterHandler.function( //
      name, //
      (request, response) -> {
        return request.getAttribute(name);
      }, //
      converter, //
      required, //
      defaultValue //
    );
  }

  public static WebParameterHandler requestHeader(final WebAnnotationMethodHandlerAdapter adapter,
    final Parameter parameter, final Annotation annotation) {
    final Class<?> parameterType = parameter.getType();
    final Function<Object, Object> converter = (value) -> {
      return DataTypes.toObject(parameterType, value);
    };
    final RequestHeader requestHeader = (RequestHeader)annotation;
    final String name = requestHeader.value();
    final boolean required = requestHeader.required();
    final Object defaultValue = parseDefaultValueAttribute(converter, requestHeader.defaultValue());
    return WebParameterHandler.function( //
      name, //
      (request, response) -> {
        return request.getHeader(name);
      }, //
      converter, //
      required, //
      defaultValue //
    );
  }

  public static WebParameterHandler requestParameter(
    final WebAnnotationMethodHandlerAdapter adapter, final Parameter parameter,
    final Annotation annotation) {
    final Class<?> parameterType = parameter.getType();
    final Function<Object, Object> converter = (value) -> {
      return DataTypes.toObject(parameterType, value);
    };
    // TODO lists and arrays
    final RequestParam requestParam = (RequestParam)annotation;
    final String name = getName(parameter, requestParam.value());
    final boolean required = requestParam.required();
    final Object defaultValue = parseDefaultValueAttribute(converter, requestParam.defaultValue());

    return WebParameterHandler.function( //
      name, //
      (request, response) -> {
        return request.getParameter(name);
      }, //
      converter, //
      required, //
      defaultValue //
    );
  }

  private final Method method;

  private final int parameterCount;

  private final WebParameterHandler[] parameterHandlers;

  private final WebAnnotationMethodHandlerAdapter adapter;

  public WebMethodHandler(final WebAnnotationMethodHandlerAdapter adapter, final Method method) {
    this.adapter = adapter;
    this.method = method;
    final Parameter[] parameters = method.getParameters();
    this.parameterCount = parameters.length;
    this.parameterHandlers = new WebParameterHandler[this.parameterCount];
    for (int i = 0; i < this.parameterCount; i++) {
      final Parameter parameter = parameters[i];
      this.parameterHandlers[i] = newParameterHandler(parameter);
    }
  }

  public Method getMethod() {
    return this.method;
  }

  public Object invokeMethod(final Object handler, final HttpServletRequest request,
    final HttpServletResponse response) throws Exception {
    final Object[] parameters = new Object[this.parameterCount];
    for (int i = 0; i < this.parameterHandlers.length; i++) {
      try {
        final WebParameterHandler webParameterHandler = this.parameterHandlers[i];
        final Object parameterValue = webParameterHandler.getParameter(request, response);
        parameters[i] = parameterValue;
      } catch (final WrappedException e) {
        throw (Exception)e.getCause();
      }
    }
    try {
      return this.method.invoke(handler, parameters);
    } catch (final Throwable e) {
      return Exceptions.throwUncheckedException(e);
    }
  }

  protected WebParameterHandler newParameterHandler(final Parameter parameter) {
    final Annotation[] annotations = parameter.getAnnotations();
    final Class<?> parameterType = parameter.getType();
    WebParameterHandler parameterHandler = CLASS_HANDLERS.get(parameterType);
    for (final Annotation annotation : annotations) {
      final Class<? extends Annotation> annotationClass = annotation.annotationType();
      final Function3<WebAnnotationMethodHandlerAdapter, Parameter, Annotation, WebParameterHandler> factory = ANNOTATION_HANDLERS
        .get(annotationClass);
      if (factory != null) {
        final WebParameterHandler currentParameterHandler = factory.apply(this.adapter, parameter,
          annotation);
        if (currentParameterHandler != null) {
          if (parameterHandler == null) {
            parameterHandler = currentParameterHandler;
          } else {
            throw new IllegalArgumentException(
              "Multiple matches for parameter: " + parameter + " " + annotation);
          }
        }
      }
    }

    if (parameterHandler == null) {
      parameterHandler = CLASS_HANDLERS.get(parameterType);
      if (parameterHandler == null) {
        LoggerFactory.getLogger(getClass()).warn("No handler for: " + parameter);
        return WebParameterHandler.fixed(null);
      }
    }
    return parameterHandler;
  }
}
