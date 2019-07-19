package com.revolsys.ui.web.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jeometry.common.logging.Logs;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.WebUtils;

import com.revolsys.ui.web.utils.HttpServletUtils;

public class DispatcherServlet extends org.springframework.web.servlet.DispatcherServlet {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  @Override
  public void destroy() {
    super.destroy();
    final WebApplicationContext webApplicationContext = getWebApplicationContext();
    if (webApplicationContext instanceof AbstractApplicationContext) {
      final AbstractApplicationContext cwac = (AbstractApplicationContext)webApplicationContext;
      cwac.getApplicationListeners().clear();
      if (cwac.isActive()) {
        final ApplicationEventMulticaster eventMultiCaster = cwac.getBean(
          AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME,
          ApplicationEventMulticaster.class);
        eventMultiCaster.removeAllListeners();
      }
    }
  }

  @Override
  protected void doService(final HttpServletRequest request, final HttpServletResponse response)
    throws Exception {
    final HttpServletRequest savedRequest = HttpServletUtils.getRequest();
    final HttpServletResponse savedResponse = HttpServletUtils.getResponse();
    try {
      HttpServletUtils.setRequestAndResponse(request, response);
      super.doService(request, response);
      request.removeAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_PATH_INFO_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_QUERY_STRING_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_REQUEST_URI_ATTRIBUTE);
      request.removeAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE);
    } catch (final AccessDeniedException e) {
      throw e;
    } catch (final Exception e) {
      Logs.error(this, e.getMessage(), e);
      throw e;
    } finally {
      if (savedRequest == null) {
        HttpServletUtils.clearRequestAndResponse();
      } else {
        HttpServletUtils.setRequestAndResponse(savedRequest, savedResponse);
      }
    }
  }
}
