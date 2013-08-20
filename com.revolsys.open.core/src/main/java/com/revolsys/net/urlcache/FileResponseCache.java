package com.revolsys.net.urlcache;

import java.io.File;
import java.io.IOException;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.ResponseCache;
import java.net.URI;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import com.revolsys.io.FileUtil;

public class FileResponseCache extends ResponseCache {
  private final File directory;

  public FileResponseCache(final File directory) {
    if (!directory.exists()) {
      directory.mkdirs();
    }
    this.directory = directory;
  }

  public FileResponseCache(final String directory) {
    this(new File(directory));
  }

  public FileResponseCache() {
    this(System.getProperty("java.io.tmpdir"));
  }

  @Override
  public CacheResponse get(final URI uri, final String method,
    final Map<String, List<String>> headers) throws IOException {
    if (headers.isEmpty() && method.equals("GET")) {
      final File file = toFile(uri);
      if (file != null && file.exists()) {
        return new FileCacheResponse(file, headers);
      }
    }
    return null;
  }

  @Override
  public CacheRequest put(final URI uri, final URLConnection connection)
    throws IOException {
    final File file = toFile(uri);
    if (file != null) {

      final long lastModified = connection.getLastModified();
      if (lastModified != 0) {
        // TODO doesn't work as the file is actually written by the connection
        file.setLastModified(lastModified);
      } else {
        // return null;
      }
      return new FileCacheRequest(file);
    }
    return null;
  }

  private File toFile(final URI uri) {
    final String scheme = uri.getScheme();
    if (scheme.equals("http") || scheme.equals("https")) {
      File file = new File(directory, scheme);
      final String host = uri.getHost();
      file = new File(file, host);
      final int port = uri.getPort();
      if (port != -1) {
        file = new File(file, String.valueOf(port));
      }
      String extension = null;
      String fileName = null;
      final String path = uri.getPath();
      if (path != null) {
        file = new File(file, path);
        if (!path.endsWith("/")) {
          extension = FileUtil.getFileNameExtension(file);
          if (extension.length() > 0) {
            fileName = FileUtil.getFileNamePrefix(file);
          } else {
            fileName = file.getName();
          }
          file = file.getParentFile();
        }
      }
      if (fileName == null) {
        final CRC32 crc32 = new CRC32();
        crc32.update(uri.toString().getBytes());
        fileName = String.valueOf(crc32.getValue());
      }
      final String query = uri.getQuery();
      if (query != null) {
        final CRC32 crc32 = new CRC32();
        crc32.update(query.getBytes());
        fileName += "-q" + crc32.getValue();
      }
      if (extension.length() > 0) {
        fileName = fileName + "." + extension;
      }
      return new File(file, fileName);
    }
    return null;
  }

}
