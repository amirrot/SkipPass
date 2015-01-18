package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ListDesc<T> implements PacketDesc<List<T>> {
  private final PacketDesc<T> desc;

  public ListDesc(PacketDesc<T> desc) {
    this.desc = desc;
  }

  @Override
  public List<T> read(InputStream is) throws IOException, ParseError {
    List<T> res = new ArrayList<T>();
    int l = IntDesc.def.read(is);
    for (int i = 0; i < l; i++) {
      res.add(desc.read(is));
    }
    return res;
  }

  @Override
  public void write(OutputStream os, List<T> msg) throws IOException {
    IntDesc.def.write(os, msg.size());
    for (T t : msg) {
      desc.write(os, t);
    }
  }

  @Override
  public List<T> create() {
    return new ArrayList<T>();
  }
}
