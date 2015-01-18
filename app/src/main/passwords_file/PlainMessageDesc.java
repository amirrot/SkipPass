package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PlainMessageDesc<T> implements PacketDesc<T> {
  private PacketDesc<T> desc;

  public PlainMessageDesc(PacketDesc<T> desc) {
    this.desc = desc;
  }

  @Override public T read(InputStream is) throws IOException, ParseError {
    CheckBufferDesc.def.read(is);
    return desc.read(is);
  }

  @Override public void write(OutputStream os, T msg) throws IOException {
    CheckBufferDesc.def.write(os, CheckBufferDesc.def.create());
    desc.write(os, msg);
  }

  @Override public T create() {
    return desc.create();
  }
}
