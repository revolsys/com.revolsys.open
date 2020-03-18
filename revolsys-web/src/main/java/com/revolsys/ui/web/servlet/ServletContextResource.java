package com.revolsys.ui.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletContext;

import org.jeometry.common.exception.Exceptions;
import org.springframework.core.io.ContextResource;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import com.revolsys.spring.resource.AbstractResource;
import com.revolsys.spring.resource.Resource;

public class ServletContextResource extends AbstractResource implements ContextResource {

  private final String path;

  private final ServletContext servletContext;

  public ServletContextResource(final ServletContext servletContext, final String path) {
    // check ServletContext
    Assert.notNull(servletContext, "Cannot resolve ServletContextResource without ServletContext");
    this.servletContext = servletContext;

    // check path
    Assert.notNull(path, "Path is required");
    String pathToUse = StringUtils.cleanPath(path);
    if (!pathToUse.startsWith("/")) {
      pathToUse = "/" + pathToUse;
    }
    this.path = pathToUse;
  }

  @Override
  public Resource createRelative(final String relativePath) {
    final String pathToUse = StringUtils.applyRelativePath(this.path, relativePath);
    return new ServletContextResource(this.servletContext, pathToUse);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ServletContextResource) {
      final ServletContextResource otherRes = (ServletContextResource)obj;
      return this.servletContext.equals(otherRes.servletContext) && this.path.equals(otherRes.path);
    }
    return false;
  }

  @Override
  public boolean exists() {
    try {
      final URL url = this.servletContext.getResource(this.path);
      return url != null;
    } catch (final MalformedURLException ex) {
      return false;
    }
  }

  public final String getContextPath() {
    return this.path;
  }

  @Override
  public String getDescription() {
    return this.path;
  }

  @Override
  public File getFile() {
    try {
      final URL url = this.servletContext.getResource(this.path);
      if (url != null && ResourceUtils.isFileURL(url)) {
        // Proceed with file system resolution...
        return super.getFile();
      } else {
        final String realPath = WebUtils.getRealPath(this.servletContext, this.path);
        return new File(realPath);
      }
    } catch (final IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public String getFilename() {
    return StringUtils.getFilename(this.path);
  }

  @Override
  public InputStream getInputStream() {
    final InputStream is = this.servletContext.getResourceAsStream(this.path);
    if (is == null) {
      throw new IllegalArgumentException("Could not open " + getDescription());
    }
    return is;
  }

  @Override
  public String getPathWithinContext() {
    return this.path;
  }

  public final ServletContext getServletContext() {
    return this.servletContext;
  }

  @Override
  public URL getURL() {
    try {
      final URL url = this.servletContext.getResource(this.path);
      if (url == null) {
        throw new IllegalArgumentException(
          getDescription() + " cannot be resolved to URL because it does not exist");
      }
      return url;
    } catch (final MalformedURLException e) {
      throw Exceptions.wrap(e);
    }
  }

  @Override
  public int hashCode() {
    return this.path.hashCode();
  }

  @Override
  public boolean isReadable() {
    final InputStream is = this.servletContext.getResourceAsStream(this.path);
    if (is != null) {
      try {
        is.close();
      } catch (final IOException ex) {
        // ignore
      }
      return true;
    } else {
      return false;
    }
  }

}
