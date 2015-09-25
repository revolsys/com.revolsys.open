package com.revolsys.record.io.format.openstreetmap.pbf;

import java.util.ArrayList;
import java.util.List;

public class DenseInfo {
  final List<Long> changesets = new ArrayList<>();

  final List<Long> timestamps = new ArrayList<>();

  final List<Integer> uids = new ArrayList<>();

  final List<String> userNames = new ArrayList<>();

  final List<Integer> versions = new ArrayList<>();

  final List<Boolean> visibles = new ArrayList<>();

}
