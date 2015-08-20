package com.revolsys.maven;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.revolsys.format.xml.XmlMapIoFactory;
import com.revolsys.spring.resource.DefaultResourceLoader;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class MavenRepository implements URLStreamHandlerFactory {

  public static String getMavenId(final String groupId, final String artifactId, final String type,
    final String classifier, final String version, final String scope) {
    return CollectionUtil.toString(":", groupId, artifactId, type, classifier, version, scope);
  }

  public static String getPath(final String groupId, final String artifactId,
    final String pathVersion, final String type, final String classifier, final String version,
    final String algorithm) {
    final StringBuilder path = new StringBuilder();
    path.append('/');
    path.append(groupId.replace('.', '/'));
    path.append('/');
    path.append(artifactId);
    path.append('/');
    path.append(pathVersion);
    path.append('/');
    path.append(artifactId);
    path.append('-');
    path.append(version);
    if (Property.hasValue(classifier)) {
      path.append('-');
      path.append(classifier);
    }
    path.append('.');
    path.append(type);
    if (Property.hasValue(algorithm)) {
      path.append('.');
      path.append(algorithm);

    }
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
    setRoot(root);
  }

  public URLClassLoader createClassLoader(final String id) {
    final Set<String> exclusionIds = Collections.emptySet();
    return createClassLoader(id, exclusionIds);
  }

  public URLClassLoader createClassLoader(final String id, final Collection<String> exclusionIds) {
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

  @Override
  public URLStreamHandler createURLStreamHandler(final String protocol) {
    return this.urlHandler;
  }

  public Map<String, Object> getMavenMetadata(final String groupId, final String artifactId,
    final String version) {
    final String recordDefinitionPath = "/" + CollectionUtil.toString("/",
      groupId.replace('.', '/'), artifactId, version, "maven-metadata.xml");
    final Resource recordDefinitionResource = SpringUtil.getResource(this.root,
      recordDefinitionPath);
    if (recordDefinitionResource.exists()) {
      try {
        return XmlMapIoFactory.toMap(recordDefinitionResource);
      } catch (final RuntimeException e) {
        LoggerFactory.getLogger(getClass())
          .error("Error loading maven resource" + recordDefinitionResource, e);
        if (recordDefinitionResource instanceof FileSystemResource) {
          try {
            final File file = recordDefinitionResource.getFile();
            if (file.delete()) {
              LoggerFactory.getLogger(getClass())
                .error("Deleting corrupt maven resource" + recordDefinitionResource, e);
            }
          } catch (final Throwable ioe) {
          }
        }
        throw e;
      }
    } else {
      return Collections.emptyMap();
    }

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
    return getPath(groupId, artifactId, version, type, classifier, version, null);
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
    if (parts.length < 3) {
      throw new IllegalArgumentException(id
        + " is not a valid Maven identifier. Should be in the format: <groupId>:<artifactId>:<version>.");
    }
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

  public MavenPom getPom(final String groupId, final String artifactId, final String version) {
    final Resource resource = getResource(groupId, artifactId, "pom", version);
    if (resource.exists()) {
      final Map<String, Object> map = XmlMapIoFactory.toMap(resource);
      return new MavenPom(this, map);
    } else {
      throw new IllegalArgumentException(
        "Pom does not exist for " + groupId + ":" + artifactId + ":" + version + " at " + resource);
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

  public Resource getResource(final String groupId, final String artifactId, final String type,
    final String version) {
    return getResource(groupId, artifactId, type, null, version);
  }

  public Resource getResource(final String groupId, final String artifactId, final String type,
    final String classifier, final String version) {
    return getResource(groupId, artifactId, type, classifier, version, null);
  }

  public Resource getResource(final String groupId, final String artifactId, final String type,
    final String classifier, final String version, final String algorithm) {

    final String path = getPath(groupId, artifactId, version, type, classifier, version, algorithm);
    final Resource artifactResource = SpringUtil.getResource(this.root, path);
    if (!artifactResource.exists()) {
      return handleMissingResource(artifactResource, groupId, artifactId, type, classifier, version,
        algorithm);
    }
    return artifactResource;
  }

  public Resource getRoot() {
    return this.root;
  }

  public String getSha1(final String groupId, final String artifactId, final String type,
    final String classifier, final String version, final String algorithm) {
    if (!Property.hasValue(algorithm)) {
      final Resource digestResource = getResource(groupId, artifactId, type, classifier, version,
        "sha1");
      if (digestResource.exists()) {
        String digestContents = null;
        try {
          digestContents = digestResource.contentsAsString();
          return digestContents.trim().substring(0, 40);
        } catch (final Throwable e) {
          if (digestContents == null) {
            LoggerFactory.getLogger(getClass()).error("Error downloading: " + digestResource, e);
          } else {
            LoggerFactory.getLogger(getClass())
              .error("Error in SHA-1 checksum " + digestContents + " for " + digestResource, e);
          }
        }
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public String getSnapshotVersion(final Map<String, Object> mavenMetadata) {
    final Map<String, Object> versioning = (Map<String, Object>)mavenMetadata.get("versioning");
    if (versioning != null) {
      final Map<String, Object> snapshot = (Map<String, Object>)versioning.get("snapshot");
      if (snapshot != null) {
        final String timestamp = (String)snapshot.get("timestamp");
        if (Property.hasValue(timestamp)) {
          final String buildNumber = (String)snapshot.get("buildNumber");
          if (Property.hasValue(timestamp)) {
            return timestamp + "-" + buildNumber;
          } else {
            return timestamp + "-1";
          }
        }
      }
    }
    return null;
  }

  public URL getURL(final String id) {
    final String path = id.replace(':', '/');
    try {
      return new URL("mvn", "", -1, path, this.urlHandler);
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Not a valid maven identifier " + id, e);
    }
  }

  protected Resource handleMissingResource(final Resource resource, final String groupId,
    final String artifactId, final String type, final String classifier, final String version,
    final String algorithm) {
    if (version.endsWith("-SNAPSHOT")) {
      final Map<String, Object> mavenMetadata = getMavenMetadata(groupId, artifactId, version);
      final String snapshotVersion = getSnapshotVersion(mavenMetadata);
      if (snapshotVersion != null) {
        final String timestampVersion = version.replaceAll("SNAPSHOT$", snapshotVersion);
        return getResource(groupId, artifactId, type, classifier, timestampVersion, algorithm);
      }
    }
    return resource;
  }

  public void setRoot(final Resource root) {
    if (root == null) {
      this.root = new FileSystemResource(System.getProperty("user.home") + "/.m2/repository/");
    } else {
      try {
        String url = root.getURL().toExternalForm();
        url = url.replaceAll("([^(:/{2,3)])//+", "$1/");

        if (!url.endsWith("/")) {
          url += '/';
        }
        this.root = new DefaultResourceLoader().getResource(url);
      } catch (final Throwable e) {
        this.root = root;
      }
    }

  }
}
