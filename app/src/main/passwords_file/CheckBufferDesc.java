package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CheckBufferDesc implements PacketDesc<byte[]> {
  public static final CheckBufferDesc def = new CheckBufferDesc();
  private static final byte[] CHECK = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 };

  private CheckBufferDesc() {}

  @Override public byte[] read(InputStream is) throws IOException, ParseError {
    byte[] buf = new byte[CHECK.length];
    if (is.read(buf) != buf.length) {
      throw new ParseError();
    }
    if (!Utils.compare(CHECK, buf)) {
      throw new ParseError();
    }
    return buf;
  }

  @Override public void write(OutputStream os, byte[] msg) throws IOException {
    assert Utils.compare(CHECK, msg);
    os.write(msg);
  }

  @Override public byte[] create() {
    return CHECK.clone();
  }
}
