 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 

uchar4 slopeColorGradient(
  float *m,
  const int hasValue,
  const float xFactor,
  const float yFactor,
  const int rangeCount,
  const __global float *slopes,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange,
  __global uchar4 *output
) {
  if (hasValue == 0) {
    return (uchar4)(0);
  } else {
    float dzDivDx = deltaZX(m, xFactor);
    float dzDivDy = deltaZY(m, yFactor);

    float slopeRadians = slope(dzDivDx, dzDivDy, 1);
    
    int index = rangeIndexFloat(slopes, rangeCount, slopeRadians);
    float percent = rangePercentFloat(slopes, index, slopeRadians);
    
    return rangeColor(index, percent, rangeCount, rRange, gRange, bRange);
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
  int hasValue = subGridInt3x3(cells, width, height, m, imageX, imageY, offsetZ, scaleZ);
  output[imageY * width + imageX] = slopeColorGradient(m, hasValue, xFactor, yFactor, rangeCount, slopes, rRange, gRange, bRange, output);
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
  int hasValue = subGridFloat3x3(cells, width, height, m, imageX, imageY);
  output[imageY * width + imageX] = slopeColorGradient(m, hasValue, xFactor, yFactor, rangeCount, slopes, rRange, gRange, bRange, output);
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
  int hasValue = subGridDouble3x3(cells, width, height, m, imageX, imageY);
  output[imageY * width + imageX] = slopeColorGradient(m, hasValue, xFactor, yFactor, rangeCount, slopes, rRange, gRange, bRange, output);
}