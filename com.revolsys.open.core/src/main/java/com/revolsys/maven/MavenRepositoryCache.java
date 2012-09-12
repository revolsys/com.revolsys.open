package com.revolsys.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.spring.SpringUtil;

public class MavenRepositoryCache extends MavenRepository {
  private static final Logger LOG = LoggerFactory.getLogger(MavenRepository.class);

  private List<MavenRepository> repositories = new ArrayList<MavenRepository>();

  public MavenRepositoryCache() {
  }

  public MavenRepositoryCache(final Resource root) {
    super(root);
  }

  public MavenRepositoryCache(final Resource root,
    final String... repositoryUrls) {
    super(root);
    for (String repository : repositoryUrls) {
      if (!repository.endsWith("/")) {
        repository += '/';
      }
      final Resource resource = SpringUtil.getResource(repository);
      this.repositories.add(new MavenRepository(resource));
    }
  }

  public MavenRepositoryCache(final String... repositoryUrls) {
    this(null, repositoryUrls);
  }

  public boolean copyRepositoryResource(final Resource resource,
    final MavenRepository repository, final String path) {
    final Resource repositoryResource = SpringUtil.getResource(
      repository.getRoot(), path);
    if (repositoryResource.exists()) {
      try {
        SpringUtil.copy(repositoryResource, resource);
        return true;
      } catch (final Exception e) {
        LOG.warn("Unable to download " + repositoryResource, e);
      }
    }
    return false;
  }

  public List<MavenRepository> getRepositories() {
    return repositories;
  }

  @Override
  protected Resource handleMissingResource(final Resource resource,
    final String groupId, final String artifactId, final String type,
    final String classifier, final String version) {
    if (version.endsWith("-SNAPSHOT")) {
      final TreeMap<String, MavenRepository> versionsByRepository = new TreeMap<String, MavenRepository>();

      for (final MavenRepository repository : repositories) {
        final Map<String, Object> mavenMetadata = repository.getMavenMetadata(
          groupId, artifactId, version);
        final String snapshotVersion = getSnapshotVersion(mavenMetadata);
        if (snapshotVersion != null) {
          final String timestampVersion = version.replaceAll("SNAPSHOT$",
            snapshotVersion);
          versionsByRepository.put(timestampVersion, repository);
        }
      }
      if (!versionsByRepository.isEmpty()) {
        final Entry<String, MavenRepository> entry = versionsByRepository.lastEntry();
        final String timestampVersion = entry.getKey();

        final String path = getPath(groupId, artifactId, version, type,
          classifier, timestampVersion);
        final Resource cachedResource = SpringUtil.getResource(getRoot(), path);
        if (cachedResource.exists()) {
          return cachedResource;
        } else {

          final MavenRepository repository = entry.getValue();
          if (copyRepositoryResource(cachedResource, repository, path)) {
            return cachedResource;
          }
        }

      }
    }
    final String path = getPath(groupId, artifactId, version, type, classifier,
      version);
    for (final MavenRepository repository : repositories) {
      if (copyRepositoryResource(resource, repository, path)) {
        return resource;
      }
    }
    return resource;
  }

  public void setRepositories(final List<MavenRepository> repositories) {
    this.repositories = repositories;
  }

  public void setRepositoryLocations(final List<Resource> repositoryLocations) {
    for (final Resource resource : repositoryLocations) {
      repositories.add(new MavenRepository(resource));
    }
  }

  @Override
  public void setRoot(final Resource root) {
    if (root != null) {
      try {
        final File file = root.getFile();
        if (!file.exists()) {
          if (!file.mkdirs()) {
            throw new IllegalArgumentException(
              "Cannot create maven cache directory " + file);
          }
        } else if (!file.isDirectory()) {
          throw new IllegalArgumentException(
            "Maven cache is not a directory directory " + file);
        }
        final FileSystemResource fileResource = new FileSystemResource(file);
        super.setRoot(fileResource);
      } catch (final IOException e) {
        throw new IllegalArgumentException(
          "Maven cache must resolve to a local directory " + root);
      }
    }
  }
}
