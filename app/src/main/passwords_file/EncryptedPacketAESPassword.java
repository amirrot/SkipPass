package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EncryptedPacketAESPassword {
  public SaltedPassword saltedPassword;
  public EncryptedDataAES cipher;
  public static final PacketDesc<EncryptedPacketAESPassword> def = new PacketDesc<EncryptedPacketAESPassword>() {

    @Override
    public EncryptedPacketAESPassword read(InputStream is) throws IOException, ParseError {
      EncryptedPacketAESPassword res = new EncryptedPacketAESPassword();
      res.saltedPassword = SaltedPassword.def.read(is);
      res.cipher = EncryptedDataAES.def.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, EncryptedPacketAESPassword msg) throws IOException {
      SaltedPassword.def.write(os, msg.saltedPassword);
      EncryptedDataAES.def.write(os, msg.cipher);
    }

    @Override
    public EncryptedPacketAESPassword create() {
      EncryptedPacketAESPassword res = new EncryptedPacketAESPassword();
      res.saltedPassword = SaltedPassword.def.create();
      res.cipher = EncryptedDataAES.def.create();
      return res;
    }
  };
}
