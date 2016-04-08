package com.revolsys.ui.model;

import java.util.ArrayList;
import java.util.List;

public class Navbar extends Menu {
  private List<Brand> brands = new ArrayList<>();

  private String navbarAlign = "left";

  public List<Brand> getBrands() {
    return this.brands;
  }

  public String getNavbarAlign() {
    return this.navbarAlign;
  }

  public void setBrands(final List<Brand> brands) {
    this.brands = brands;
  }

  public void setNavbarAlign(final String navbarAlign) {
    this.navbarAlign = navbarAlign;
  }
}
