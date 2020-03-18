 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 
 uchar4 colorPercent(
  const float percent,
  const uchar4 minColor,
  const uchar4 maxColor
) {
  if (percent <= 0) {
    return minColor;
  } else if (percent >= 1) {
    return maxColor;
  } else {
  
    int red = round(minColor[2] + percent * (maxColor[2] - minColor[2]));
    int green = round(minColor[1] + percent * (maxColor[1] - minColor[1]));
    int blue = round(minColor[0] + percent * (maxColor[0] - minColor[0]));
    return (uchar4)(blue, green , red, 255);
  }
}

uchar4 color(
  const float z,
  const float minZ,
  const float rangeZ, 
  const uchar4 minColor,
  const uchar4 maxColor
) {
  if (isnan(z)) {
    return (uchar4)(0);
  } else {
    float percent = (z - minZ) / rangeZ;
    return colorPercent(percent, minColor, maxColor);
  }
}

__kernel void colorRasterizer_int(
  __global int *cells,
  const int width, 
  const int height, 
  const int minZInt,
  const int rangeZInt, 
  const uchar4 minColor,
  const uchar4 maxColor, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  int elevationInt = cells[gridY * width + gridX];
  
  if (elevationInt == -2147483648) {
    output[imageY * width + imageX] = (uchar4)(0);
  } else {
    float percent = (float)(elevationInt - minZInt) / rangeZInt;
    output[imageY * width + imageX] = colorPercent(percent, minColor, maxColor);
  }
}

__kernel void colorRasterizer_float(
  __global float *cells,
  const int width, 
  const int height, 
  const float minZ,
  const float rangeZ, 
  const uchar4 minColor,
  const uchar4 maxColor, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  float z = cells[gridY * width + gridX];
  output[imageY * width + imageX] = color(z, minZ, rangeZ, minColor, maxColor);
}

__kernel void colorRasterizer_double(
  __global double *cells,
  const int width, 
  const int height, 
  const float minZ,
  const float rangeZ, 
  const uchar4 minColor,
  const uchar4 maxColor, 
  __global uchar4 *output
) {
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  double z = cells[gridY * width + gridX];
  output[imageY * width + imageX] = color(z, minZ, rangeZ, minColor, maxColor);
}