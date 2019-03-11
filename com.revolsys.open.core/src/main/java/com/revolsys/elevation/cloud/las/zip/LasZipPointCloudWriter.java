package com.revolsys.elevation.cloud.las.zip;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.elevation.cloud.las.LasPointCloudHeader;
import com.revolsys.elevation.cloud.las.LasPointCloudWriter;
import com.revolsys.elevation.cloud.las.LasVariableLengthRecord;
import com.revolsys.elevation.cloud.las.pointformat.LasPoint;
import com.revolsys.math.arithmeticcoding.ArithmeticCodingInteger;
import com.revolsys.math.arithmeticcoding.ArithmeticEncoder;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Pair;

public class LasZipPointCloudWriter extends LasPointCloudWriter {
  private LasZipCompressorType compressor = LasZipCompressorType.POINTWISE;

  private LasZipHeader lasZipHeader;

  private boolean isNewHeader = false;

  private int lasZipVersion = 1;

  private int number_chunks = 0;

  private long chunk_table_start_position = -1;

  private int chunk_count;

  private long chunk_start_position;

  private LasZipItemCodec[] codecs;

  private long chunk_size = Integer.MAX_VALUE;

  private ArithmeticEncoder encoder;

  private int context;

  private int alloced_chunks;

  private int[] chunk_bytes;

  private int[] chunk_sizes;

  public LasZipPointCloudWriter(final LasPointCloud pointCloud, final Resource resource,
    final MapEx properties) {
    super(resource);
    setProperties(properties);
    setPointCloud(pointCloud);
  }

  private void add_chunk_to_table() {
    if (this.number_chunks == this.alloced_chunks) {
      if (this.chunk_bytes == null) {
        this.alloced_chunks = 1024;
        if (this.chunk_size == Integer.MAX_VALUE) {
          this.chunk_sizes = new int[this.alloced_chunks];
        }
        this.chunk_bytes = new int[this.alloced_chunks];
      } else {
        this.alloced_chunks *= 2;
        if (this.chunk_size == Integer.MAX_VALUE) {
          final int[] chunkSizes = new int[this.alloced_chunks];
          System.arraycopy(this.chunk_sizes, 0, chunkSizes, 0, this.chunk_sizes.length);
          this.chunk_sizes = chunkSizes;
        }
        final int[] chunkBytes = new int[this.alloced_chunks];
        System.arraycopy(this.chunk_bytes, 0, chunkBytes, 0, this.chunk_bytes.length);
        this.chunk_bytes = chunkBytes;
      }
      if (this.chunk_size == Integer.MAX_VALUE && this.chunk_sizes == null) {
        return;
      }
      if (this.chunk_bytes == null) {
        return;
      }
    }
    final long position = this.out.position();
    if (this.chunk_size == Integer.MAX_VALUE) {
      this.chunk_sizes[this.number_chunks] = this.chunk_count;
    }
    this.chunk_bytes[this.number_chunks] = (int)(position - this.chunk_start_position);
    this.chunk_start_position = position;
    this.number_chunks++;
  }

  @Override
  public void close() {
    done();
    super.close();
  }

  public void done() {

    if (this.compressor == LasZipCompressorType.LAYERED_CHUNKED) {
      // write how many points are in the chunk
      this.out.putInt(this.chunk_count);
      // write all layers
      for (final LasZipItemCodec codec : this.codecs) {
        codec.writeChunkSizes();
      }
      for (final LasZipItemCodec codec : this.codecs) {
        codec.writeChunkBytes();
      }
    }
    this.encoder.done();
    if (this.chunk_start_position != 0) {
      if (this.chunk_count != 0) {
        add_chunk_to_table();
      }
      write_chunk_table();
    }
    // }
    // else if (writers == 0)
    // {
    // if (chunk_start_position)
    // {
    // return write_chunk_table();
    // }
    // }
  }

  public LasZipCompressorType getCompressor() {
    return this.compressor;
  }

  @Override
  protected Map<Pair<String, Integer>, LasVariableLengthRecord> getLasProperties(
    final LasPointCloudHeader header) {
    final LasPointCloud pointCloud = header.getPointCloud();
    Map<Pair<String, Integer>, LasVariableLengthRecord> lasProperties = super.getLasProperties(
      header);
    if (this.isNewHeader) {
      lasProperties = new LinkedHashMap<>(lasProperties);
      final LasVariableLengthRecord property = new LasVariableLengthRecord(pointCloud,
        LasZipHeader.KAY_LAS_ZIP, "laszip", this.lasZipHeader);
      lasProperties.put(LasZipHeader.KAY_LAS_ZIP, property);
    }
    this.chunk_size = this.lasZipHeader.getChunkSize();
    this.chunk_count = (int)this.chunk_size;
    return lasProperties;
  }

