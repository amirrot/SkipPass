package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StrDesc implements PacketDesc<String> {
  public static final StrDesc def = new StrDesc();

  private StrDesc() {}

  @Override public String read(InputStream is) throws IOException, ParseError {
    byte[] buf = BufDesc.def.read(is);
    return new String(buf);
  }

  @Override public void write(OutputStream os, String msg) throws IOException {
    byte[] buf = msg.getBytes();
    BufDesc.def.write(os, buf);
  }

  @Override public String create() {
    return "";
  }
}
