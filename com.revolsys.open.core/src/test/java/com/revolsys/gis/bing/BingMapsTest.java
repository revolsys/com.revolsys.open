package com.revolsys.gis.bing;

import com.revolsys.gis.bing.BingClient.ImagerySet;

public class BingMapsTest {

  public static void main(String[] args) {
    BingClient client = new BingClient(
      "ArIUlgTPb9o1ZxL_dqrMpjv3FYzgBdgVKua-8czp3OVpMjGjOIzwoZevIk_gSk4i");
    System.out.println(client.get(ImagerySet.AerialWithLabels, null, "jpeg", -122.325, 45.219,
      -122.107, 47.610, 800, 600));
  }
}
