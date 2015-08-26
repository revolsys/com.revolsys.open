package com.revolsys.ui.web.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Site implements Cloneable {
  private String name;

  /** The cache of pages that can be found from the decendents of this site. */
  private final Map pageCache = new HashMap();

  /** The list of pages defined as direct children of the site. */
  private final Map pages = new HashMap();

  private SiteNode rootNode = new SiteNode();

  public Site() {
  }

  public Site(final String name) {
    this.name = name;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    final Site site = new Site();

    return site;
  }

  /**
   * Get the site node controller for the specified path.
   *
   * @param path The path
   * @return The contoller or null if not found.
   */
  public SiteNodeController getController(final String path) {
    if (path.equals("/")) {
      return this.rootNode.getController();
    } else {
      return this.rootNode.getController(path);
    }
  }

  /**
   * Get the nane of the site.
   *
   * @return The nane of the site.
   */
  public String getName() {
    return this.name;
  }

  public Collection getNodes() {
    return this.rootNode.getNodes();
  }

  /**
   * @return Returns the rootNode.
   */
  public SiteNode getRootNode() {
    return this.rootNode;
  }

  /**
   * Set the nane of the site.
   *
   * @param name The name of the site.
   */
  public void setName(final String name) {
    this.name = name;
  }

  public void setNodes(final Collection nodes) {
    this.rootNode.setNodes(nodes);
  }

  /**
   * @param rootNode The rootNode to set.
   */
  public void setRootNode(final SiteNode rootNode) {
    this.rootNode = rootNode;
  }
}
