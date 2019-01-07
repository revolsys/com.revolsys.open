 #pragma OPENCL EXTENSION cl_khr_fp64 : enable
 __kernel void colorRasterizerInt(
    __global int *cells,
   const int width, 
   const int height, 
   const int minZInt,
   const int rangeZInt, 
    __global char4 *output)
{
  int imageX = get_global_id(0);
  int imageY = get_global_id(1);

  int gridX = imageX;
  int gridY = height - imageY - 1;
  int elevationInt = cells[gridY * width + gridX];
  
  if (elevationInt == -2147483648) {
    output[imageY * width + imageX] = (char4)(0);
  } else {
    float percent = (float)(elevationInt - minZInt) / rangeZInt;
    if (percent < 0) {
      percent = 0;
    } else if (percent > 1) {
      percent = 1;
    }
    int c = round(percent * 255);
    output[imageY * width + imageX] = (char4)(c, c, c, 255);
  }
}
