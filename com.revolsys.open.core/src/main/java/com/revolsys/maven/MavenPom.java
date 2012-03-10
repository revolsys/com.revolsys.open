package com.revolsys.maven;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.revolsys.util.CollectionUtil;

public class MavenPom extends LinkedHashMap<String, Object> {
  public static String getGroupAndArtifactId(String id) {
    String[] parts = id.split(":");
    if (parts.length < 2) {
      return id;
    } else {
      return parts[0] + ":" + parts[1];
    }
  }

  private final MavenRepository mavenRepository;

  private Map<String, Object> properties;

  public MavenPom(final MavenRepository mavenRepository,
    final Map<String, Object> pom) {
    super(pom);
    this.mavenRepository = mavenRepository;
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  public boolean addDependenciesFromTree(
    final Map<String, String> dependencies,
    final String dependencyPath,
    final Map<String, Map<String, Map>> dependencyTree,
    final int depth,
    final int searchDepth) {
    boolean hasChildren = false;
    final Set<Entry<String, Map<String, Map>>> entries = dependencyTree.entrySet();
    for (final Entry<String, Map<String, Map>> dependencyEntry : entries) {
      final String childDependencyId = dependencyEntry.getKey();
      final String childPath = dependencyPath + "/" + childDependencyId;
      final Map childTree = dependencyEntry.getValue();
      final String existingDependencyPath = dependencies.get(childDependencyId);
      if (childPath.equals(existingDependencyPath)) {
        if (depth < searchDepth) {
          if (addDependenciesFromTree(dependencies, childPath, childTree,
            depth + 1, searchDepth)) {
            hasChildren = true;
          }
        }
      } else if (!isDependencyIgnored(dependencies.keySet(), childDependencyId)) {
        if (depth == searchDepth) {
          dependencies.put(childDependencyId, childPath);
          if (!childTree.isEmpty()) {
            hasChildren = true;
          }
        } else if (addDependenciesFromTree(dependencies, childPath, childTree,
          depth + 1, searchDepth)) {
          hasChildren = true;
        }
      }
    }
    return hasChildren;
  }

  public ClassLoader createClassLoader() {
    return mavenRepository.createClassLoader(getMavenId());
  }

  public ClassLoader createClassLoader(final Collection<String> exclusionIds) {
    final String id = getMavenId();
    return mavenRepository.createClassLoader(id, exclusionIds);
  }

  public Set<String> getDependencies() {
    final Set<String> exclusionIds = Collections.emptySet();
    return getDependencies(exclusionIds);
  }

  public Set<String> getDependencies(final Collection<String> exclusionIds) {
    final Map<String, String> versions = getDependencyVersions();
    final Map<String, Map<String, Map>> dependencyTree = getDependencyTree(
      versions, exclusionIds, true);

    return getDependenciesFromTree(dependencyTree);
  }

  private Set<String> getDependenciesFromTree(
    final Map<String, Map<String, Map>> dependencyTree) {
    final Map<String, String> dependencyPaths = new LinkedHashMap<String, String>();
    int searchDepth = 0;
    while (addDependenciesFromTree(dependencyPaths, "", dependencyTree, 0,
      searchDepth)) {
      searchDepth++;
    }
    return dependencyPaths.keySet();
  }

  protected Map<String, Map<String, Map>> getDependencyTree(
    final Map<String, String> versions,
    final Collection<String> exclusionIds,
    final boolean includeOptional) {
    final Map<String, Map<String, Map>> dependencies = new LinkedHashMap<String, Map<String, Map>>();
    final Map<String, Object> dependencyMap = (Map<String, Object>)get("dependencies");
    if (dependencyMap != null) {
      final List<Map<String, Object>> dependencyList = getList(dependencyMap,
        "dependency");
      if (dependencyList != null) {
        for (final Map<String, Object> dependency : dependencyList) {
          final String groupId = getMapValue(dependency, "groupId", null);
          final String artifactId = getMapValue(dependency, "artifactId", null);
          final String dependencyKey = groupId + ":" + artifactId;
          String version = versions.get(dependencyKey);
          if (!StringUtils.hasText(version)) {
            version = getMapValue(dependency, "version", null);
          }
          final String scope = getMapValue(dependency, "scope", "compile");
          final String optional = getMapValue(dependency, "optional", "false");
          if (scope.equals("compile")
            && (includeOptional || "false".equals(optional))) {
            if (!StringUtils.hasText(version)) {
              if (groupId.equals(getGroupId())) {
                version = getVersion();
              }
            }
            if (!exclusionIds.contains(dependencyKey)
              && !exclusionIds.contains(groupId + ":*")) {
              try {
                final MavenPom pom = mavenRepository.getPom(groupId,
                  artifactId, version);
                final String dependencyId = pom.getMavenId();
                final Set<String> mergedExclusionIds = new HashSet<String>(
                  exclusionIds);
                mergedExclusionIds.addAll(getExclusionIds(dependency));

                // Add child dependencies first so they don't override parent
                final Map<String, String> mergedVersions = new HashMap<String, String>();
                mergedVersions.putAll(pom.getDependencyVersions());
                mergedVersions.putAll(versions);

                final Map childDependencyTree = pom.getDependencyTree(
                  mergedVersions, mergedExclusionIds, false);
                dependencies.put(dependencyId, childDependencyTree);
              } catch (final Exception e) {
                throw new IllegalArgumentException(
                  "Unable to download pom for " + dependencyKey + ":" + version
                    + " in pom " + getMavenId(), e);
              }
            }
          }
        }
      }
    }
    return dependencies;
  }

  public Map<String, String> getDependencyVersions() {
    final Map<String, String> versions = new HashMap<String, String>();
    final MavenPom parent = getParentPom();
    if (parent != null) {
      versions.putAll(parent.getDependencyVersions());
    }
    final Map<String, Object> dependencyManagement = (Map<String, Object>)get("dependencyManagement");
    if (dependencyManagement != null) {
      final Map<String, Object> dependencyMap = (Map<String, Object>)dependencyManagement.get("dependencies");
      if (dependencyMap != null) {
        final List<Map<String, Object>> dependencyList = getList(dependencyMap,
          "dependency");
        if (dependencyList != null) {
          for (final Map<String, Object> dependency : dependencyList) {
            final String groupId = getMapValue(dependency, "groupId", null);
            final String artifactId = getMapValue(dependency, "artifactId",
              null);
            final String version = getMapValue(dependency, "version", null);
            versions.put(groupId + ":" + artifactId, version);
          }
        }
      }
    }
    return versions;
  }

  public Set<String> getExclusionIds(final Collection<String> dependencyIds) {
    final Set<String> exclusionIds = new LinkedHashSet<String>();
    for (final String dependencyId : dependencyIds) {
      final int index1 = dependencyId.indexOf(':');
      if (index1 != -1) {
        final int index2 = dependencyId.indexOf(':', index1 + 1);
        if (index2 != -1) {
          final String exclusionId = dependencyId.substring(0, index2);
          exclusionIds.add(exclusionId);
        }
      }
    }
    return exclusionIds;
  }

  public Set<String> getExclusionIds(final Map<String, Object> dependency) {
    final Set<String> exclusionIds = new LinkedHashSet<String>();
    final Map<String, Object> exclusionsMap = (Map<String, Object>)dependency.get("exclusions");
    if (exclusionsMap != null) {
      final List<Map<String, Object>> exclusionsList = getList(exclusionsMap,
        "exclusion");
      if (exclusionsList != null) {
        for (final Map<String, Object> exclusion : exclusionsList) {
          final String groupId = getMapValue(exclusion, "groupId", null);
          final String artifactId = getMapValue(exclusion, "artifactId", null);
          final String exclusionId = groupId + ":" + artifactId;
          exclusionIds.add(exclusionId);
        }
      }
    }
    return exclusionIds;
  }

  public String getGroupId() {
    final String groupId = (String)get("groupId");
    if (groupId == null) {
      final Map<String, Object> parent = (Map<String, Object>)get("parent");
      if (parent == null) {
        return null;
      } else {
        return (String)parent.get("groupId");
      }
    } else {
      return groupId;
    }
  }

  public <T> List<T> getList(final Map<String, Object> map, final String key) {
    final Object value = map.get(key);
    if (value instanceof List) {
      return (List<T>)value;
    } else if (value == null) {
      return Collections.emptyList();
    } else {
      return (List<T>)Arrays.asList(value);
    }
  }

  public String getMapValue(
    final Map<String, Object> map,
    final String key,
    final String defaultValue) {
    String value = CollectionUtil.get(map, key, defaultValue);
    value = CollectionUtil.replaceProperties(value, getProperties());
    return value;
  }

  public String getMavenId() {
    final String groupId = getGroupId();
    final String artifactId = getMapValue(this, "artifactId", null);
    final String type = getMapValue(this, "packaging", "jar");
    final String classifier = getMapValue(this, "classifier", null);
    final String version = getVersion();
    final String scope = getMapValue(this, "scope", "compile");
    return MavenRepository.getMavenId(groupId, artifactId, type, classifier,
      version, scope);
  }

  public String getMavenId(final Map<String, Object> dependency) {
    final String groupId = getMapValue(dependency, "groupId", null);
    final String artifactId = getMapValue(dependency, "artifactId", null);
    final String type = getMapValue(dependency, "type", "jar");
    final String classifier = getMapValue(dependency, "classifier", null);
    final String version = getMapValue(dependency, "version", null);
    final String scope = getMapValue(dependency, "scope", "compile");
    return MavenRepository.getMavenId(groupId, artifactId, type, classifier,
      version, scope);
  }

  public MavenPom getParentPom() {
    final Map<String, Object> parent = (Map<String, Object>)get("parent");
    if (parent == null) {
      return null;
    } else {
      final String groupId = (String)parent.get("groupId");
      final String artifactId = (String)parent.get("artifactId");
      final String version = (String)parent.get("version");
      return mavenRepository.getPom(groupId, artifactId, version);
    }
  }

  public Map<String, Object> getProperties() {
    if (properties == null) {
      properties = new LinkedHashMap<String, Object>();
      final MavenPom parentPom = getParentPom();
      if (parentPom != null) {
        final Map<String, Object> parentProperties = parentPom.getProperties();
        properties.putAll(parentProperties);
      }

      final Map<String, Object> pomProperties = (Map<String, Object>)get("properties");
      if (pomProperties != null) {
        properties.putAll(pomProperties);
      }
      properties.put("project.artifactId", get("artifactId"));
      properties.put("project.version", getVersion());
      properties.put("project.groupId", getGroupId());
    }
    return properties;
  }

  public String getVersion() {
    final String version = (String)get("version");
    if (version == null) {
      final Map<String, Object> parent = (Map<String, Object>)get("parent");
      if (parent == null) {
        return null;
      } else {
        return (String)parent.get("version");
      }
    } else {
      return version;
    }
  }

  public boolean isDependencyIgnored(
    final Set<String> dependencies,
    final String dependencyId) {
    for (final String matchedDependencyId : dependencies) {
      boolean match = true;
      final String[] parts = dependencyId.split(":");
      final String[] matchParts = matchedDependencyId.split(":");
      if (matchParts.length == parts.length) {
        for (int i = 0; i < parts.length - 2; i++) {
          final String value1 = parts[i];
          final String value2 = matchParts[i];
          if (!value1.equals(value2)) {
            match = false;
          }
        }
        if (match) {
          return true;
        }
      }
    }
    return false;
  }

}