  public int getLasZipVersion() {
    return this.lasZipVersion;
  }

  @Override
  protected void open() {
    super.open();
    if (this.compressor != LasZipCompressorType.POINTWISE) {
      this.chunk_count = 0;
      this.number_chunks = Integer.MAX_VALUE;
    }
    this.encoder = new ArithmeticEncoder(this.out);
    this.codecs = this.lasZipHeader.newLazCodecs(this.encoder);
    if (this.number_chunks == Integer.MAX_VALUE) {
      this.number_chunks = 0;
      if (this.out.isSeekable()) {
        this.chunk_table_start_position = this.out.position();
      } else {
        this.chunk_table_start_position = -1;
      }
      this.out.putLong(this.chunk_table_start_position);
      this.chunk_start_position = this.out.position();
    }

  }

  public void setCompressor(final LasZipCompressorType compressor) {
    this.compressor = compressor;
  }

  public void setLasZipVersion(final int lasZipVersion) {
    this.lasZipVersion = lasZipVersion;
  }

  @Override
  protected void setPointCloud(final LasPointCloud pointCloud) {
    this.lasZipHeader = LasZipHeader.getLasZipHeader(pointCloud);
    if (this.lasZipHeader == null) {
      this.lasZipHeader = LasZipHeader.newLasZipHeader(pointCloud, this.compressor,
        this.lasZipVersion);
      this.compressor = this.lasZipHeader.getCompressor();
      this.isNewHeader = true;
    }
    super.setPointCloud(pointCloud);
  }

  private void startChunk() {
    if (this.number_chunks == Integer.MAX_VALUE) {
      this.number_chunks = 0;
      this.chunk_table_start_position = this.out.position();
      this.out.putLong(this.chunk_table_start_position);
      this.chunk_start_position = this.out.position();
    }
  }

  private void write_chunk_table() {
    final long position = this.out.position();
    if (this.chunk_table_start_position != -1) {
      // stream is seekable
      this.out.seek(this.chunk_table_start_position);
      this.out.putLong(position);
      this.out.seek(position);
    }
    this.out.putInt(0); // version
    this.out.putInt(this.number_chunks);

    if (this.number_chunks > 0) {
      this.encoder.init();
      final ArithmeticCodingInteger ic = new ArithmeticCodingInteger(this.encoder, 32, 2);
      ic.initCompressor();
      for (int i = 0; i < this.number_chunks; i++) {
        if (this.chunk_size == Integer.MAX_VALUE) {
          ic.compress(i != 0 ? this.chunk_sizes[i - 1] : 0, this.chunk_sizes[i], 0);
        }
        ic.compress(i != 0 ? this.chunk_bytes[i - 1] : 0, this.chunk_bytes[i], 1);
      }
      this.encoder.done();
    }
    if (this.chunk_table_start_position == -1) {
      // stream is not-seekable
      this.out.putLong(position);
    }
  }

  @Override
  public void writePoint(final LasPoint point) {
    this.header.addCounts(point);
    if (this.chunk_count == this.chunk_size) {
      if (this.compressor == LasZipCompressorType.LAYERED_CHUNKED) {
        this.out.putInt(this.chunk_count);
        for (final LasZipItemCodec codec : this.codecs) {
          codec.writeChunkSizes();
        }
        for (final LasZipItemCodec codec : this.codecs) {
          codec.writeChunkBytes();
        }
        add_chunk_to_table();
        startChunk();
      } else if (this.compressor == LasZipCompressorType.POINTWISE_CHUNKED) {
        this.encoder.done();
        add_chunk_to_table();
        startChunk();
      }
      this.chunk_count = 0;
    }
    this.chunk_count++;
    if (this.chunk_count == 1) {
      point.writeLasPoint(this.out);
      for (final LasZipItemCodec codec : this.codecs) {
        this.context = codec.init(point, this.context);
      }
      this.encoder.init();
    } else {
      for (final LasZipItemCodec codec : this.codecs) {
        this.context = codec.write(point, this.context);
      }
    }
  }
}
