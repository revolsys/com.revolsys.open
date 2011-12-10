package com.revolsys.maven;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.io.xml.XmlMapIoFactory;
import com.revolsys.spring.SpringUtil;

public class MavenRepository {

  private Resource root;

  public MavenRepository() {
  }

  /**
   * Root resource must end in a /
   * 
   * @param root
   */
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

  public Map<String, Object> getPom(final String groupId,
    final String artifactId, final String version) {
    Resource resource = getResource(groupId, artifactId, version, "pom");
    if (resource.exists()) {
      return XmlMapIoFactory.toMap(resource);
    } else {
      throw new IllegalArgumentException("Pom does not exist for " + groupId
        + ":" + artifactId + ":" + version + " at " + resource);
    }
  }

  public List<Map<String, Object>> getDependencies(final String groupId,
    final String artifactId, final String version) {
    Map<String, Object> pom = getPom(groupId, artifactId, version);
    Map<String, Object> dependencyMap = (Map<String, Object>)pom.get("dependencies");
    if (dependencyMap == null) {
      return Collections.emptyList();
    } else {
      List<Map<String, Object>> dependencyList = (List<Map<String, Object>>)dependencyMap.get("dependency");
      if (dependencyList == null) {
        return dependencyList;
      } else {
        return dependencyList;
      }
    }
  }

  protected StringBuffer getPath(final String groupId, final String artifactId,
    final String version, final String classifier, final String type) {
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
