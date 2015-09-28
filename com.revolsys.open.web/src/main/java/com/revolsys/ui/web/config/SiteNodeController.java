package com.revolsys.ui.web.config;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The SiteNodeController defines the interface for the types of controllers
 * that can
 * <dl>
 * <dt>{@link PageController}</dt>
 * <dd>A controller that can be used to Construct a new web page using a series of
 * {@link IafAction}s with a {@link Layout} to display some {@link Component}s.</dd>
 *
 * @author paustin
 */
public interface SiteNodeController extends Cloneable {
  Object clone();

  SiteNode getNode();

  String getPath();

  void process(ServletContext servletConfig, HttpServletRequest request,
    HttpServletResponse response) throws IOException, ServletException;

  void setNode(SiteNode node);
}
