package com.google.android.skippass.passwords_file;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Utils {
  public static final SecureRandom random = new SecureRandom();
  public static final byte[] EMPTY_HASH = hash(new byte[0]);
  public static final char[] hexArray = "0123456789ABCDEF".toCharArray();
  public static final String ABC_l = "abcdefghijklmnopqrstuvwxyz";
  public static final String ABC_u = ABC_l.toUpperCase();
  public static final String ABC_d = "0123456789";
  public static final String ABC_s = "~`!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?";
  public static final String ABC_ld = ABC_l + ABC_d;
  public static final String ABC_ud = ABC_u + ABC_d;
  public static final String ABC_lud = ABC_l + ABC_u + ABC_d;
  public static final String ABC_all = ABC_l + ABC_u + ABC_d + ABC_s;

  public static byte[] xor(byte[] b1, byte[] b2) {
    assert b1.length == b2.length;
    byte[] res = new byte[b1.length];
    for (int i = 0; i < b1.length; i++) {
      res[i] = (byte) (b1[i] ^ b2[i]);
    }
    return res;
  }

  public static byte[] hash(byte[] buf) {
    MessageDigest sha;
    try {
      sha = MessageDigest.getInstance("SHA-1");
    } catch (NoSuchAlgorithmException e) {
      assert false;
      return null;
    }
    return sha.digest(buf);
  }

  public static byte[] concat(byte[]... params) {
    int total = 0;
    for (byte[] b : params) {
      total += b.length;
    }
    byte[] res = new byte[total];
    int pos = 0;
    for (byte[] b : params) {
      for (int i = 0; i < b.length; i++) {
        res[pos + i] = b[i];
      }
      pos += b.length;
    }
    return res;
  }

  public static byte[] cut(byte[] buf, int start, int len) {
    assert start >= 0;
    assert buf.length >= start + len;
    if (start == 0 && buf.length == len) {
      return buf;
    }
    byte[] res = new byte[len];
    for (int i = 0; i < len; i++) {
      res[i] = buf[start + i];
    }
    return res;
  }

  public static byte[] cut(byte[] buf, int len) {
    return cut(buf, 0, len);
  }

  public static String toHex(byte[] buf) {
    char[] hexChars = new char[buf.length * 2];
    for (int j = 0; j < buf.length; j++) {
      int v = buf[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static String genPassword(String abc, int l) {
    char[] res = new char[l];
    for (int i = 0; i < l; i++) {
      res[i] = abc.charAt(random.nextInt(abc.length()));
    }
    return new String(res);
  }

  public static boolean compare(byte[] b1, byte[] b2) {
    if (b1 == b2) {
      return true;
    }
    if (b1 == null || b2 == null) {
      return false;
    }
    if (b1.length != b2.length) {
      return false;
    }
    for (int i = 0; i < b1.length; i++) {
      if (b1[i] != b2[i]) {
        return false;
      }
    }
    return true;
  }
}
