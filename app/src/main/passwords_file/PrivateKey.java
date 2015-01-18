package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class PrivateKey {
  public BigInteger d;
  public static final PacketDesc<PrivateKey> def = new PlainMessageDesc<PrivateKey>(new PacketDesc<PrivateKey>() {
    @Override
    public PrivateKey read(InputStream is) throws IOException, ParseError {
      PrivateKey res = new PrivateKey();
      res.d = LongDesc.def.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, PrivateKey msg) throws IOException {
      LongDesc.def.write(os, msg.d);
    }

    @Override
    public PrivateKey create() {
      PrivateKey res = new PrivateKey();
      res.d = LongDesc.def.create();
      return res;
    }
  });
}
