// package com.revolsys.elevation.cloud.las.zip.compress;
//
// import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
// import com.revolsys.io.channels.ChannelWriter;
//
// public abstract class LasWriteItemRaw {
// protected final ChannelWriter outstream;
//
// public LasWriteItemRaw(final ChannelWriter outstream) {
// this.outstream = outstream;
// }
// }
//
// class LasWriteItemRaw_POINT10 extends LasWriteItemRaw {
//
// LasWriteItemRaw_POINT10(final ChannelWriter outstream) {
// super(outstream);
// }
//
// public void write(final LasPoint point, final int context)
// {
// final int xInt = point.getXInt();
// final int yInt = point.getYInt();
// final int zInt = point.getZInt();
//
// this.outstream.putInt(xInt);
// this.outstream.putInt(yInt);
// this.outstream.putInt(zInt);
//
// ENDIAN_SWAP_32(item[ 0], swapped[ 0]); // X
// ENDIAN_SWAP_32(item[ 4], swapped[ 4]); // Y
// ENDIAN_SWAP_32(item[ 8], swapped[ 8]); // Z
// ENDIAN_SWAP_16(item[12], swapped[12]); // intensity
// *(U32*)swapped[14] = *U32*)item[14]; // bitfield, classification,
// scan_angle_rank, user_data
// ENDIAN_SWAP_16(item[18], swapped[18]); // point_source_ID
// this.outstream.putBytes(swapped, 20);
// }private U8 swapped[20];
// }
//
////
//// class LasWriteItemRaw_GPSTIME11 extends LasWriteItemRaw
//// {
////
//// LasWriteItemRaw_GPSTIME11(final ChannelWriter outstream){super(outstream);}
//// public void write(LasPoint point, int context)
//// {
//// ENDIAN_SWAP_64(item, swapped);
//// outstream.putBytes(swapped, 8);
//// }
//// private
//// U8 swapped[8];
//// }
////
////
//// class LasWriteItemRaw_RGB12 extends LasWriteItemRaw
//// {
////
//// LasWriteItemRaw_RGB12(final ChannelWriter outstream){super(outstream);}
//// public void write(LasPoint point, int context)
//// {
//// ENDIAN_SWAP_32(item[ 0], swapped[ 0]); // R
//// ENDIAN_SWAP_32(item[ 2], swapped[ 2]); // G
//// ENDIAN_SWAP_32(item[ 4], swapped[ 4]); // B
//// outstream.putBytes(swapped, 6);
//// }
//// private
//// U8 swapped[6];
//// }
////
////
//// class LasWriteItemRaw_WAVEPACKET13 extends LasWriteItemRaw
//// {
////
//// LasWriteItemRaw_WAVEPACKET13(final ChannelWriter
//// outstream){super(outstream);}
//// public void write(LasPoint point, int context)
//// {
//// swapped[0] = item[0]; // wavepacket descriptor index
//// ENDIAN_SWAP_64(item[ 1], swapped[ 1]); // byte offset to waveform data
//// ENDIAN_SWAP_32(item[ 9], swapped[ 9]); // waveform packet size in bytes
//// ENDIAN_SWAP_32(item[13], swapped[13]); // return point waveform location
//// ENDIAN_SWAP_32(item[17], swapped[17]); // X(t)
//// ENDIAN_SWAP_32(item[21], swapped[21]); // Y(t)
//// ENDIAN_SWAP_32(item[25], swapped[25]); // Z(t)
//// outstream.putBytes(swapped, 29);
//// }
//// private
//// U8 swapped[29];
//// }
////
//// class LasWriteItemRaw_BYTE extends LasWriteItemRaw
//// {
////
//// LasWriteItemRaw_BYTE(U32 number)
//// {
//// this.number = number;
//// }
//// public void write(LasPoint point, int context)
//// {
//// outstream.putBytes(item, number);
//// }
//// private
//// U32 number;
//// }
////
//// class LAStempWritePoint10
//// {
////
//// I32 X;
//// I32 Y;
//// I32 Z;
//// U16 intensity;
//// U8 return_number : 3;
//// U8 number_of_returns : 3;
//// U8 scan_direction_flag : 1;
//// U8 edge_of_flight_line : 1;
//// U8 classification;
//// I8 scan_angle_rank;
//// U8 user_data;
//// U16 point_source_ID;
////
//// // LAS 1.4 only
//// I16 extended_scan_angle;
//// U8 extended_point_type : 2;
//// U8 extended_scanner_channel : 2;
//// U8 extended_classification_flags : 4;
//// U8 extended_classification;
//// U8 extended_return_number : 4;
//// U8 extended_number_of_returns : 4;
////
//// // for 8 byte alignment of the GPS time
//// U8 dummy[3];
////
//// // LASlib only
//// U32 deleted_flag;
////
//// F64 gps_time;
//// }
////
//// class LAStempWritePoint14
//// {
////
//// I32 X;
//// I32 Y;
//// I32 Z;
//// U16 intensity;
//// U8 return_number : 4;
//// U8 number_of_returns : 4;
//// U8 classification_flags : 4;
//// U8 scanner_channel : 2;
//// U8 scan_direction_flag : 1;
//// U8 edge_of_flight_line : 1;
//// U8 classification;
//// U8 user_data;
//// I16 scan_angle;
//// U16 point_source_ID;
//// }
////
//// class LasWriteItemRaw_POINT14 extends LasWriteItemRaw
//// {
////
//// LasWriteItemRaw_POINT14(final ChannelWriter outstream){super(outstream);}
//// public void write(LasPoint point, int context)
//// {
//// ENDIAN_SWAP_32(item[ 0], swapped[ 0]); // X
//// ENDIAN_SWAP_32(item[ 4], swapped[ 4]); // Y
//// ENDIAN_SWAP_32(item[ 8], swapped[ 8]); // Z
//// ENDIAN_SWAP_16(item[12], swapped[12]); // intensity
//// ((LAStempWritePoint14*)swapped).scan_direction_flag =
//// ((LAStempWritePoint10*)item).scan_direction_flag;
//// ((LAStempWritePoint14*)swapped).edge_of_flight_line =
//// ((LAStempWritePoint10*)item).edge_of_flight_line;
//// ((LAStempWritePoint14*)swapped).classification =
//// (((LAStempWritePoint10*)item).classification 31);
//// ((LAStempWritePoint14*)swapped).user_data =
//// ((LAStempWritePoint10*)item).user_data;
//// ENDIAN_SWAP_16(item[18], swapped[20]); // point_source_ID
////
//// if (((LAStempWritePoint10*)item).extended_point_type)
//// {
//// ((LAStempWritePoint14*)swapped).classification_flags =
//// (((LAStempWritePoint10*)item).extended_classification_flags 8) |
//// (((LAStempWritePoint10*)item).classification >> 5);
//// if (((LAStempWritePoint14*)swapped).classification == 0)
//// ((LAStempWritePoint14*)swapped).classification =
//// ((LAStempWritePoint10*)item).extended_classification;
//// ((LAStempWritePoint14*)swapped).scanner_channel =
//// ((LAStempWritePoint10*)item).extended_scanner_channel;
//// ((LAStempWritePoint14*)swapped).return_number =
//// ((LAStempWritePoint10*)item).extended_return_number;
//// ((LAStempWritePoint14*)swapped).number_of_returns =
//// ((LAStempWritePoint10*)item).extended_number_of_returns;
//// ENDIAN_SWAP_16(item[20], swapped[18]); // scan_angle
//// }
//// else
//// {
//// ((LAStempWritePoint14*)swapped).classification_flags =
//// (((LAStempWritePoint10*)item).classification >> 5);
//// ((LAStempWritePoint14*)swapped).scanner_channel = 0;
//// ((LAStempWritePoint14*)swapped).return_number =
//// ((LAStempWritePoint10*)item).return_number;
//// ((LAStempWritePoint14*)swapped).number_of_returns =
//// ((LAStempWritePoint10*)item).number_of_returns;
//// I16 scan_angle =
//// I16_QUANTIZE(((LAStempWritePoint10*)item).scan_angle_rank/0.006f);
//// ENDIAN_SWAP_16((U8*)(scan_angle), swapped[18]); // scan_angle
//// }
//// ENDIAN_SWAP_64((U8*)(((LAStempWritePoint10*)item).gps_time), swapped[22]);
//// outstream.putBytes(swapped, 30);
//// }
//// private
//// U8 swapped[30];
//// }
////
////
//// class LasWriteItemRaw_RGBNIR14 extends LasWriteItemRaw
//// {
////
//// LasWriteItemRaw_RGBNIR14(final ChannelWriter outstream){super(outstream);}
//// public void write(LasPoint point, int context)
//// {
//// ENDIAN_SWAP_32(item[ 0], swapped[ 0]); // R
//// ENDIAN_SWAP_32(item[ 2], swapped[ 2]); // G
//// ENDIAN_SWAP_32(item[ 4], swapped[ 4]); // B
//// ENDIAN_SWAP_32(item[ 6], swapped[ 6]); // NIR
//// outstream.putBytes(swapped, 8);
//// }
//// private
//// U8 swapped[8];
//// }
