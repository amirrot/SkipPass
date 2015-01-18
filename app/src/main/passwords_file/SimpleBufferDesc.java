package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

public class SimpleBufferDesc implements PacketDesc<byte[]> {
  private int size;
  private static SecureRandom random = new SecureRandom();

  public SimpleBufferDesc(int size) {
    this.size = size;
  }

  @Override public byte[] read(InputStream is) throws IOException, ParseError {
    byte[] res = new byte[size];
    if (is.read(res) != size) {
      throw new ParseError();
    }
    return res;
  }

  @Override public void write(OutputStream os, byte[] msg) throws IOException {
    assert msg.length == size;
    os.write(msg);
  }

  @Override public byte[] create() {
    byte[] res = new byte[size];
    random.nextBytes(res);
    return res;
  }
}
