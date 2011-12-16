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

  public MavenRepositoryCache(Resource root) {
    super(root);
  }

  public MavenRepositoryCache(Resource root, String... repositoryUrls) {
    super(root);
    for (String repository : repositoryUrls) {
      if (!repository.endsWith("/")) {
        repository += '/';
      }
      Resource resource = SpringUtil.getResource(repository);
      this.repositories.add(new MavenRepository(resource));
    }
  }

  public MavenRepositoryCache(String... repositoryUrls) {
    this(null, repositoryUrls);
  }

  public List<MavenRepository> getRepositories() {
    return repositories;
  }

  @Override
  protected void handleMissingResource(
    Resource resource,
    String groupId,
    String artifactId,
    String type,
    String classifier,
    String version) {
    for (MavenRepository repository : repositories) {
      Resource repositoryResource = repository.getResource(groupId, artifactId,
        type, classifier, version);
      if (repositoryResource.exists()) {
        try {
          SpringUtil.copy(repositoryResource, resource);
          return;
        } catch (Exception e) {
          LOG.warn("Unable to download " + repositoryResource, e);
        }
      }
    }
  }

  public void setRepositories(List<MavenRepository> repositories) {
    this.repositories = repositories;
  }
}
