package com.revolsys.maven;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.Resource;

import com.revolsys.spring.SpringUtil;

public class MavenRepositoryCache extends MavenRepository {
  public MavenRepositoryCache() {
  }

  public MavenRepositoryCache(Resource root) {
    super(root);
  }

  private List<MavenRepository> repositories = new ArrayList<MavenRepository>();

  @Override
  protected void handleMissingResource(Resource resource, String groupId,
    String artifactId, String version, String classifier, String type) {
    for (MavenRepository repository : repositories) {
      Resource repositoryResource = repository.getResource(groupId, artifactId,
        version, classifier, type);
      if (repositoryResource.exists()) {
        SpringUtil.copy(repositoryResource, resource);
      }
    }
  }

  public List<MavenRepository> getRepositories() {
    return repositories;
  }

  public void setRepositories(List<MavenRepository> repositories) {
    this.repositories = repositories;
  }
}
