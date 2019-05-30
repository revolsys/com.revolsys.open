package com.revolsys.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.jeometry.common.exception.WrappedException;
import org.jeometry.common.logging.Logs;

import java.util.TreeMap;

import com.revolsys.collection.map.MapEx;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Hex;
import com.revolsys.util.Property;

public class MavenRepositoryCache extends MavenRepository {
  private List<MavenRepository> repositories = new ArrayList<>();

  public MavenRepositoryCache() {
  }

  public MavenRepositoryCache(final Resource root) {
    super(root);
  }

  public MavenRepositoryCache(final Resource root, final String... repositoryUrls) {
    super(root);
    for (String repositoryUrl : repositoryUrls) {
      if (!repositoryUrl.endsWith("/")) {
        repositoryUrl += '/';
      }
      final Resource resource = Resource.getResource(repositoryUrl);
      final MavenRepository repository = new MavenRepository(resource);
      this.repositories.add(repository);
    }
  }

  public MavenRepositoryCache(final String... repositoryUrls) {
    this(null, repositoryUrls);
  }

  public boolean copyRepositoryResource(final Resource resource, final MavenRepository repository,
    final String path, final String sha1Digest) {
    final Resource repositoryResource = repository.getRoot().newChildResource(path);
    try {
      if (Property.hasValue(sha1Digest)) {
        final InputStream in = repositoryResource.getInputStream();
        final DigestInputStream digestIn = new DigestInputStream(in,
          MessageDigest.getInstance("SHA-1"));
        resource.copyFrom(digestIn);
        final MessageDigest messageDigest = digestIn.getMessageDigest();
        final byte[] digest = messageDigest.digest();
        final String fileDigest = Hex.toHex(digest);
        if (!sha1Digest.equals(fileDigest)) {
          Logs.error(this, ".sha1 digest is different for: " + repositoryResource);
          resource.delete();
          return false;
        }
      } else {
        resource.copyFrom(repositoryResource);
      }
      return true;
    } catch (Throwable e) {
      resource.delete();
      while (e instanceof WrappedException) {
        e = e.getCause();
      }
      if (e instanceof FileNotFoundException) {
        return false;
      } else if (e instanceof IOException) {
        final IOException ioException = (IOException)e;
        if (ioException.getMessage().contains(" 404 ")) {
          return false;
        }
      }
      Logs.error(this,
        "Unable to download MVN resource " + repositoryResource + "\n  " + e.getMessage());
      return false;
    }
  }

  public List<MavenRepository> getRepositories() {
    return this.repositories;
  }

  @Override
  protected Resource handleMissingResource(final Resource resource, final String groupId,
    final String artifactId, final String type, final String classifier, final String version,
    final String algorithm) {
    if (version != null && version.endsWith("-SNAPSHOT")) {
      final TreeMap<String, MavenRepository> versionsByRepository = new TreeMap<>();

      for (final MavenRepository repository : this.repositories) {
        final MapEx mavenMetadata = repository.getMavenMetadata(groupId, artifactId, version);
        final String snapshotVersion = getSnapshotVersion(mavenMetadata);
        if (snapshotVersion != null) {
          final String timestampVersion = version.replaceAll("SNAPSHOT$", snapshotVersion);
          versionsByRepository.put(timestampVersion, repository);
        }
      }
      if (!versionsByRepository.isEmpty()) {
        final Entry<String, MavenRepository> entry = versionsByRepository.lastEntry();
        final String timestampVersion = entry.getKey();

        final String path = getPath(groupId, artifactId, version, type, classifier,
          timestampVersion, algorithm);
        final Resource cachedResource = getRoot().newChildResource(path);
        if (cachedResource.exists()) {
          return cachedResource;
        } else {

          final MavenRepository repository = entry.getValue();
          final String sha1Digest = repository.getSha1(groupId, artifactId, type, classifier,
            version, algorithm);

          if (copyRepositoryResource(cachedResource, repository, path, sha1Digest)) {
            return cachedResource;
          }
        }

      }
    }
    final String path = getPath(groupId, artifactId, version, type, classifier, version, algorithm);
    for (final MavenRepository repository : this.repositories) {
      final String sha1Digest = repository.getSha1(groupId, artifactId, type, classifier, version,
        algorithm);
      if (copyRepositoryResource(resource, repository, path, sha1Digest)) {
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
      this.repositories.add(new MavenRepository(resource));
    }
  }

  @Override
  public void setRoot(final Resource root) {
    if (root != null) {
      try {
        final File file = root.getFile();
        if (!file.exists()) {
          if (!file.mkdirs()) {
            throw new IllegalArgumentException("Cannot create maven cache directory " + file);
          }
        } else if (!file.isDirectory()) {
          throw new IllegalArgumentException("Maven cache is not a directory directory " + file);
        }
        final FileSystemResource fileResource = new FileSystemResource(file);
        super.setRoot(fileResource);
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Maven cache must resolve to a local directory " + root);
      }
    }
  }
}
