 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 
 
uchar4 slopeColorGradient(
  float *m,
  const int isNull,
  const float xFactor,
  const float yFactor,
  const int rangeCount,
  const __global float *slopes,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange,
  __global uchar4 *output
) {
  if (isNull == 0) {
    return (uchar4)(0);
  } else {
    float dzDivDx = deltaZX(m, xFactor);
    float dzDivDy = deltaZY(m, yFactor);

    float slopeRadians = slope(dzDivDx, dzDivDy, 1);
     
    int red = rRange[rangeCount - 1];
    int green = gRange[rangeCount - 1];
    int blue = bRange[rangeCount - 1];
    if (slopeRadians <= slopes[0]) {
      red = rRange[0];
      green = gRange[0];
      blue = bRange[0];
    } else {
      for (int i = 1; i < rangeCount; i++) {
        float slopeCurrent = slopes[i];
        if (slopeRadians <= slopeCurrent) {
          int previousI = i - 1;
          float slopePrevious = slopes[previousI];
          float percent = (float)(slopeRadians - slopePrevious) / (slopeCurrent - slopePrevious);
          int previousR = rRange[previousI];
          red = round((previousR + (rRange[i] - previousR) * percent));
          int previousG = gRange[previousI];
          green = round((previousG + (gRange[i] - previousG) * percent));
          int previousB = bRange[previousI];
          blue = round((previousB + (bRange[i] - previousB) * percent));
          break;
        }
      }
    }
    return (uchar4)(blue, green , red, 255);
  }
}

 __kernel void slopeColorGradientRasterizer_int(
  const __global int *cells,
  const int width, 
  const int height,
  const float offsetZ, 
  const float scaleZ, 
  const float xFactor,
  const float yFactor,
  const int rangeCount,
  const __global float *slopes,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  float m[9];
  int isNull = subGridInt3x3(cells, width, height, m, imageX, imageY, offsetZ, scaleZ);
  output[imageY * width + imageX] = slopeColorGradient(m, isNull, xFactor, yFactor, rangeCount, slopes, rRange, gRange, bRange, output);
}

 __kernel void slopeColorGradientRasterizer_float(
  const __global float *cells,
  const int width, 
  const int height,
  const float xFactor,
  const float yFactor,
  const int rangeCount,
  const __global float *slopes,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  float m[9];
  int isNull = subGridFloat3x3(cells, width, height, m, imageX, imageY);
  output[imageY * width + imageX] = slopeColorGradient(m, isNull, xFactor, yFactor, rangeCount, slopes, rRange, gRange, bRange, output);
}

 __kernel void slopeColorGradientRasterizer_double(
  const __global double *cells,
  const int width, 
  const int height,
  const float xFactor,
  const float yFactor,
  const int rangeCount,
  const __global float *slopes,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  float m[9];
  int isNull = subGridDouble3x3(cells, width, height, m, imageX, imageY);
  output[imageY * width + imageX] = slopeColorGradient(m, isNull, xFactor, yFactor, rangeCount, slopes, rRange, gRange, bRange, output);
}