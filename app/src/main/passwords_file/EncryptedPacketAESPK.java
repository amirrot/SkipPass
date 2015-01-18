package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EncryptedPacketAESPK {
  public EncryptedDataPK key;
  public EncryptedDataAES cipher;

  public static final PacketDesc<EncryptedPacketAESPK> def = new PacketDesc<EncryptedPacketAESPK>() {
    @Override
    public EncryptedPacketAESPK read(InputStream is) throws IOException, ParseError {
      EncryptedPacketAESPK res = new EncryptedPacketAESPK();
      res.key = EncryptedDataPK.def.read(is);
      res.cipher = EncryptedDataAES.def.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, EncryptedPacketAESPK msg) throws IOException {
      EncryptedDataPK.def.write(os, msg.key);
      EncryptedDataAES.def.write(os, msg.cipher);
    }

    @Override
    public EncryptedPacketAESPK create() {
      EncryptedPacketAESPK res = new EncryptedPacketAESPK();
      res.key = EncryptedDataPK.def.create();
      res.cipher = EncryptedDataAES.def.create();
      return res;
    }
  };
}
