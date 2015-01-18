package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

public class EncryptedDataPK {
  public BigInteger data;
  public static final PacketDesc<EncryptedDataPK> def = new PacketDesc<EncryptedDataPK>() {

    @Override
    public EncryptedDataPK read(InputStream is) throws IOException, ParseError {
      EncryptedDataPK res = new EncryptedDataPK();
      res.data = LongDesc.def.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, EncryptedDataPK msg) throws IOException {
      LongDesc.def.write(os, msg.data);
    }

    @Override
    public EncryptedDataPK create() {
      EncryptedDataPK res = new EncryptedDataPK();
      res.data = LongDesc.def.create();
      return res;
    }
  };
}
