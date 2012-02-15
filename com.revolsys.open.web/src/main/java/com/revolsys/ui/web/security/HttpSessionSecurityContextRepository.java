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

public class HttpSessionSecurityContextRepository implements
  SecurityContextRepository {
  final class SaveToSessionResponseWrapper extends HttpServletResponseWrapper {

    private final HttpServletRequest request;

    private final boolean httpSessionExistedAtStartOfRequest;

    private final int contextHashBeforeChainExecution;

    private boolean contextSaved = false;

    private final boolean disableUrlRewriting;

    SaveToSessionResponseWrapper(final HttpServletResponse response,
      final HttpServletRequest request,
      final boolean httpSessionExistedAtStartOfRequest,
      final int contextHashBeforeChainExecution) {
      super(response);
      this.disableUrlRewriting = isDisableUrlRewriting();
      this.request = request;
      this.httpSessionExistedAtStartOfRequest = httpSessionExistedAtStartOfRequest;
      this.contextHashBeforeChainExecution = contextHashBeforeChainExecution;
    }

    private HttpSession createNewSessionIfAllowed(final SecurityContext context) {
      if (httpSessionExistedAtStartOfRequest) {

        return null;
      }

      if (!allowSessionCreation) {

        return null;
      }

      if (contextObject.equals(context)) {

        return null;
      }

      try {
        return request.getSession(true);
      } catch (final IllegalStateException e) {
        logger.warn("Failed to create a session, as response has been committed. Unable to store"
          + " SecurityContext.");
      }

      return null;
    }

    private void doSaveContext() {
      saveContext(SecurityContextHolder.getContext());
      contextSaved = true;
    }

    @Override
    public final String encodeRedirectUrl(final String url) {
      if (disableUrlRewriting) {
        return url;
      }
      return super.encodeRedirectUrl(url);
    }

    @Override
    public final String encodeRedirectURL(final String url) {
      if (disableUrlRewriting) {
        return url;
      }
      return super.encodeRedirectURL(url);
    }

    @Override
    public final String encodeUrl(final String url) {
      if (disableUrlRewriting) {
        return url;
      }
      return super.encodeUrl(url);
    }

    @Override
    public final String encodeURL(final String url) {
      if (disableUrlRewriting) {
        return url;
      }
      return super.encodeURL(url);
    }

    public final boolean isContextSaved() {
      return contextSaved;
    }

    protected void saveContext(final SecurityContext context) {
      final Authentication authentication = context.getAuthentication();
      HttpSession httpSession = request.getSession(false);

      if (authentication == null
        || authenticationTrustResolver.isAnonymous(authentication)) {

        if (httpSession != null) {
          httpSession.removeAttribute(springSecurityContextKey);
        }
        return;
      }

      if (httpSession == null) {
        httpSession = createNewSessionIfAllowed(context);
      }

      if (httpSession != null
        && (context.hashCode() != contextHashBeforeChainExecution || httpSession.getAttribute(springSecurityContextKey) == null)) {
        httpSession.setAttribute(springSecurityContextKey, context);

      }
    }

    @Override
    public final void sendError(final int sc) throws IOException {
      doSaveContext();
      super.sendError(sc);
    }

    @Override
    public final void sendError(final int sc, final String msg)
      throws IOException {
      doSaveContext();
      super.sendError(sc, msg);
    }

    @Override
    public final void sendRedirect(final String location) throws IOException {
      doSaveContext();
      super.sendRedirect(location);
    }
  }

  public String springSecurityContextKey = "SPRING_SECURITY_CONTEXT";

  protected final Log logger = LogFactory.getLog(this.getClass());

  private final Class<? extends SecurityContext> securityContextClass = null;

  private final Object contextObject = SecurityContextHolder.createEmptyContext();

  private final boolean cloneFromHttpSession = false;

  private boolean allowSessionCreation = true;

  private boolean disableUrlRewriting = false;

  private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

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

  public boolean containsContext(final HttpServletRequest request) {
    final HttpSession session = request.getSession(false);

    if (session == null) {
      return false;
    }

    return session.getAttribute(springSecurityContextKey) != null;
  }

  SecurityContext generateNewContext() {
    SecurityContext context = null;

    if (securityContextClass == null) {
      context = SecurityContextHolder.createEmptyContext();

      return context;
    }

    try {
      context = securityContextClass.newInstance();
    } catch (final Exception e) {
      ReflectionUtils.handleReflectionException(e);
    }
    return context;
  }

  public String getSpringSecurityContextKey() {
    return springSecurityContextKey;
  }

  public boolean isDisableUrlRewriting() {
    return disableUrlRewriting;
  }

  public SecurityContext loadContext(
    final HttpRequestResponseHolder requestResponseHolder) {
    final HttpServletRequest request = requestResponseHolder.getRequest();
    final HttpServletResponse response = requestResponseHolder.getResponse();
    final HttpSession httpSession = request.getSession(false);

    SecurityContext context = readSecurityContextFromSession(httpSession);

    if (context == null) {
      context = generateNewContext();

    }

    requestResponseHolder.setResponse(new SaveToSessionResponseWrapper(
      response, request, httpSession != null, context.hashCode()));

    return context;
  }

  private SecurityContext readSecurityContextFromSession(
    final HttpSession httpSession) {
    final boolean debug = logger.isDebugEnabled();

    if (httpSession == null) {
      return null;
    }

    Object contextFromSession = httpSession.getAttribute(springSecurityContextKey);

    if (contextFromSession == null) {
      return null;
    }

    if (!(contextFromSession instanceof SecurityContext)) {
      if (logger.isWarnEnabled()) {
        logger.warn("SPRING_SECURITY_CONTEXT did not contain a SecurityContext but contained: '"
          + contextFromSession
          + "'; are you improperly modifying the HttpSession directly "
          + "(you should always use SecurityContextHolder) or using the HttpSession attribute "
          + "reserved for this class?");
      }

      return null;
    }

    if (cloneFromHttpSession) {
      contextFromSession = cloneContext(contextFromSession);
    }

    if (debug) {
      logger.debug("Obtained a valid SecurityContext from SPRING_SECURITY_CONTEXT: '"
        + contextFromSession + "'");
    }

    return (SecurityContext)contextFromSession;
  }

  public void saveContext(
    final SecurityContext context,
    final HttpServletRequest request,
    final HttpServletResponse response) {
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
