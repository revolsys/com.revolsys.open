package com.revolsys.maven;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.spring.SpringUtil;

public class MavenRepository {

  private Resource root;

  public MavenRepository() {
  }

  public MavenRepository(final Resource root) {
    this.root = root;
  }

  public Resource getResource(final String groupId, final String artifactId,
    final String version, final String type) {
    return getResource(groupId, artifactId, version, null, type);
  }

  public Resource getResource(final String groupId, final String artifactId,
    final String version, final String classifier, final String type) {
    final StringBuffer path = getPath(groupId, artifactId, version, classifier,
      type);
    Resource artifactResource = SpringUtil.getResource(root, path);
    if (!artifactResource.exists()) {
      handleMissingResource(artifactResource, groupId, artifactId, version,
        classifier, type);
    }
    return artifactResource;
  }

  protected StringBuffer getPath(final String groupId, final String artifactId,
    final String version, final String classifier, final String type) {
    final StringBuffer path = new StringBuffer('/');
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
    return path;
  }

  protected void handleMissingResource(Resource resource, String groupId,
    String artifactId, String version, String classifier, String type) {
  }

  public Resource getRoot() {
    return root;
  }

  public void setRoot(final Resource root) {
    this.root = root;
  }
}
