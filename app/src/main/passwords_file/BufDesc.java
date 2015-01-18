package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BufDesc implements PacketDesc<byte[]> {
  public static final BufDesc def = new BufDesc();

  private BufDesc() {}

  @Override public byte[] read(InputStream is) throws IOException, ParseError {
    int l = IntDesc.def.read(is);
    byte[] res = new byte[l];
    if (is.read(res) != l) {
      throw new ParseError();
    }
    return res;
  }

  @Override public void write(OutputStream os, byte[] msg) throws IOException {
    IntDesc.def.write(os, msg.length);
    os.write(msg);
  }

  @Override public byte[] create() {
    return new byte[0];
  }
}
