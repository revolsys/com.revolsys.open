package com.revolsys.maven;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  public List<MavenRepository> getRepositories() {
    return repositories;
  }

  @Override
  protected void handleMissingResource(
    final Resource resource,
    final String groupId,
    final String artifactId,
    final String type,
    final String classifier,
    final String version) {
    for (final MavenRepository repository : repositories) {
      final Resource repositoryResource = repository.getResource(groupId,
        artifactId, type, classifier, version);
      if (repositoryResource.exists()) {
        try {
          SpringUtil.copy(repositoryResource, resource);
          return;
        } catch (final Exception e) {
          LOG.warn("Unable to download " + repositoryResource, e);
        }
      }
    }
  }

  public void setRepositories(final List<MavenRepository> repositories) {
    this.repositories = repositories;
  }

  public void setRepositoryLocations(final List<Resource> repositoryLocations) {
    for (final Resource resource : repositoryLocations) {
      repositories.add(new MavenRepository(resource));
    }
  }
}
