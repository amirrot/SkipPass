package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class PublicKey {
  public BigInteger mod;
  public BigInteger e;
  public static final PacketDesc<PublicKey> def = new PacketDesc<PublicKey>() {
    @Override
    public PublicKey read(InputStream is) throws IOException, ParseError {
      PublicKey res = new PublicKey();
      res.mod = LongDesc.def.read(is);
      res.e = LongDesc.def.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, PublicKey msg) throws IOException {
      LongDesc.def.write(os, msg.mod);
      LongDesc.def.write(os, msg.e);
    }

    @Override
    public PublicKey create() {
      PublicKey res = new PublicKey();
      res.mod = LongDesc.def.create();
      res.e = LongDesc.def.create();
      return res;
    }
  };
}
