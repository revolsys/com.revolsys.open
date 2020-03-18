 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 
 __kernel void colorGradientRasterizer_int(
   const __global int *cells,
   const int width, 
   const int height,
   const int rangeCount,
   const __global int *zRange,
   const __global int *rRange,
   const __global int *gRange,
   const __global int *bRange, 
    __global uchar4 *output) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  int z = cells[gridY * width + gridX];

  if (z == -2147483648) {
    output[imageY * width + imageX] = (uchar4)(0);
  } else {
    int index = rangeIndexInt(zRange, rangeCount, z);
    float percent = rangePercentInt(zRange, index, z);
    output[imageY * width + imageX] = rangeColor(index, percent, rangeCount, rRange, gRange, bRange);
  }
}

uchar4 colorGradient(
  const float z,
  const int rangeCount,
  const __global float *zRange,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange
) {
  if (isnan(z)) {
    return (uchar4)(0);
  } else {
    int index = rangeIndexFloat(zRange, rangeCount, z);
    float percent = rangePercentFloat(zRange, index, z);
    return rangeColor(index, percent, rangeCount, rRange, gRange, bRange);
  }
}

__kernel void colorGradientRasterizer_float(
  const __global float *cells,
  const int width, 
  const int height,
  const int rangeCount,
  const __global float *zRange,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  float z = cells[gridY * width + gridX];

  output[imageY * width + imageX] = colorGradient(z, rangeCount, zRange, rRange, gRange, bRange);
}

__kernel void colorGradientRasterizer_double(
  const __global double *cells,
  const int width, 
  const int height,
  const int rangeCount,
  const __global float *zRange,
  const __global int *rRange,
  const __global int *gRange,
  const __global int *bRange, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  double z = cells[gridY * width + gridX];

  output[imageY * width + imageX] = colorGradient(z, rangeCount, zRange, rRange, gRange, bRange);
}
