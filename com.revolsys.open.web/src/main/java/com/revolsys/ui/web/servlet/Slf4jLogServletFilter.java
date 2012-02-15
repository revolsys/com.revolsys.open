package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogServletFilter implements Filter {

  private Logger log;

  public void destroy() {
    log = null;
  }

  public void doFilter(
    final ServletRequest request,
    final ServletResponse response,
    final FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    } catch (final IOException e) {
      throw e;
    } catch (final ServletException e) {
      throw e;
    } catch (final RuntimeException e) {
      HttpServletLogUtil.logRequestException(log, null, e);
      throw e;
    } catch (final Error e) {
      HttpServletLogUtil.logRequestException(log, null, e);
      throw e;
    }
  }

  public void init(final FilterConfig filterConfig) throws ServletException {
    log = LoggerFactory.getLogger(getClass());
  }

}
