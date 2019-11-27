package com.revolsys.ui.web.security;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

public class HttpSessionSecurityContextRepository implements SecurityContextRepository {
  final class SaveToSessionResponseWrapper extends HttpServletResponseWrapper {

    private final int contextHashBeforeChainExecution;

    private boolean contextSaved = false;

    private final boolean disableUrlRewriting;

    private final boolean httpSessionExistedAtStartOfRequest;

    private final HttpServletRequest request;

    SaveToSessionResponseWrapper(final HttpServletResponse response,
      final HttpServletRequest request, final boolean httpSessionExistedAtStartOfRequest,
      final int contextHashBeforeChainExecution) {
      super(response);
      this.disableUrlRewriting = isDisableUrlRewriting();
      this.request = request;
      this.httpSessionExistedAtStartOfRequest = httpSessionExistedAtStartOfRequest;
      this.contextHashBeforeChainExecution = contextHashBeforeChainExecution;
    }

    private HttpSession createNewSessionIfAllowed(final SecurityContext context) {
      if (this.httpSessionExistedAtStartOfRequest) {

        return null;
      }

      if (!HttpSessionSecurityContextRepository.this.allowSessionCreation) {

        return null;
      }

      if (HttpSessionSecurityContextRepository.this.contextObject.equals(context)) {

        return null;
      }

      try {
        return this.request.getSession(true);
      } catch (final IllegalStateException e) {
        HttpSessionSecurityContextRepository.this.logger
          .warn("Failed to Construct a new session, as response has been committed. Unable to store"
            + " SecurityContext.");
      }

      return null;
    }

    private void doSaveContext() {
      saveContext(SecurityContextHolder.getContext());
      this.contextSaved = true;
    }

    @Override
    public final String encodeRedirectUrl(final String url) {
      if (this.disableUrlRewriting) {
        return url;
      }
      return super.encodeRedirectUrl(url);
    }

    @Override
    public final String encodeRedirectURL(final String url) {
      if (this.disableUrlRewriting) {
        return url;
      }
      return super.encodeRedirectURL(url);
    }

    @Override
    public final String encodeUrl(final String url) {
      if (this.disableUrlRewriting) {
        return url;
      }
      return super.encodeUrl(url);
    }

    @Override
    public final String encodeURL(final String url) {
      if (this.disableUrlRewriting) {
        return url;
      }
      return super.encodeURL(url);
    }

    public final boolean isContextSaved() {
      return this.contextSaved;
    }

    protected void saveContext(final SecurityContext context) {
      final Authentication authentication = context.getAuthentication();
      HttpSession httpSession = this.request.getSession(false);

      if (authentication == null
        || HttpSessionSecurityContextRepository.this.authenticationTrustResolver
          .isAnonymous(authentication)) {

        if (httpSession != null) {
          httpSession
            .removeAttribute(HttpSessionSecurityContextRepository.this.springSecurityContextKey);
        }
        return;
      }

      if (httpSession == null) {
        httpSession = createNewSessionIfAllowed(context);
      }

      if (httpSession != null
        && (context.hashCode() != this.contextHashBeforeChainExecution || httpSession.getAttribute(
          HttpSessionSecurityContextRepository.this.springSecurityContextKey) == null)) {
        httpSession.setAttribute(HttpSessionSecurityContextRepository.this.springSecurityContextKey,
          context);

      }
    }

    @Override
    public final void sendError(final int sc) throws IOException {
      doSaveContext();
      super.sendError(sc);
    }

    @Override
    public final void sendError(final int sc, final String msg) throws IOException {
      doSaveContext();
      super.sendError(sc, msg);
    }

    @Override
    public final void sendRedirect(final String location) throws IOException {
      doSaveContext();
      super.sendRedirect(location);
    }
  }

  private boolean allowSessionCreation = true;

  private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

  private final boolean cloneFromHttpSession = false;

  private final Object contextObject = SecurityContextHolder.createEmptyContext();

  private boolean disableUrlRewriting = false;

  protected final Log logger = LogFactory.getLog(this.getClass());

  private final Class<? extends SecurityContext> securityContextClass = null;

  public String springSecurityContextKey = "SPRING_SECURITY_CONTEXT";

