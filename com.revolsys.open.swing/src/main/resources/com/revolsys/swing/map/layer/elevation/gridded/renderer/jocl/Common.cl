#pragma OPENCL EXTENSION cl_khr_fp64 : enable
 
float slope(float dzDivDx, float dzDivDy, float zFactor) {
  return atan(zFactor * sqrt(dzDivDx * dzDivDx + dzDivDy * dzDivDy));
}
 
float aspect(float dzDivDx, float dzDivDy) {
  double aspect = 0;
  if (dzDivDx == 0) {
    if (dzDivDy > 0) {
      aspect = M_PI * 2;
    } else if (dzDivDy < 0) {
      aspect = M_PI * 2 - M_PI / 2;
    } else {
      aspect = 0;
    }
  } else {
    aspect = atan2(dzDivDy, -dzDivDx);
    if (aspect < 0) {
      aspect = M_PI * 2 + aspect;
    }
  }
  return aspect;
}

int fixNulls3x3(float* m) {
  if (isnan(m[4])) {
    return 0;
  } else {
    if (isnan(m[3])) {
      if (isnan(m[5])) {
        m[3] = m[4];
        m[5] = m[4];
      } else {
        m[3] = m[4] - (m[5] - m[4]);
      }
    } else if (isnan(m[5])) {
      m[5] = m[4];
    }
    if (isnan(m[0])) {
      if (isnan(m[6])) {
        m[0] = m[3];
      } else {
        m[0] = m[3] - (m[6] - m[3]);
      }
    }
    if (isnan(m[1])) {
      if (isnan(m[7])) {
        m[1] = m[4];
      } else {
        m[1] = m[4] - (m[7] - m[4]);
      }
    }
    if (isnan(m[2])) {
      if (isnan(m[8])) {
        m[2] = m[5];
      } else {
        m[2] = m[5] - (m[8] - m[5]);
      }
    }
    if (isnan(m[6])) {
      m[6] = m[3] - (m[0] - m[3]);
    }
    if (isnan(m[7])) {
      m[7] = m[4] - (m[1] - m[4]);
    }
    if (isnan(m[8])) {
      m[8] = m[5] - (m[2] - m[5]);
    }
    return 1;
  }
}

int subGridInt3x3(const __global int *cells, int width, int height, float* m, int imageX, int imageY, float offsetZ, float scaleZ) {
  int gridX = imageX;
  int gridY = height - imageY - 1;

  for (int i = 0; i < 9; i++) {
    m[i] = NAN;
  }
  int startY = gridY - 1;
  if (startY < 0) {
    startY = 0;
  }
  int endY = gridY + 1;
  if (endY >= height) {
    endY = height - 1;
  }
  int startX = gridX - 1;
  if (startX < 0) {
    startX = 0;
  }
  int endX = gridX + 1;
  if (endX >= width) {
    endX = width - 1;
  }
  int i = 0;
  for (int y = endY; y >= startY; y--) {
    for (int x = startX; x <= endX; x++) {
      int zInt = cells[y * width + x];
      if (zInt != -2147483648) {
        m[i] = offsetZ + zInt / scaleZ;
      } 
      i++;
    }
  }
  return fixNulls3x3(m);
}

int subGridFloat3x3(const __global float *cells, int width, int height, float* m, int imageX, int imageY) {
  int gridX = imageX;
  int gridY = height - imageY - 1;

  for (int i = 0; i < 9; i++) {
    m[i] = NAN;
  }
  int startY = gridY - 1;
  if (startY < 0) {
    startY = 0;
  }
  int endY = gridY + 1;
  if (endY >= height) {
    endY = height - 1;
  }
  int startX = gridX - 1;
  if (startX < 0) {
    startX = 0;
  }
  int endX = gridX + 1;
  if (endX >= width) {
    endX = width - 1;
  }
  int i = 0;
  for (int y = endY; y >= startY; y--) {
    for (int x = startX; x <= endX; x++) {
      m[i] = cells[y * width + x];
      i++;
    }
  }
  return fixNulls3x3(m);
}

int subGridDouble3x3(const __global double *cells, int width, int height, float* m, int imageX, int imageY) {
  int gridX = imageX;
  int gridY = height - imageY - 1;

  for (int i = 0; i < 9; i++) {
    m[i] = NAN;
  }
  int startY = gridY - 1;
  if (startY < 0) {
    startY = 0;
  }
  int endY = gridY + 1;
  if (endY >= height) {
    endY = height - 1;
  }
  int startX = gridX - 1;
  if (startX < 0) {
    startX = 0;
  }
  int endX = gridX + 1;
  if (endX >= width) {
    endX = width - 1;
  }
  int i = 0;
  for (int y = endY; y >= startY; y--) {
    for (int x = startX; x <= endX; x++) {
      m[i] = (float)cells[y * width + x];
      i++;
    }
  }
  return fixNulls3x3(m);
}

float deltaZX(float* m, float xFactor) {
  return (m[2] + 2 * m[5] + m[8] - (m[0] + 2 * m[3] + m[6])) * xFactor;
}

float deltaZY(float* m, float yFactor) {
  return (m[6] + 2 * m[7] + m[8] - (m[0] + 2 * m[1] + m[2])) * yFactor;
}

int rangeIndexFloat(
  const __global float *ranges,
  const int rangeCount,
  const float value
) {
  if (value <= ranges[0]) {
    return 0;
  } else {
    for (int i = 1; i < rangeCount; i++) {
      float rangeCurrent = ranges[i];
      if (value <= rangeCurrent) {
        return i;
      }
    }
  }
  return -1;
}

int rangeIndexInt(
  const __global int *ranges,
  const int rangeCount,
  const float value
) {
  if (value <= ranges[0]) {
    return 0;
  } else {
    for (int i = 1; i < rangeCount; i++) {
      int rangeCurrent = ranges[i];
      if (value <= rangeCurrent) {
        return i;
      }
    }
  }
  return -1;
}

float rangePercentFloat(
  const __global float *ranges,
  const int index,
  const float value
) {
  if (index == 0) {
    return 0;
  } else if (index < 0) {
    return 0;
  } else {
    float previous = ranges[index - 1];
    float next = ranges[index];
    return (value - previous) / (next - previous);
  }
}

float rangePercentInt(
  const __global int *ranges,
  const int index,
  const float value
) {
  if (index == 0) {
    return 0;
  } else if (index < 0) {
    return 0;
  } else {
    int previous = ranges[index - 1];
    int next = ranges[index];
    return (float)(value - previous) / (next - previous);
  }
}

int rangeOffsetInt( 
  const __global int *values,
  const int rangeCount,
  const int index,
  const float percent
) {
  int next = values[index];
  if (index == 0) {
    return values[0];
  } else if (index == -1) {
    return values[rangeCount - 1];
  } else {
    int previous = values[index -1];
    return round(previous + (next - previous) * percent);
  }
}

uchar4 rangeColor(
  const int index,
  const float percent,
  const int rangeCount,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange
) {
  int red = rangeOffsetInt(rRange, rangeCount, index, percent);
  int green = rangeOffsetInt(gRange, rangeCount, index, percent);
  int blue = rangeOffsetInt(bRange, rangeCount, index, percent);
  return (uchar4)(blue, green , red, 255);
}