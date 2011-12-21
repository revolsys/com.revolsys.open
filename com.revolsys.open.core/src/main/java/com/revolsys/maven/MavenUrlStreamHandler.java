package com.revolsys.maven;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.springframework.core.io.Resource;

public class MavenUrlStreamHandler extends URLStreamHandler {

  private MavenRepository mavenRepository;

  public MavenUrlStreamHandler(MavenRepository mavenRepository) {
    this.mavenRepository = mavenRepository;
  }

  @Override
  protected URLConnection openConnection(URL url) throws IOException {
    String protocol = url.getProtocol();
    if (protocol.equals("jar")) {
      String file = url.getFile();
      int separator = file.indexOf("!/");
      if (separator == -1) {
        throw new MalformedURLException("no !/ found in url spec:" + file);
      } else {

        String subUrl = file.substring(0, separator++);
        if (subUrl.startsWith("mvn")) {
          String mavenId = subUrl.substring(4);
          Resource resource = mavenRepository.getResource(mavenId);
          URL resourceUrl = resource.getURL();

          String entryName = "/";

          if (++separator != file.length()) {
            entryName = file.substring(separator-1, file.length());
          }
          String jarUrl = "jar:" + resourceUrl + "!" + entryName;
          return new URL(jarUrl).openConnection();
        }
      }
    } else if (protocol.equals("mvn")) {
      String mavenId = url.getFile();
      Resource resource = mavenRepository.getResource(mavenId);
      URL resourceUrl = resource.getURL();
      return resourceUrl.openConnection();
    }
    return new URL(url.toString()).openConnection();
  }
}