  private Object cloneContext(final Object context) {
    Object clonedContext = null;
    Assert.isInstanceOf(Cloneable.class, context,
      "Context must implement Cloneable and provide a Object.clone() method");
    try {
      final Method m = context.getClass().getMethod("clone", new Class[] {});
      if (!m.isAccessible()) {
        m.setAccessible(true);
      }
      clonedContext = m.invoke(context, new Object[] {});
    } catch (final Exception ex) {
      ReflectionUtils.handleReflectionException(ex);
    }

    return clonedContext;
  }

  @Override
  public boolean containsContext(final HttpServletRequest request) {
    final HttpSession session = request.getSession(false);

    if (session == null) {
      return false;
    }

    return session.getAttribute(this.springSecurityContextKey) != null;
  }

  SecurityContext generateNewContext() {
    SecurityContext context = null;

    if (this.securityContextClass == null) {
      context = SecurityContextHolder.createEmptyContext();

      return context;
    }

    try {
      context = this.securityContextClass.newInstance();
    } catch (final Exception e) {
      ReflectionUtils.handleReflectionException(e);
    }
    return context;
  }

  public String getSpringSecurityContextKey() {
    return this.springSecurityContextKey;
  }

  public boolean isDisableUrlRewriting() {
    return this.disableUrlRewriting;
  }

  @Override
  public SecurityContext loadContext(final HttpRequestResponseHolder requestResponseHolder) {
    final HttpServletRequest request = requestResponseHolder.getRequest();
    final HttpServletResponse response = requestResponseHolder.getResponse();
    final HttpSession httpSession = request.getSession(false);

    SecurityContext context = readSecurityContextFromSession(httpSession);

    if (context == null) {
      context = generateNewContext();

    }

    requestResponseHolder.setResponse(
      new SaveToSessionResponseWrapper(response, request, httpSession != null, context.hashCode()));

    return context;
  }

  private SecurityContext readSecurityContextFromSession(final HttpSession httpSession) {
    final boolean debug = this.logger.isDebugEnabled();

    if (httpSession == null) {
      return null;
    }

    Object contextFromSession = httpSession.getAttribute(this.springSecurityContextKey);

    if (contextFromSession == null) {
      return null;
    }

    if (!(contextFromSession instanceof SecurityContext)) {
      if (this.logger.isWarnEnabled()) {
        this.logger
          .warn("SPRING_SECURITY_CONTEXT did not contain a SecurityContext but contained: '"
            + contextFromSession + "'; are you improperly modifying the HttpSession directly "
            + "(you should always use SecurityContextHolder) or using the HttpSession attribute "
            + "reserved for this class?");
      }

      return null;
    }

    if (this.cloneFromHttpSession) {
      contextFromSession = cloneContext(contextFromSession);
    }

    if (debug) {
      this.logger.debug("Obtained a valid SecurityContext from SPRING_SECURITY_CONTEXT: '"
        + contextFromSession + "'");
    }

    return (SecurityContext)contextFromSession;
  }

  @Override
  public void saveContext(final SecurityContext context, final HttpServletRequest request,
    HttpServletResponse response) {
    while (!(response instanceof SaveToSessionResponseWrapper)) {
      if (response instanceof HttpServletResponseWrapper) {
        final HttpServletResponseWrapper wrappedResponse = (HttpServletResponseWrapper)response;
        response = (HttpServletResponse)wrappedResponse.getResponse();
      } else {
        return;
      }
      if (response == null) {
        return;
      }
    }
    final SaveToSessionResponseWrapper responseWrapper = (SaveToSessionResponseWrapper)response;
    if (!responseWrapper.isContextSaved()) {
      responseWrapper.saveContext(context);
    }
  }

  public void setAllowSessionCreation(final boolean allowSessionCreation) {
    this.allowSessionCreation = allowSessionCreation;
  }

  public void setDisableUrlRewriting(final boolean disableUrlRewriting) {
    this.disableUrlRewriting = disableUrlRewriting;
  }

  public void setSpringSecurityContextKey(final String springSecurityContextKey) {
    this.springSecurityContextKey = springSecurityContextKey;
  }
}
