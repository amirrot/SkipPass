package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface PacketDesc<T> {
  public T read(InputStream is) throws IOException, ParseError;

  public void write(OutputStream os, T msg) throws IOException;

  public T create();
}
