package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EncryptedDataAES {
  public byte[] iv;
  public byte[] data;
  private static final SimpleBufferDesc simpleBufferDesc16 = new SimpleBufferDesc(16);
  public static final PacketDesc<EncryptedDataAES> def = new PacketDesc<EncryptedDataAES>() {

    @Override
    public EncryptedDataAES read(InputStream is) throws IOException, ParseError {
      EncryptedDataAES res = new EncryptedDataAES();
      res.iv = simpleBufferDesc16.read(is);
      res.data = BufDesc.def.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, EncryptedDataAES msg) throws IOException {
      simpleBufferDesc16.write(os, msg.iv);
      BufDesc.def.write(os, msg.data);
    }

    @Override
    public EncryptedDataAES create() {
      EncryptedDataAES res = new EncryptedDataAES();
      res.iv = simpleBufferDesc16.create();
      res.data = BufDesc.def.create();
      return res;
    }
  };
}
