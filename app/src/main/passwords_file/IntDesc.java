package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IntDesc implements PacketDesc<Integer> {
  @Override public Integer read(InputStream is) throws IOException {
    int res = 0;
    for (int i = 0; i < 4; i++) {
      res += ((is.read()) << (8 * i));
    }
    return res;
  }

  @Override public void write(OutputStream os, Integer msg) throws IOException {
    long l = msg.intValue();
    for (int i = 0; i < 4; i++) {
      os.write((int) (l & 0xff));
      l >>>= 8;
    }
  }

  @Override public Integer create() {
    return 0;
  }

  public static final IntDesc def = new IntDesc();
}
