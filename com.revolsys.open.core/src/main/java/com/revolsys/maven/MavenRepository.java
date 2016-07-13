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
import java.util.WeakHashMap;

import com.revolsys.collection.map.MapEx;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.xml.XmlMapIoFactory;
import com.revolsys.spring.resource.DefaultResourceLoader;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class MavenRepository implements URLStreamHandlerFactory {
  public static String getMavenId(final String groupId, final String artifactId, final String type,
    final String classifier, final String version, final String scope) {
    return Strings.toString(":", groupId, artifactId, type, classifier, version, scope);
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

  private Resource root;

  private final URLStreamHandler urlHandler = new MavenUrlStreamHandler(this);

  private final Map<String, MavenPom> pomCache = new WeakHashMap<>();

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

  @Override
  public URLStreamHandler createURLStreamHandler(final String protocol) {
    return this.urlHandler;
  }

  public MapEx getMavenMetadata(final String groupId, final String artifactId,
    final String version) {
    final String mavenMetadataPath = "/"
      + Strings.toString("/", groupId.replace('.', '/'), artifactId, version, "maven-metadata.xml");
    final Resource resource = this.root;
    final Resource mavenMetadataResource = resource.newChildResource(mavenMetadataPath);
    if (mavenMetadataResource.exists()) {
      try {
        return XmlMapIoFactory.toMap(mavenMetadataResource);
      } catch (final RuntimeException e) {
        Logs.error(this, "Error loading maven resource" + mavenMetadataResource, e);
        if (mavenMetadataResource instanceof FileSystemResource) {
          try {
            final File file = mavenMetadataResource.getFile();
            if (file.delete()) {
              Logs.error(this, "Deleting corrupt maven resource" + mavenMetadataResource, e);
            }
          } catch (final Throwable ioe) {
          }
        }
        throw e;
      }
    } else {
      return MapEx.EMPTY;
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
      final MapEx map = XmlMapIoFactory.toMap(resource);
      final MavenPom mavenPom = new MavenPom(this, map);
      final String groupArtifactVersion = mavenPom.getGroupArtifactVersion();
      this.pomCache.put(groupArtifactVersion, mavenPom);
      return mavenPom;
    } else {
      throw new IllegalArgumentException("Pom does not exist for " + resource);
    }
  }

  public MavenPom getPom(final String groupArtifactVersion) {
    MavenPom pom = this.pomCache.get(groupArtifactVersion);
    if (pom == null) {
      final String[] parts = groupArtifactVersion.split(":");
      if (parts.length < 3) {
        throw new IllegalArgumentException(groupArtifactVersion
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

      pom = getPom(groupId, artifactId, version);
      if (pom == null) {
        Logs.error(this, "Maven pom not found for parent " + groupArtifactVersion);
      }
    }
    return pom;
  }

  public MavenPom getPom(final String groupId, final String artifactId, final String version) {
    final String groupArtifactVersion = groupId + ":" + artifactId + ":" + version;
    MavenPom pom = this.pomCache.get(groupArtifactVersion);
    if (pom == null) {
      final Resource resource = getResource(groupId, artifactId, "pom", version);
      if (resource.exists()) {
        final MapEx map = XmlMapIoFactory.toMap(resource);
        pom = new MavenPom(this, map);
        this.pomCache.put(groupArtifactVersion, pom);
      } else {
        return null;
      }
    }
    return pom;
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
    final Resource resource = this.root;
    final Resource artifactResource = resource.newChildResource(path);
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
            Logs.error(this, "Error downloading: " + digestResource, e);
          } else {
            Logs.error(this, "Error in SHA-1 checksum " + digestContents + " for " + digestResource,
              e);
          }
        }
      }
    }
    return null;
  }

  public String getSnapshotVersion(final MapEx mavenMetadata) {
    final MapEx versioning = mavenMetadata.getValue("versioning");
    if (versioning != null) {
      final MapEx snapshot = versioning.getValue("snapshot");
      if (snapshot != null) {
        final String timestamp = snapshot.getString("timestamp");
        if (Property.hasValue(timestamp)) {
          final String buildNumber = snapshot.getString("buildNumber");
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
      final MapEx mavenMetadata = getMavenMetadata(groupId, artifactId, version);
      final String snapshotVersion = getSnapshotVersion(mavenMetadata);
      if (snapshotVersion != null) {
        final String timestampVersion = version.replaceAll("SNAPSHOT$", snapshotVersion);
        return getResource(groupId, artifactId, type, classifier, timestampVersion, algorithm);
      }
    }
    return resource;
  }

  public URLClassLoader newClassLoader(final String id) {
    final Set<String> exclusionIds = Collections.emptySet();
    return newClassLoader(id, exclusionIds);
  }

  public URLClassLoader newClassLoader(final String id, final Collection<String> exclusionIds) {
    final MavenPom pom = getPom(id);
    final Set<String> dependencies = pom.getDependencyIds(exclusionIds);
    final URL[] urls = new URL[dependencies.size() + 1];
    urls[0] = getURL(pom.getMavenId());
    int i = 1;
    for (final String dependencyId : dependencies) {
      urls[i++] = getURL(dependencyId);
    }
    final ClassLoader parentClassLoader = getClass().getClassLoader();
    return new URLClassLoader(urls, parentClassLoader, this);
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
