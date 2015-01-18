package com.google.android.skippass.passwords_file;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class User {
  public PublicKey pubKey;
  public EncryptedPacketAESPassword encPrivKey;
  public List<EncryptedPacketAESPK> encPasswords;
  private static final ListDesc<EncryptedPacketAESPK> list = new ListDesc<EncryptedPacketAESPK>(EncryptedPacketAESPK.def);
  public static final PacketDesc<User> def = new PacketDesc<User>() {

    @Override
    public User read(InputStream is) throws IOException, ParseError {
      User res= new User();
      res.pubKey = PublicKey.def.read(is);
      res.encPrivKey = EncryptedPacketAESPassword.def.read(is);
      res.encPasswords = list.read(is);
      return res;
    }

    @Override
    public void write(OutputStream os, User msg) throws IOException {
      PublicKey.def.write(os, msg.pubKey);
      EncryptedPacketAESPassword.def.write(os, msg.encPrivKey);
      list.write(os, msg.encPasswords);
    }

    @Override
    public User create() {
      User res= new User();
      res.pubKey = PublicKey.def.create();
      res.encPrivKey = EncryptedPacketAESPassword.def.create();
      res.encPasswords = list.create();
      return res;
    }
  };
}
