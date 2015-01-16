package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogServletFilter implements Filter {

  private Logger log;

  @Override
  public void destroy() {
    this.log = null;
  }

  @Override
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
      HttpServletLogUtil.logRequestException(this.log, (HttpServletRequest)request, e);
      throw e;
    } catch (final Error e) {
      HttpServletLogUtil.logRequestException(this.log, (HttpServletRequest)request, e);
      throw e;
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    this.log = LoggerFactory.getLogger(getClass());
  }

}
