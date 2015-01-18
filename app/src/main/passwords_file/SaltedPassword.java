package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SaltedPassword {
  public byte[] aux;
  private static final SimpleBufferDesc simpleBufferDesc20 = new SimpleBufferDesc(20);
  public static final PacketDesc<SaltedPassword> def = new PacketDesc<SaltedPassword>() {
    @Override
    public SaltedPassword read(InputStream is) throws IOException, ParseError {
      SaltedPassword res = new SaltedPassword();
      res.aux = simpleBufferDesc20.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, SaltedPassword msg) throws IOException {
      simpleBufferDesc20.write(os, msg.aux);
    }

    @Override
    public SaltedPassword create() {
      SaltedPassword res = new SaltedPassword();
      res.aux = simpleBufferDesc20.create();
      return res;
    }
  };
}
