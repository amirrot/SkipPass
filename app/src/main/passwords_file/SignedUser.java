package com.google.android.skippass.passwords_file;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignedUser {
  private User user;
  private PrivateKey privKey;
  private List<Password> decPasswords = new ArrayList<Password>();

  public SignedUser(String password) {
    user = User.def.create();
    SecureRandom random = new SecureRandom();
    while (true) {
      BigInteger p = BigInteger.probablePrime(1024, random);
      BigInteger q = BigInteger.probablePrime(1024, random);
      BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
      user.pubKey.mod = p.multiply(q);
      user.pubKey.e = BigInteger.valueOf(65537);
      if (phi.mod(user.pubKey.e).equals(BigInteger.ZERO)) {
        continue;
      }
      privKey = new PrivateKey();
      privKey.d = user.pubKey.e.modInverse(phi);
      BigInteger a = BigInteger.valueOf(12345);
      BigInteger b = a.modPow(user.pubKey.e, user.pubKey.mod);
      BigInteger c = b.modPow(privKey.d, user.pubKey.mod);
      if (a.equals(c)) {
        break;
      }
    }
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      PrivateKey.def.write(bos, privKey);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
    byte[] key = saltToKey(user.encPrivKey.saltedPassword, password);
    encryptPacket(user.encPrivKey.cipher, key, bos.toByteArray());
    Password pwd1 = Password.def.create();
    pwd1.name = "My App";
    pwd1.username = "nadav.sherman@gmail.com";
    pwd1.password = "password";
    addPassword(pwd1);
  }

  public SignedUser(InputStream is, String password) throws IOException, ParseError {
    this.user = User.def.read(is);
    byte[] key = saltToKey(user.encPrivKey.saltedPassword, password);
    byte[] privKeyData = decryptPacket(user.encPrivKey.cipher, key);
    this.privKey = PrivateKey.def.read(new ByteArrayInputStream(privKeyData));
    for (EncryptedPacketAESPK encPassword : user.encPasswords) {
      this.decPasswords.add(decryptPassword(encPassword));
    }
  }

  public void writeToDisk(OutputStream os) throws IOException {
    User.def.write(os, this.user);
  }

  private Password decryptPassword(EncryptedPacketAESPK encPassword) throws IOException, ParseError {
    OaepDesc.Oaep d = decryptUsingPrivateKey(encPassword.key.data);
    byte[] m = decryptPacket(encPassword.cipher, d.msg);
    return Password.def.read(new ByteArrayInputStream(m));
  }

  private EncryptedPacketAESPK encryptPassword(Password pwd) {
    ByteArrayOutputStream io = new ByteArrayOutputStream();
    try {
      Password.def.write(io, pwd);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
    OaepDesc.Oaep oaep = OaepDesc.def.create();
    EncryptedPacketAESPK res = EncryptedPacketAESPK.def.create();
    res.key.data = encryptUsingPublicKey(oaep);
    encryptPacket(res.cipher, oaep.msg, io.toByteArray());
    return res;
  }

  private OaepDesc.Oaep decryptUsingPrivateKey(BigInteger data) throws IOException, ParseError {
    BigInteger plain = data.modPow(privKey.d, user.pubKey.mod);
    byte[] buf = LongDesc.num2Buf(plain);
    if (buf.length < 255) {
      buf = Utils.concat(new byte[255 - buf.length], buf);
    }
    return OaepDesc.def.read(new ByteArrayInputStream(buf));
  }

  private BigInteger encryptUsingPublicKey(OaepDesc.Oaep oaep) {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      OaepDesc.def.write(os, oaep);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
    BigInteger a = LongDesc.buf2Num(os.toByteArray());
    return a.modPow(user.pubKey.e, user.pubKey.mod);
  }

  private static byte[] decryptPacket(EncryptedDataAES message, byte[] key) throws ParseError, IOException {
    Log.d("SignedUser Decrypt:", Arrays.toString(message.data));
    Rijndael rd = new Rijndael();
    rd.makeKey(key, key.length * 8, Rijndael.DIR_DECRYPT);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] data = message.data.clone();
    byte[] iv = message.iv.clone();
    if (data.length % 16 != 0) {
      throw new ParseError();
    }
    if (iv.length != 16) {
      throw new ParseError();
    }
    byte[] temp = Utils.concat(iv, data);
    for (int i = 16; i < temp.length; i += 16) {
      iv = Utils.cut(temp, i - 16, 16);
      byte[] buf = new byte[16];
      rd.encrypt(iv, buf);
      out.write(Utils.xor(Utils.cut(temp, i, 16), buf));
    }
    byte[] buf = out.toByteArray();
    Log.d("SignedUser Decrypted:", Arrays.toString(buf));
    int pad = buf[buf.length - 1];
    if (pad <= 0 || pad > 16 || pad > buf.length) {
      throw new ParseError();
    }
    for (int i = 0; i < pad; i++) {
      if (buf[buf.length - i - 1] != pad) {
        throw new ParseError();
      }
    }
    return Utils.cut(buf, buf.length - pad);
  }

  private static void encryptPacket(EncryptedDataAES packet, byte[] key, byte[] data) {
    Rijndael rd = new Rijndael();
    rd.makeKey(key, key.length * 8, Rijndael.DIR_ENCRYPT);
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    try {
      os.write(data);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
    int pad = 16 - (os.size() % 16);
    for (int i = 0; i < pad; i++) {
      os.write(pad);
    }
    byte[] data2 = os.toByteArray();
    Log.d("SignedUser Encrypt:", Arrays.toString(data2));
    byte[] iv = packet.iv.clone();
    os = new ByteArrayOutputStream();
    for (int i = 0; i < data.length; i += 16) {
      byte[] buf = new byte[16];
      rd.encrypt(iv, buf);
      iv = Utils.xor(buf, Utils.cut(data2, i, 16));
      try {
        os.write(iv);
      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException();
      }
    }
    packet.data = os.toByteArray();
    Log.d("SignedUser Encrypted:", Arrays.toString(packet.data));
  }

  private static byte[] saltToKey(SaltedPassword salt, String password) {
    final int ROUNDS = 1 << 10;
    byte[] pwd;
    try {
      pwd = password.getBytes("UTF-16LE");
    } catch (UnsupportedEncodingException e) {
      assert false;
      return null;
    }
    byte[] buf = salt.aux.clone();
    for (int i = 0; i < ROUNDS; i++) {
      byte[] counter = new byte[4];
      counter[0] = (byte) (i >>> 0);
      counter[1] = (byte) (i >>> 8);
      counter[2] = (byte) (i >>> 16);
      counter[3] = (byte) (i >>> 24);
      buf = Utils.hash(Utils.concat(buf, pwd, counter));
    }
    return Utils.cut(buf, 16);
  }

  public List<Password> getPasswords() {
    return decPasswords;
  }

  public void updatePassword(Password pwd) {
    for (int i = 0; i < decPasswords.size(); i++) {
      if (decPasswords.get(i) == pwd) {
        user.encPasswords.set(i, encryptPassword(pwd));
      }
    }
  }

  public void addPassword(Password pwd) {
    decPasswords.add(pwd);
    user.encPasswords.add(encryptPassword(pwd));
  }
}
