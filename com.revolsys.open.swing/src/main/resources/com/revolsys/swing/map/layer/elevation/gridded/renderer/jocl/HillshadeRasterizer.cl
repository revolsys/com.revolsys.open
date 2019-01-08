 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 
uchar4 hillshade(
  float *m,
  const int hasValue,
  const float azimuthRadians,
  const float cosZenithRadians,
  const float sinZenithRadians,
  const float xFactor,
  const float yFactor,
  const float zFactor,
  __global uchar4 *output
) {
  if (hasValue == 0) {
    return (uchar4)(0);
  } else {
    float dzDivDx = deltaZX(m, xFactor);
    float dzDivDy = deltaZY(m, yFactor);

    float slopeRadians = slope(dzDivDx, dzDivDy, zFactor);
    float aspectRadians = aspect(dzDivDx, dzDivDy);
    
    int hs = round((cosZenithRadians * cos(slopeRadians) + sinZenithRadians
      * sin(slopeRadians) * cos(azimuthRadians - aspectRadians)) * 255);
    if (hs < 0) {
      hs = 0;
    } else if (hs > 255) {
      hs = 255;
    }
    return (uchar4)(hs, hs, hs, 255);
  }
}

 __kernel void hillshadeRasterizer_int(
  const __global int *cells,
  const int width,
  const int height,
  const float offsetZ,
  const float scaleZ,
  const float azimuthRadians,
  const float cosZenithRadians,
  const float sinZenithRadians,
  const float xFactor,
  const float yFactor,
  const float zFactor,
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  float m[9];
  int hasValue = subGridInt3x3(cells, width, height, m, imageX, imageY, offsetZ, scaleZ);
  output[imageY * width + imageX] = hillshade(m, hasValue, azimuthRadians, cosZenithRadians, sinZenithRadians, xFactor, yFactor, zFactor, output);
}

 __kernel void hillshadeRasterizer_float(
  const __global float *cells,
  const int width,
  const int height,
  const float azimuthRadians,
  const float cosZenithRadians,
  const float sinZenithRadians,
  const float xFactor,
  const float yFactor,
  const float zFactor,
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  float m[9];
  int hasValue = subGridFloat3x3(cells, width, height, m, imageX, imageY);
  output[imageY * width + imageX] = hillshade(m, hasValue, azimuthRadians, cosZenithRadians, sinZenithRadians, xFactor, yFactor, zFactor, output);
}

 __kernel void hillshadeRasterizer_double(
  const __global double *cells,
  const int width,
  const int height,
  const float azimuthRadians,
  const float cosZenithRadians,
  const float sinZenithRadians,
  const float xFactor,
  const float yFactor,
  const float zFactor,
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  float m[9];
  int hasValue = subGridDouble3x3(cells, width, height, m, imageX, imageY);
  output[imageY * width + imageX] = hillshade(m, hasValue, azimuthRadians, cosZenithRadians, sinZenithRadians, xFactor, yFactor, zFactor, output);
}