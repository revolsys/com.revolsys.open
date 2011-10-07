package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.List;

public class MemoryPageManager implements PageManager {

  private List<byte[]> pages = new ArrayList<byte[]>();

  public byte[] getPage(int index) {
    return pages.get(index);
  }
}
