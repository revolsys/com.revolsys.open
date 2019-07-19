package com.revolsys.ui.web.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.revolsys.ui.web.utils.HttpServletUtils;

public class SavedRequestFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
    final HttpServletResponse response, final FilterChain filterChain)
    throws ServletException, IOException {
    final HttpServletRequest savedRequest = HttpServletUtils.getRequest();
    final HttpServletResponse savedResponse = HttpServletUtils.getResponse();
    try {
      HttpServletUtils.setRequestAndResponse(request, response);
      filterChain.doFilter(request, response);
    } finally {
      if (savedRequest == null) {
        HttpServletUtils.clearRequestAndResponse();
      } else {
        HttpServletUtils.setRequestAndResponse(savedRequest, savedResponse);
      }
    }
  }
}
