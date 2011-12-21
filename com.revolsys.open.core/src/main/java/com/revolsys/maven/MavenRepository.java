package com.revolsys.maven;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlMapIoFactory;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.CollectionUtil;

public class MavenRepository implements URLStreamHandlerFactory {

  public static String getMavenId(
    final String groupId,
    final String artifactId,
    final String type,
    final String classifier,
    final String version,
    final String scope) {
    return CollectionUtil.toString(":", groupId, artifactId, type, classifier,
      version, scope);
  }

  public static String getPath(
    final String groupId,
    final String artifactId,
    final String type,
    final String classifier,
    final String version) {
    final StringBuffer path = new StringBuffer();
    path.append('/');
    path.append(groupId.replace('.', '/'));
    path.append('/');
    path.append(artifactId);
    path.append('/');
    path.append(version);
    path.append('/');
    path.append(artifactId);
    path.append('-');
    path.append(version);
    if (StringUtils.hasText(classifier)) {
      path.append('-');
      path.append(classifier);
    }
    path.append('.');
    path.append(type);
    return path.toString();
  }

  private final URLStreamHandler urlHandler = new MavenUrlStreamHandler(this);

  private Resource root;

  public MavenRepository() {
    this(null);
  }

  /**
   * Root resource must end in a /
   * 
   * @param root
   */
  public MavenRepository(final Resource root) {
    if (root == null) {
      this.root = new FileSystemResource(System.getProperty("user.home")
        + "/.m2/repository/");
    } else {
      this.root = root;
    }
  }

  public URLClassLoader createClassLoader(final String id) {
    final Set<String> exclusionIds = Collections.emptySet();
    return createClassLoader(id, exclusionIds);
  }

  public URLClassLoader createClassLoader(
    final String id,
    final Collection<String> exclusionIds) {
    final MavenPom pom = getPom(id);
    final Set<String> dependencies = pom.getDependencies(exclusionIds);
    final URL[] urls = new URL[dependencies.size() + 1];
    urls[0] = getURL(pom.getMavenId());
    int i = 1;
    for (final String dependencyId : dependencies) {
      urls[i++] = getURL(dependencyId);
    }
    final ClassLoader parentClassLoader = getClass().getClassLoader();
    return new URLClassLoader(urls, parentClassLoader, this);
  }

  public URLStreamHandler createURLStreamHandler(final String protocol) {
    return urlHandler;
  }

  public String getPath(final String id) {
    final String[] parts = id.split(":");
    final String groupId = parts[0];
    final String artifactId = parts[1];
    final String type = parts[2];
    String version;
    String classifier = null;
    if (parts.length == 5) {
      version = parts[3];
    } else {
      classifier = parts[3];
      version = parts[4];
    }

    return getPath(groupId, artifactId, type, classifier, version);
  }

  public MavenPom getPom(final Resource resource) {
    if (resource.exists()) {
      final Map<String, Object> map = XmlMapIoFactory.toMap(resource);
      return new MavenPom(this, map);
    } else {
      throw new IllegalArgumentException("Pom does not exist for " + resource);
    }
  }

  public MavenPom getPom(final String id) {
    final String[] parts = id.split(":");
    final String groupId = parts[0];
    final String artifactId = parts[1];
    String version;
    if (parts.length == 5) {
      version = parts[3];
    } else if (parts.length == 6) {
      version = parts[4];
    } else {
      version = parts[2];
    }

    return getPom(groupId, artifactId, version);
  }

  public MavenPom getPom(
    final String groupId,
    final String artifactId,
    final String version) {
    final Resource resource = getResource(groupId, artifactId, "pom", version);
    if (resource.exists()) {
      final Map<String, Object> map = XmlMapIoFactory.toMap(resource);
      return new MavenPom(this, map);
    } else {
      throw new IllegalArgumentException("Pom does not exist for " + groupId
        + ":" + artifactId + ":" + version + " at " + resource);
    }
  }

  public Resource getResource(String id) {
    id = id.replace('/', ':');
    final String[] parts = id.split(":");
    final String groupId = parts[0];
    final String artifactId = parts[1];
    final String type = parts[2];
    if (parts.length < 6) {
      final String version = parts[3];
      return getResource(groupId, artifactId, type, version);
    } else {
      final String classifier = parts[3];
      final String version = parts[4];
      return getResource(groupId, artifactId, type, classifier, version);
    }
  }

  public Resource getResource(
    final String groupId,
    final String artifactId,
    final String type,
    final String version) {
    return getResource(groupId, artifactId, type, null, version);
  }

  public Resource getResource(
    final String groupId,
    final String artifactId,
    final String type,
    final String classifier,
    final String version) {
    final String path = getPath(groupId, artifactId, type, classifier, version);
    final Resource artifactResource = SpringUtil.getResource(root, path);
    if (!artifactResource.exists()) {
      handleMissingResource(artifactResource, groupId, artifactId, type,
        classifier, version);
    }
    return artifactResource;
  }

  public Resource getRoot() {
    return root;
  }

  public URL getURL(final String id) {
    final String path = id.replace(':', '/');
    try {
      return new URL("mvn", "", -1, path, urlHandler);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Not a valid maven identifier " + id,
        e);
    }
  }

  protected void handleMissingResource(
    final Resource resource,
    final String groupId,
    final String artifactId,
    final String type,
    final String classifier,
    final String version) {
  }

  public void setRoot(final Resource root) {
    this.root = root;
  }
}
